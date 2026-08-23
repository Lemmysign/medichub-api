package com.medichub.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressCalculatorTest {

    @Test
    void zeroTopicsIsZeroPercent() {
        assertThat(ProgressCalculator.percent(0, 0)).isZero();
        assertThat(ProgressCalculator.percent(5, 0)).isZero();
    }

    @Test
    void noneCompleteIsZero() {
        assertThat(ProgressCalculator.percent(0, 4)).isZero();
    }

    @Test
    void allCompleteIs100() {
        assertThat(ProgressCalculator.percent(4, 4)).isEqualTo(100);
    }

    @Test
    void partialRoundsToNearest() {
        assertThat(ProgressCalculator.percent(1, 3)).isEqualTo(33);
        assertThat(ProgressCalculator.percent(2, 3)).isEqualTo(67);
        assertThat(ProgressCalculator.percent(1, 8)).isEqualTo(13); // 12.5 -> 13
    }

    @Test
    void completedNeverExceedsTotal() {
        assertThat(ProgressCalculator.percent(10, 4)).isEqualTo(100);
    }
}
