package com.smsplatform.service;

import com.smsplatform.model.MessageStatus;

/**
 * Represents the result returned by the delivery simulator.
 */
public class DeliveryResult {

    private final MessageStatus status;
    private final String errorMessage;

    public DeliveryResult(MessageStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}