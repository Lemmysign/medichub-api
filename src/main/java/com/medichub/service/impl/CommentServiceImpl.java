package com.medichub.service.impl;

import com.medichub.dto.request.CreateCommentRequest;
import com.medichub.dto.request.CreateReplyRequest;
import com.medichub.dto.response.CommentResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.exception.AccessDeniedException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Course;
import com.medichub.model.CourseComment;
import com.medichub.model.Topic;
import com.medichub.model.User;
import com.medichub.repository.CourseCommentRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CommentService;
import com.medichub.service.CourseService;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CourseCommentRepository commentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final SubscriptionAccessService subscriptionAccessService;

    public CommentServiceImpl(CourseCommentRepository commentRepository,
                              TopicRepository topicRepository,
                              UserRepository userRepository,
                              CourseService courseService,
                              SubscriptionAccessService subscriptionAccessService) {
        this.commentRepository = commentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Override
    public CommentResponse postQuestion(Long courseId, CreateCommentRequest request) {
        Long studentId = SecurityUtils.currentUserId();
        Course course = courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(studentId);

        Topic topic = null;
        if (request.topicId() != null) {
            topic = topicRepository.findByIdAndCourseId(request.topicId(), courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", request.topicId()));
        }

        User author = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        CourseComment comment = new CourseComment();
        comment.setCourse(course);
        comment.setAuthor(author);
        comment.setAuthorName(author.getFullName());
        comment.setTopic(topic);
        comment.setText(request.text());
        comment = commentRepository.save(comment);

        return toResponse(comment, courseId, List.of());
    }

    @Override
    public CommentResponse postReply(Long courseId, Long commentId, CreateReplyRequest request) {
        Long instructorId = SecurityUtils.currentUserId();
        Course course = courseService.requireOwnedCourse(courseId, instructorId);

        CourseComment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        if (!parent.getCourse().getId().equals(courseId)) {
            throw new AccessDeniedException("Comment does not belong to this course");
        }

        User author = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", instructorId));

        CourseComment reply = new CourseComment();
        reply.setCourse(course);
        reply.setAuthor(author);
        reply.setAuthorName(author.getFullName());
        reply.setTopic(parent.getTopic());
        reply.setParent(parent);
        reply.setText(request.text());
        reply = commentRepository.save(reply);

        return toResponse(reply, courseId, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> listCourseThreads(Long courseId, Pageable pageable) {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        courseService.requirePublishedCourse(courseId);
        Page<CourseComment> roots = commentRepository
                .findByCourseIdAndParentIsNullOrderByCreatedAtDesc(courseId, pageable);
        return buildThreadPage(roots, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> listInstructorQuestions(boolean unansweredOnly, Pageable pageable) {
        Long instructorId = SecurityUtils.currentUserId();
        Page<CourseComment> roots = unansweredOnly
                ? commentRepository.findUnansweredRootsByInstructor(instructorId, pageable)
                : commentRepository.findByCourseInstructorIdAndParentIsNullOrderByCreatedAtDesc(instructorId, pageable);
        return buildThreadPage(roots, null);
    }

    // ----------------------------------------------------------------------

    /** Attach replies to each root in one extra query (no per-root N+1). */
    private PagedResponse<CommentResponse> buildThreadPage(Page<CourseComment> roots, Long courseIdOrNull) {
        List<Long> rootIds = roots.getContent().stream().map(CourseComment::getId).toList();
        Map<Long, List<CourseComment>> repliesByParent = rootIds.isEmpty()
                ? Map.of()
                : commentRepository.findByParentIdInOrderByCreatedAtAsc(rootIds).stream()
                        .collect(Collectors.groupingBy(r -> r.getParent().getId()));

        return PagedResponse.from(roots, root -> {
            Long courseId = courseIdOrNull != null ? courseIdOrNull : root.getCourse().getId();
            List<CommentResponse> replies = repliesByParent.getOrDefault(root.getId(), List.of()).stream()
                    .map(reply -> toResponse(reply, courseId, List.of()))
                    .toList();
            return toResponse(root, courseId, replies);
        });
    }

    private CommentResponse toResponse(CourseComment comment, Long courseId, List<CommentResponse> replies) {
        User author = comment.getAuthor();
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                author.getId(),
                author.getFullName(),
                author.getRole(),
                courseId,
                comment.getCourse() == null ? null : comment.getCourse().getTitle(),
                comment.getTopic() == null ? null : comment.getTopic().getId(),
                comment.getTopic() == null ? null : comment.getTopic().getTitle(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                !replies.isEmpty(),
                comment.getCreatedAt(),
                replies);
    }
}
