package com.medichub.service;

import com.medichub.dto.response.VideoPlaybackResponse;
import com.medichub.dto.response.VideoUploadCredentialResponse;

public interface VideoService {

    /** Instructor: create (or replace) the Bunny video for a topic; returns an upload credential. */
    VideoUploadCredentialResponse createVideoForTopic(Long courseId, Long topicId);

    /** Instructor: remove a topic's video from Bunny and clear the stored GUID. */
    void deleteVideoForTopic(Long courseId, Long topicId);

    /** Student (gated): a short-lived signed playback URL, honouring the download toggle. */
    VideoPlaybackResponse getPlayback(Long courseId, Long topicId);
}
