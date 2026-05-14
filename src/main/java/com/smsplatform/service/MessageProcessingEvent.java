package com.smsplatform.service;
/**
 * Event published to Kafka when a stored message is ready for async processing.
 */
public class MessageProcessingEvent {

    public Long messageId;

    public MessageProcessingEvent() {
    }

    public MessageProcessingEvent(Long messageId) {
        this.messageId = messageId;
    }
}