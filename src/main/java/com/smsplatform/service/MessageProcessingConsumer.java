package com.smsplatform.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumes message-processing events and updates stored messages with their final status.
 */
@ApplicationScoped
public class MessageProcessingConsumer {

    private final MessageService messageService;

    public MessageProcessingConsumer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Incoming("message-processing-in")
    public void consume(MessageProcessingEvent event) {
        messageService.processMessage(event.messageId);
    }
}