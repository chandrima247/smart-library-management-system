package com.college.slms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate}/{@code @LastModifiedDate} fields
 * on {@link com.college.slms.domain.BaseEntity} are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
