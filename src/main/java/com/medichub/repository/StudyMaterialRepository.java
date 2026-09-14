package com.medichub.repository;

import com.medichub.model.StudyMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    /** Creator view: admin sees all (ownerId null), instructor sees own; optional subject filter. */
    @Query("""
            select s from StudyMaterial s
            where (:ownerId is null or s.owner.id = :ownerId)
              and (:subjectId is null or s.subject.id = :subjectId)
            order by s.createdAt desc
            """)
    Page<StudyMaterial> findManageable(@Param("ownerId") Long ownerId,
                                       @Param("subjectId") Long subjectId,
                                       Pageable pageable);

    /** Student view: published only, optional subject filter. */
    @Query("""
            select s from StudyMaterial s
            where s.published = true
              and (:subjectId is null or s.subject.id = :subjectId)
            order by s.createdAt desc
            """)
    Page<StudyMaterial> findPublished(@Param("subjectId") Long subjectId, Pageable pageable);
}
