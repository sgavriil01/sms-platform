package com.smsplatform.service;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.mapper.MessageMapper;
import com.smsplatform.model.Message;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.repository.MessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageDeliverySimulator deliverySimulator;
    private final MessageProcessingProducer messageProcessingProducer;

    public MessageService(MessageRepository messageRepository,
                          MessageMapper messageMapper,
                          MessageDeliverySimulator deliverySimulator,
                          MessageProcessingProducer messageProcessingProducer) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.deliverySimulator = deliverySimulator;
        this.messageProcessingProducer = messageProcessingProducer;
    }

    /**
     * Creates a message, stores it as PENDING, and publishes it for async processing.
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        Message message = new Message(
                request.sourceNumber,
                request.destinationNumber,
                request.content
        );

        messageRepository.persist(message);
        messageProcessingProducer.publish(message.id);

        return messageMapper.toResponse(message);
    }

    /**
     * Processes a stored message and updates it with the final simulated delivery result.
     */
    @Transactional
    public void processMessage(Long messageId) {
        Message message = messageRepository.findById(messageId);

        if (message == null) {
            throw new NotFoundException("Message not found with id: " + messageId);
        }

        if (message.status != MessageStatus.PENDING) {
            return;
        }

        DeliveryResult deliveryResult = deliverySimulator.simulate(message.destinationNumber);

        message.status = deliveryResult.getStatus();
        message.errorMessage = deliveryResult.getErrorMessage();
        message.processedAt = LocalDateTime.now();
    }

    public MessageResponse getMessageById(Long id) {
        Message message = messageRepository.findById(id);

        if (message == null) {
            throw new NotFoundException("Message not found with id: " + id);
        }

        return messageMapper.toResponse(message);
    }

    public List<MessageResponse> listMessages() {
        return messageRepository.listAll()
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public List<MessageResponse> searchMessages(String sourceNumber,
                                                String destinationNumber,
                                                MessageStatus status) {
        return messageRepository.search(sourceNumber, destinationNumber, status)
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }
}