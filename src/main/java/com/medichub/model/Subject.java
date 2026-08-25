package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A taxonomy subject (e.g. Pathology, Surgery) used to categorise MCQ practice exams
 * and Recall papers. Admin-editable (create/rename/reorder/deactivate) so the exam
 * scope can evolve without a code change — see {@link com.medichub.service.SubjectService}.
 * Seeded on first startup with Dr Sam's core list (CLAUDE.md §4).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subjects")
public class Subject extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    /** URL/filter-friendly identifier derived from the name, kept unique. */
    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    /** Ascending display order in pickers and filter lists. */
    @Column(nullable = false)
    private int orderIndex;

    /** Inactive subjects are hidden from authoring/filtering but retained for existing content. */
    @Column(nullable = false)
    private boolean active = true;
}
