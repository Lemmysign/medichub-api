package com.medichub.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePlatformSettingsRequest(
        @NotNull Boolean videoDownloadEnabled
) {
}
