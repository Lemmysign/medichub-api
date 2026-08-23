package com.medichub.repository;

import com.medichub.model.Payment;
import com.medichub.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaystackReference(String paystackReference);

    /** Idempotency guard: has this reference already been recorded with the given status? */
    long countByPaystackReferenceAndStatus(String paystackReference, PaymentStatus status);

    /** Total successful revenue (kobo) in [from, to). Aggregated in the DB. */
    @Query("""
            select coalesce(sum(p.amountKobo), 0) from Payment p
            where p.status = com.medichub.model.enums.PaymentStatus.SUCCESS
              and p.paidAt >= :from and p.paidAt < :to
            """)
    long sumSuccessfulRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Successful revenue bucketed by day/week/month via Postgres date_trunc.
     * Returns rows of [bucketStart (timestamptz), totalKobo]. Aggregated in the DB.
     */
    @Query(value = """
            select date_trunc(:granularity, paid_at) as bucket, coalesce(sum(amount_kobo), 0) as total
            from payments
            where status = 'SUCCESS' and paid_at >= :from and paid_at < :to
            group by bucket
            order by bucket
            """, nativeQuery = true)
    List<Object[]> revenueSeries(@Param("granularity") String granularity,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);

    @Modifying
    @Query("update Payment p set p.studentName = :name where p.student.id = :studentId")
    int updateStudentName(@Param("studentId") Long studentId, @Param("name") String name);

    @Modifying
    @Query("update Payment p set p.studentEmail = :email where p.student.id = :studentId")
    int updateStudentEmail(@Param("studentId") Long studentId, @Param("email") String email);
}
