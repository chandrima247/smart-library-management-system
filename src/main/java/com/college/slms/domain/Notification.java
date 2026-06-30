package com.college.slms.domain;

import com.college.slms.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A message delivered to a user's in-app inbox for circulation lifecycle events
 * (request decisions, issue, return, due reminders, fines).
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "ix_notif_recipient_read", columnList = "recipient_id,read_flag")
})
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 600)
    private String message;

    @Column(name = "read_flag", nullable = false)
    private boolean read = false;

    /** Optional in-app link the notification points to (e.g. /student/history). */
    @Column(name = "link_url", length = 255)
    private String linkUrl;

    protected Notification() {
        // for JPA
    }

    public Notification(User recipient, NotificationType type, String title, String message, String linkUrl) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.linkUrl = linkUrl;
    }

    public void markRead() {
        this.read = true;
    }

    // --- getters ---

    public User getRecipient() {
        return recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public String getLinkUrl() {
        return linkUrl;
    }
}
