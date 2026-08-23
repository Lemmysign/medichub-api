package com.medichub.service;

import com.medichub.exception.SubscriptionRequiredException;
import com.medichub.model.enums.SubscriptionStatus;
import com.medichub.repository.SubscriptionRepository;
import com.medichub.service.impl.SubscriptionAccessServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionAccessServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionAccessServiceImpl service;

    @Test
    void hasActiveAccess_trueWhenActiveUnexpiredSubscriptionExists() {
        when(subscriptionRepository.existsByStudentIdAndStatusAndEndDateAfter(
                eq(1L), eq(SubscriptionStatus.ACTIVE), any(Instant.class))).thenReturn(true);

        assertThat(service.hasActiveAccess(1L)).isTrue();
    }

    @Test
    void hasActiveAccess_falseWhenNoActiveSubscription() {
        when(subscriptionRepository.existsByStudentIdAndStatusAndEndDateAfter(
                eq(2L), eq(SubscriptionStatus.ACTIVE), any(Instant.class))).thenReturn(false);

        assertThat(service.hasActiveAccess(2L)).isFalse();
    }

    @Test
    void requireActiveAccess_throwsWhenNoAccess() {
        when(subscriptionRepository.existsByStudentIdAndStatusAndEndDateAfter(
                eq(3L), eq(SubscriptionStatus.ACTIVE), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> service.requireActiveAccess(3L))
                .isInstanceOf(SubscriptionRequiredException.class);
    }

    @Test
    void requireActiveAccess_passesWhenAccessActive() {
        when(subscriptionRepository.existsByStudentIdAndStatusAndEndDateAfter(
                eq(4L), eq(SubscriptionStatus.ACTIVE), any(Instant.class))).thenReturn(true);

        assertThatCode(() -> service.requireActiveAccess(4L)).doesNotThrowAnyException();
    }
}
