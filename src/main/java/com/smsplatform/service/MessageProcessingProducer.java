package com.smsplatform.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class MessageProcessingProducer {

    private final Emitter<MessageProcessingEvent> emitter;

    public MessageProcessingProducer(
            @Channel("message-processing") Emitter<MessageProcessingEvent> emitter
    ) {
        this.emitter = emitter;
    }

    public void publish(Long messageId) {
        emitter.send(new MessageProcessingEvent(messageId));
    }
}