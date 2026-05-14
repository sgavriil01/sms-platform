package com.smsplatform.service;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class MessageProcessingEventDeserializer extends ObjectMapperDeserializer<MessageProcessingEvent> {

    public MessageProcessingEventDeserializer() {
        super(MessageProcessingEvent.class);
    }
}