package com.smsplatform.service;

public class MessageProcessingEvent {

    public Long messageId;

    public MessageProcessingEvent() {
    }

    public MessageProcessingEvent(Long messageId) {
        this.messageId = messageId;
    }
}