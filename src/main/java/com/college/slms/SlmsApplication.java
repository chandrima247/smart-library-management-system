package com.college.slms;

import com.college.slms.config.SlmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Smart Library Management System.
 *
 * <p>The application is a server-rendered (Thymeleaf) ERP secured with Spring
 * Security and backed by Spring Data JPA. It ships with an embedded H2 database
 * for zero-setup development and a {@code mysql} profile for production.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(SlmsProperties.class)
@EnableScheduling
public class SlmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlmsApplication.class, args);
    }
}
