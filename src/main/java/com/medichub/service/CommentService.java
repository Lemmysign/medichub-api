package com.medichub.service;

import com.medichub.dto.request.CreateCommentRequest;
import com.medichub.dto.request.CreateReplyRequest;
import com.medichub.dto.response.CommentResponse;
import com.medichub.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    /** Student posts a question (root comment) on a course/topic. Gated. */
    CommentResponse postQuestion(Long courseId, CreateCommentRequest request);

    /** Instructor replies to a question on their own course. */
    CommentResponse postReply(Long courseId, Long commentId, CreateReplyRequest request);

    /** Student view: question threads (roots + replies) for a course. Gated. */
    PagedResponse<CommentResponse> listCourseThreads(Long courseId, Pageable pageable);

    /** Instructor view: questions across their courses, optionally only unanswered ones. */
    PagedResponse<CommentResponse> listInstructorQuestions(boolean unansweredOnly, Pageable pageable);
}
