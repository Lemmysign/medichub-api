package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton settings row (id = 1), admin-editable (CLAUDE.md §4).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "platform_settings")
public class PlatformSettings extends BaseEntity {

    /** Global toggle for whether students may download videos. Off by default. */
    @Column(nullable = false)
    private boolean videoDownloadEnabled = false;
}
