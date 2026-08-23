package com.medichub.service.impl;

import com.medichub.dto.response.VideoPlaybackResponse;
import com.medichub.dto.response.VideoUploadCredentialResponse;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Course;
import com.medichub.model.Topic;
import com.medichub.repository.TopicRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.BunnyService;
import com.medichub.service.BunnyVideoUpload;
import com.medichub.service.CourseService;
import com.medichub.service.PlatformSettingsService;
import com.medichub.service.SubscriptionAccessService;
import com.medichub.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
public class VideoServiceImpl implements VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoServiceImpl.class);
    private static final Duration PLAYBACK_TTL = Duration.ofHours(4);

    private final TopicRepository topicRepository;
    private final CourseService courseService;
    private final BunnyService bunnyService;
    private final SubscriptionAccessService subscriptionAccessService;
    private final PlatformSettingsService platformSettingsService;

    public VideoServiceImpl(TopicRepository topicRepository,
                            CourseService courseService,
                            BunnyService bunnyService,
                            SubscriptionAccessService subscriptionAccessService,
                            PlatformSettingsService platformSettingsService) {
        this.topicRepository = topicRepository;
        this.courseService = courseService;
        this.bunnyService = bunnyService;
        this.subscriptionAccessService = subscriptionAccessService;
        this.platformSettingsService = platformSettingsService;
    }

    @Override
    public VideoUploadCredentialResponse createVideoForTopic(Long courseId, Long topicId) {
        Course course = courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Topic topic = requireTopic(topicId, courseId);

        // Replacing an existing video: best-effort delete of the old Bunny object.
        if (topic.getBunnyVideoId() != null) {
            try {
                bunnyService.deleteVideo(topic.getBunnyVideoId());
            } catch (RuntimeException ex) {
                log.warn("Could not delete previous Bunny video {}: {}", topic.getBunnyVideoId(), ex.getMessage());
            }
        }

        BunnyVideoUpload upload = bunnyService.createVideo(course.getTitle() + " - " + topic.getTitle());
        topic.setBunnyVideoId(upload.guid());
        topic.setVideoDurationSeconds(null);

        return new VideoUploadCredentialResponse(
                topic.getId(),
                upload.guid(),
                upload.libraryId(),
                upload.tusEndpoint(),
                upload.expiresAtEpochSeconds(),
                upload.signature());
    }

    @Override
    public void deleteVideoForTopic(Long courseId, Long topicId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Topic topic = requireTopic(topicId, courseId);
        if (topic.getBunnyVideoId() != null) {
            bunnyService.deleteVideo(topic.getBunnyVideoId());
            topic.setBunnyVideoId(null);
            topic.setVideoDurationSeconds(null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoPlaybackResponse getPlayback(Long courseId, Long topicId) {
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());

        Topic topic = requireTopic(topicId, courseId);
        if (topic.getBunnyVideoId() == null) {
            throw new ResourceNotFoundException("This topic has no video yet");
        }

        String playbackUrl = bunnyService.signedPlaybackUrl(topic.getBunnyVideoId(), PLAYBACK_TTL);
        boolean downloadEnabled = platformSettingsService.isVideoDownloadEnabled();
        String downloadUrl = downloadEnabled
                ? bunnyService.signedDownloadUrl(topic.getBunnyVideoId(), PLAYBACK_TTL)
                : null;

        return new VideoPlaybackResponse(
                topic.getId(),
                topic.getBunnyVideoId(),
                playbackUrl,
                PLAYBACK_TTL.toSeconds(),
                downloadEnabled,
                downloadUrl);
    }

    private Topic requireTopic(Long topicId, Long courseId) {
        return topicRepository.findByIdAndCourseId(topicId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
    }
}
