package com.smsplatform.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "source_number", nullable = false, length = 20)
    public String sourceNumber;

    @Column(name = "destination_number", nullable = false, length = 20)
    public String destinationNumber;

    @Column(nullable = false, length = 160)
    public String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public MessageStatus status;

    @Column(name = "error_message")
    public String errorMessage;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "processed_at")
    public LocalDateTime processedAt;

    public Message() {
    }

    public Message(String sourceNumber, String destinationNumber, String content) {
        this.sourceNumber = sourceNumber;
        this.destinationNumber = destinationNumber;
        this.content = content;
        this.status = MessageStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}