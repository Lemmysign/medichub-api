package com.medichub.dto.response;

/** Student view of an option — deliberately omits the correct flag. */
public record StudentOptionResponse(
        Long id,
        String text,
        int orderIndex
) {
}
