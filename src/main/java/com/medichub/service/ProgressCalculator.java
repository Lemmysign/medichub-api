package com.medichub.service;

/** Single home for the course-completion formula: completed / total, rounded to a percent. */
public final class ProgressCalculator {

    private ProgressCalculator() {
    }

    public static int percent(long completedTopics, long totalTopics) {
        if (totalTopics <= 0) {
            return 0;
        }
        long clamped = Math.min(completedTopics, totalTopics);
        return (int) Math.round(clamped * 100.0 / totalTopics);
    }
}
