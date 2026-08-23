package com.medichub.service;

import java.time.Duration;

public interface BunnyService {

    /** Create a video object in the library and return a browser-direct upload credential. */
    BunnyVideoUpload createVideo(String title);

    void deleteVideo(String guid);

    /** Mint a short-lived token-authenticated HLS playback URL for the given video GUID. */
    String signedPlaybackUrl(String guid, Duration ttl);

    /** Mint a short-lived token-authenticated MP4 download URL (only offered when the admin enables downloads). */
    String signedDownloadUrl(String guid, Duration ttl);
}
