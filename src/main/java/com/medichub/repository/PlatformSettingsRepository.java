package com.medichub.repository;

import com.medichub.model.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Singleton settings row. Added here (beyond the Phase-2 auth repos) so startup
 * can seed the row per CLAUDE.md §4 / the Phase-2 task.
 */
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {
}
