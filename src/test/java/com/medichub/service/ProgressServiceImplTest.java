package com.medichub.service;

import com.medichub.dto.request.MarkProgressRequest;
import com.medichub.dto.response.TopicProgressResponse;
import com.medichub.model.Course;
import com.medichub.model.Topic;
import com.medichub.model.TopicProgress;
import com.medichub.model.User;
import com.medichub.model.enums.Role;
import com.medichub.repository.TopicProgressRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.CustomUserDetails;
import com.medichub.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProgressServiceImplTest {

    private static final long STUDENT_ID = 7L;
    private static final int THRESHOLD = 30;

    @Mock private TopicProgressRepository topicProgressRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseService courseService;
    @Mock private SubscriptionAccessService subscriptionAccessService;
    @Mock private EnrollmentService enrollmentService;

    private ProgressServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProgressServiceImpl(topicProgressRepository, topicRepository, userRepository,
                courseService, subscriptionAccessService, enrollmentService, THRESHOLD);

        CustomUserDetails principal = new CustomUserDetails(STUDENT_ID, "s@x.com", null, Role.STUDENT, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Topic topic = new Topic();
        topic.setTitle("T");
        when(courseService.requirePublishedCourse(1L)).thenReturn(new Course());
        when(topicRepository.findByIdAndCourseId(2L, 1L)).thenReturn(Optional.of(topic));
        when(userRepository.getReferenceById(STUDENT_ID)).thenReturn(new User());
        when(topicProgressRepository.save(any(TopicProgress.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void belowThreshold_notCompleted() {
        when(topicProgressRepository.findByStudentIdAndTopicId(STUDENT_ID, 2L)).thenReturn(Optional.empty());

        TopicProgressResponse res = service.markTopicWatched(1L, 2L, new MarkProgressRequest(10));

        assertThat(res.completed()).isFalse();
        assertThat(res.secondsWatched()).isEqualTo(10);
        assertThat(res.completedAt()).isNull();
    }

    @Test
    void atThreshold_marksCompleted() {
        when(topicProgressRepository.findByStudentIdAndTopicId(STUDENT_ID, 2L)).thenReturn(Optional.empty());

        TopicProgressResponse res = service.markTopicWatched(1L, 2L, new MarkProgressRequest(THRESHOLD));

        assertThat(res.completed()).isTrue();
        assertThat(res.completedAt()).isNotNull();
    }

    @Test
    void secondsWatchedNeverRegresses() {
        TopicProgress existing = new TopicProgress();
        existing.setSecondsWatched(20);
        existing.setCompleted(false);
        when(topicProgressRepository.findByStudentIdAndTopicId(STUDENT_ID, 2L)).thenReturn(Optional.of(existing));

        TopicProgressResponse res = service.markTopicWatched(1L, 2L, new MarkProgressRequest(5));

        assertThat(res.secondsWatched()).isEqualTo(20);
        assertThat(res.completed()).isFalse();
    }

    @Test
    void accumulatingPastThreshold_completes() {
        TopicProgress existing = new TopicProgress();
        existing.setSecondsWatched(20);
        existing.setCompleted(false);
        when(topicProgressRepository.findByStudentIdAndTopicId(STUDENT_ID, 2L)).thenReturn(Optional.of(existing));

        TopicProgressResponse res = service.markTopicWatched(1L, 2L, new MarkProgressRequest(45));

        assertThat(res.secondsWatched()).isEqualTo(45);
        assertThat(res.completed()).isTrue();
    }
}
