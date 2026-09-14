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
 * A standalone study document (PDF or Word) an instructor uploads for students to read in-browser.
 * Not tied to a course. The file lives in R2 (gated); only metadata is stored here. Optionally
 * tagged with a {@link Subject}. Visible to students once {@code published}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "study_materials", indexes = {
        @Index(name = "idx_study_materials_owner", columnList = "owner_id"),
        @Index(name = "idx_study_materials_subject", columnList = "subject_id")
})
public class StudyMaterial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Optional taxonomy tag. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String fileName;

    private String contentType;

    /** Storage key / provider public id (R2 key or Cloudinary public_id) used for deletion. */
    @Column(nullable = false)
    private String r2Key;

    /** Delivery URL for the stored file (Cloudinary secure_url). */
    @Column(length = 1000)
    private String url;

    private Long sizeBytes;

    @Column(nullable = false)
    private boolean published = false;
}
