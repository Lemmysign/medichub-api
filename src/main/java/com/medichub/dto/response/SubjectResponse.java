package com.medichub.dto.response;

/** A taxonomy subject as exposed to admins (management) and to instructors/students (pickers/filters). */
public record SubjectResponse(
        Long id,
        String name,
        String slug,
        int orderIndex,
        boolean active
) {
}
