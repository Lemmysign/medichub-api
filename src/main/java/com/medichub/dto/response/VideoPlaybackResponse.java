package com.medichub.dto.response;

/**
 * Student playback response. {@code downloadUrl} is populated only when the admin
 * has enabled video downloads (PlatformSettings.videoDownloadEnabled).
 */
public record VideoPlaybackResponse(
        Long topicId,
        String bunnyVideoId,
        String playbackUrl,
        long expiresInSeconds,
        boolean downloadEnabled,
        String downloadUrl
) {
}
