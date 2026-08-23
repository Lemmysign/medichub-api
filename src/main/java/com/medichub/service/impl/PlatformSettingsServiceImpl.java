package com.medichub.service.impl;

import com.medichub.dto.response.PlatformSettingsResponse;
import com.medichub.model.PlatformSettings;
import com.medichub.repository.PlatformSettingsRepository;
import com.medichub.service.PlatformSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    protected final PlatformSettingsRepository repository;

    public PlatformSettingsServiceImpl(PlatformSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isVideoDownloadEnabled() {
        return settings().isVideoDownloadEnabled();
    }

    @Override
    public PlatformSettingsResponse getSettings() {
        return new PlatformSettingsResponse(settings().isVideoDownloadEnabled());
    }

    @Override
    @Transactional
    public PlatformSettingsResponse updateVideoDownload(boolean enabled) {
        PlatformSettings settings = repository.findAll().stream().findFirst()
                .orElseGet(PlatformSettings::new);
        settings.setVideoDownloadEnabled(enabled);
        settings = repository.save(settings);
        return new PlatformSettingsResponse(settings.isVideoDownloadEnabled());
    }

    /**
     * The singleton settings row (seeded at startup). Returns a transient default if
     * somehow missing, so read paths never fail inside a read-only transaction.
     */
    protected PlatformSettings settings() {
        return repository.findAll().stream().findFirst().orElseGet(() -> {
            PlatformSettings defaults = new PlatformSettings();
            defaults.setVideoDownloadEnabled(false);
            return defaults;
        });
    }
}
