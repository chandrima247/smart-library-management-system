package com.college.slms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Shared identity, optimistic-locking and audit columns for every persistent
 * entity. Auditing is populated automatically by {@link AuditingEntityListener}
 * which is enabled via {@code @EnableJpaAuditing}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic lock guard against concurrent circulation updates. */
    @Version
    private Long version;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Convenience for views: the creation timestamp as a local date. */
    public LocalDate getCreatedDate() {
        return createdAt == null ? null : LocalDate.ofInstant(createdAt, ZoneId.systemDefault());
    }

    public boolean isNew() {
        return id == null;
    }
}
