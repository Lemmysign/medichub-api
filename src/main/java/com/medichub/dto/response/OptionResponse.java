package com.medichub.dto.response;

/** Instructor view of an option — includes the correct flag. */
public record OptionResponse(
        Long id,
        String text,
        boolean correct,
        int orderIndex
) {
}
