package com.medichub.service;

import com.medichub.dto.response.PlatformSettingsResponse;

public interface PlatformSettingsService {

    /** Whether students may download videos (admin-controlled global toggle). */
    boolean isVideoDownloadEnabled();

    /** Current platform settings (admin view). */
    PlatformSettingsResponse getSettings();

    /** Update the global video-download toggle (admin). */
    PlatformSettingsResponse updateVideoDownload(boolean enabled);
}
