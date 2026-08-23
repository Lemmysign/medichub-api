package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Downloadable PDF/DOC stored on Cloudflare R2 (CLAUDE.md §4, §6).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "course_materials", indexes = {
        @Index(name = "idx_materials_course", columnList = "course_id"),
        @Index(name = "idx_materials_topic", columnList = "topic_id")
})
public class CourseMaterial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Optional: a material may be scoped to a specific topic. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private String fileName;

    private String contentType;

    /** Object key within the R2 bucket. */
    @Column(nullable = false)
    private String r2Key;

    private String url;

    private Long sizeBytes;
}
