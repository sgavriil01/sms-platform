package com.smsplatform.dto;

import com.smsplatform.model.MessageStatus;

import java.time.LocalDateTime;

public class MessageResponse {

    public Long id;
    public String sourceNumber;
    public String destinationNumber;
    public String content;
    public MessageStatus status;
    public String errorMessage;
    public LocalDateTime createdAt;
    public LocalDateTime processedAt;

    public MessageResponse(
            Long id,
            String sourceNumber,
            String destinationNumber,
            String content,
            MessageStatus status,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        this.id = id;
        this.sourceNumber = sourceNumber;
        this.destinationNumber = destinationNumber;
        this.content = content;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }
}