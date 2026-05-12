package com.smsplatform.service;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.mapper.MessageMapper;
import com.smsplatform.model.Message;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.repository.MessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageDeliverySimulator deliverySimulator;

    public MessageService(MessageRepository messageRepository,
                          MessageMapper messageMapper,
                          MessageDeliverySimulator deliverySimulator) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.deliverySimulator = deliverySimulator;
    }

    /**
     * Creates, processes, stores, and returns the final message result.
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        Message message = new Message(
                request.sourceNumber,
                request.destinationNumber,
                request.content
        );

        DeliveryResult deliveryResult = deliverySimulator.simulate(request.destinationNumber);

        message.status = deliveryResult.getStatus();
        message.errorMessage = deliveryResult.getErrorMessage();
        message.processedAt = LocalDateTime.now();

        messageRepository.persist(message);

        return messageMapper.toResponse(message);
    }

    /**
     * Returns all stored messages.
     */
    public List<MessageResponse> listMessages() {
        return messageRepository.listAll()
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    /**
     * Searches stored messages using optional filters.
     */
    public List<MessageResponse> searchMessages(String sourceNumber,
                                                String destinationNumber,
                                                MessageStatus status) {
        return messageRepository.search(sourceNumber, destinationNumber, status)
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }
}