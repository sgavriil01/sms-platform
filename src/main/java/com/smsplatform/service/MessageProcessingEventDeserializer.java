package com.smsplatform.service;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
/**
 * Deserializes Kafka message payloads into MessageProcessingEvent objects.
 */
public class MessageProcessingEventDeserializer extends ObjectMapperDeserializer<MessageProcessingEvent> {

    public MessageProcessingEventDeserializer() {
        super(MessageProcessingEvent.class);
    }
}