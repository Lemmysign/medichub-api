package com.medichub.repository;

import com.medichub.model.User;
import com.medichub.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Lookup by the unique (indexed) email column. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    /**
     * Admin account search: optional role filter + optional name/email query.
     * {@code q} is passed as "" (never null) to keep Postgres from typing a null
     * bind as bytea inside lower(); an empty q disables the text filter.
     */
    @Query("""
            select u from User u
            where (:role is null or u.role = :role)
              and (:enabled is null or u.enabled = :enabled)
              and (:q = '' or lower(u.fullName) like lower(concat('%', :q, '%'))
                   or lower(u.email) like lower(concat('%', :q, '%')))
            """)
    Page<User> searchUsers(@Param("role") Role role, @Param("enabled") Boolean enabled,
                           @Param("q") String q, Pageable pageable);

    @Query("select u.id from User u where u.enabled = false")
    List<Long> findDisabledUserIds();
}
