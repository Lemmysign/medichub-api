package com.medichub.service.impl;

import com.medichub.exception.SubscriptionRequiredException;
import com.medichub.model.enums.SubscriptionStatus;
import com.medichub.repository.SubscriptionRepository;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class SubscriptionAccessServiceImpl implements SubscriptionAccessService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionAccessServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean hasActiveAccess(Long studentId) {
        return subscriptionRepository.existsByStudentIdAndStatusAndEndDateAfter(
                studentId, SubscriptionStatus.ACTIVE, Instant.now());
    }

    @Override
    public void requireActiveAccess(Long studentId) {
        if (!hasActiveAccess(studentId)) {
            throw new SubscriptionRequiredException("An active subscription is required to access this content");
        }
    }
}
