package com.smsplatform.service;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.mapper.MessageMapper;
import com.smsplatform.model.Message;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private MessageMapper messageMapper;
    private MessageDeliverySimulator deliverySimulator;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        messageMapper = new MessageMapper();
        deliverySimulator = mock(MessageDeliverySimulator.class);

        messageService = new MessageService(
                messageRepository,
                messageMapper,
                deliverySimulator
        );
    }

    @Test
    void shouldSendDeliveredMessageAndPersistIt() {
        SendMessageRequest request = new SendMessageRequest();
        request.sourceNumber = "+35799123456";
        request.destinationNumber = "+35799876543";
        request.content = "Hello";

        when(deliverySimulator.simulate(request.destinationNumber))
                .thenReturn(new DeliveryResult(MessageStatus.DELIVERED, null));

        MessageResponse response = messageService.sendMessage(request);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).persist(messageCaptor.capture());

        Message persistedMessage = messageCaptor.getValue();

        assertEquals("+35799123456", persistedMessage.sourceNumber);
        assertEquals("+35799876543", persistedMessage.destinationNumber);
        assertEquals("Hello", persistedMessage.content);
        assertEquals(MessageStatus.DELIVERED, persistedMessage.status);
        assertNull(persistedMessage.errorMessage);
        assertNotNull(persistedMessage.createdAt);
        assertNotNull(persistedMessage.processedAt);

        assertEquals(MessageStatus.DELIVERED, response.status);
        assertNull(response.errorMessage);
    }

    @Test
    void shouldSendFailedMessageAndPersistFailureReason() {
        SendMessageRequest request = new SendMessageRequest();
        request.sourceNumber = "+35799123456";
        request.destinationNumber = "+3579900000";
        request.content = "This should fail";

        when(deliverySimulator.simulate(request.destinationNumber))
                .thenReturn(new DeliveryResult(
                        MessageStatus.FAILED,
                        "Simulated delivery failure for destination number"
                ));

        MessageResponse response = messageService.sendMessage(request);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).persist(messageCaptor.capture());

        Message persistedMessage = messageCaptor.getValue();

        assertEquals(MessageStatus.FAILED, persistedMessage.status);
        assertEquals(
                "Simulated delivery failure for destination number",
                persistedMessage.errorMessage
        );

        assertEquals(MessageStatus.FAILED, response.status);
        assertEquals(
                "Simulated delivery failure for destination number",
                response.errorMessage
        );
    }
}