package com.medichub.repository;

import com.medichub.model.Subscription;
import com.medichub.model.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** The access gate: an ACTIVE subscription that has not yet expired. */
    boolean existsByStudentIdAndStatusAndEndDateAfter(Long studentId, SubscriptionStatus status, Instant now);

    Optional<Subscription> findByStudentIdAndStatus(Long studentId, SubscriptionStatus status);

    Optional<Subscription> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    Optional<Subscription> findByPaystackSubscriptionCode(String paystackSubscriptionCode);

    long countByStatusAndEndDateAfter(SubscriptionStatus status, Instant now);

    @EntityGraph(attributePaths = {"student", "plan"})
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    @Modifying
    @Query("update Subscription s set s.studentName = :name where s.student.id = :studentId")
    int updateStudentName(@Param("studentId") Long studentId, @Param("name") String name);

    @Modifying
    @Query("update Subscription s set s.studentEmail = :email where s.student.id = :studentId")
    int updateStudentEmail(@Param("studentId") Long studentId, @Param("email") String email);
}
