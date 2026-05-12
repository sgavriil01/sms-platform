package com.smsplatform.mapper;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.model.Message;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageMapper {

    /**
     * Converts a Message entity into the DTO returned by the API.
     *
     * Keeping this mapping outside the controller/service avoids exposing
     * the database entity directly and keeps response formatting centralized.
     */
    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.id,
                message.sourceNumber,
                message.destinationNumber,
                message.content,
                message.status,
                message.errorMessage,
                message.createdAt,
                message.processedAt
        );
    }
}