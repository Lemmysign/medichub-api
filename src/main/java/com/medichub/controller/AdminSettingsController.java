package com.medichub.controller;

import com.medichub.dto.request.UpdatePlatformSettingsRequest;
import com.medichub.dto.response.PlatformSettingsResponse;
import com.medichub.service.PlatformSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final PlatformSettingsService platformSettingsService;

    public AdminSettingsController(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    @GetMapping
    public PlatformSettingsResponse getSettings() {
        return platformSettingsService.getSettings();
    }

    @PutMapping("/video-download")
    public PlatformSettingsResponse updateVideoDownload(@Valid @RequestBody UpdatePlatformSettingsRequest request) {
        return platformSettingsService.updateVideoDownload(request.videoDownloadEnabled());
    }
}
