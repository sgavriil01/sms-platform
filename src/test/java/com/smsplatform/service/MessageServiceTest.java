package com.smsplatform.service;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.mapper.MessageMapper;
import com.smsplatform.model.Message;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.repository.MessageRepository;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private MessageMapper messageMapper;
    private MessageDeliverySimulator deliverySimulator;
    private MessageProcessingProducer messageProcessingProducer;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        messageMapper = new MessageMapper();
        deliverySimulator = mock(MessageDeliverySimulator.class);
        messageProcessingProducer = mock(MessageProcessingProducer.class);

        messageService = new MessageService(
                messageRepository,
                messageMapper,
                deliverySimulator,
                messageProcessingProducer
        );
    }

    @Test
        void shouldCreatePendingMessageAndPublishProcessingEvent() {
        SendMessageRequest request = new SendMessageRequest();
        request.sourceNumber = "+35799123456";
        request.destinationNumber = "+35799876543";
        request.content = "Hello";

        doAnswer(invocation -> {
                Message message = invocation.getArgument(0);
                message.id = 1L;
                return null;
        }).when(messageRepository).persist(any(Message.class));

        MessageResponse response = messageService.sendMessage(request);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).persist(messageCaptor.capture());

        Message persistedMessage = messageCaptor.getValue();

        assertEquals(1L, persistedMessage.id);
        assertEquals("+35799123456", persistedMessage.sourceNumber);
        assertEquals("+35799876543", persistedMessage.destinationNumber);
        assertEquals("Hello", persistedMessage.content);
        assertEquals(MessageStatus.PENDING, persistedMessage.status);
        assertNull(persistedMessage.errorMessage);
        assertNotNull(persistedMessage.createdAt);
        assertNull(persistedMessage.processedAt);

        verify(messageProcessingProducer).publish(1L);

        assertEquals(1L, response.id);
        assertEquals(MessageStatus.PENDING, response.status);
        assertNull(response.errorMessage);
        assertNull(response.processedAt);
        }

    @Test
    void shouldProcessPendingMessageAsDelivered() {
        Message message = new Message(
                "+35799123456",
                "+35799876543",
                "Hello"
        );
        message.id = 1L;

        when(messageRepository.findById(1L)).thenReturn(message);
        when(deliverySimulator.simulate(message.destinationNumber))
                .thenReturn(new DeliveryResult(MessageStatus.DELIVERED, null));

        messageService.processMessage(1L);

        assertEquals(MessageStatus.DELIVERED, message.status);
        assertNull(message.errorMessage);
        assertNotNull(message.processedAt);
    }

    @Test
    void shouldProcessPendingMessageAsFailed() {
        Message message = new Message(
                "+35799123456",
                "+3579900000",
                "This should fail"
        );
        message.id = 1L;

        when(messageRepository.findById(1L)).thenReturn(message);
        when(deliverySimulator.simulate(message.destinationNumber))
                .thenReturn(new DeliveryResult(
                        MessageStatus.FAILED,
                        "Simulated delivery failure for destination number"
                ));

        messageService.processMessage(1L);

        assertEquals(MessageStatus.FAILED, message.status);
        assertEquals(
                "Simulated delivery failure for destination number",
                message.errorMessage
        );
        assertNotNull(message.processedAt);
    }

    @Test
    void shouldNotReprocessNonPendingMessage() {
        Message message = new Message(
                "+35799123456",
                "+35799876543",
                "Already delivered"
        );
        message.id = 1L;
        message.status = MessageStatus.DELIVERED;

        when(messageRepository.findById(1L)).thenReturn(message);

        messageService.processMessage(1L);

        verify(deliverySimulator, never()).simulate(anyString());
        assertEquals(MessageStatus.DELIVERED, message.status);
    }

    @Test
    void shouldThrowNotFoundWhenProcessingUnknownMessage() {
        when(messageRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> messageService.processMessage(99L));
    }

    @Test
    void shouldGetMessageById() {
        Message message = new Message(
                "+35799123456",
                "+35799876543",
                "Hello"
        );
        message.id = 1L;

        when(messageRepository.findById(1L)).thenReturn(message);

        MessageResponse response = messageService.getMessageById(1L);

        assertEquals(1L, response.id);
        assertEquals("+35799123456", response.sourceNumber);
        assertEquals(MessageStatus.PENDING, response.status);
    }

    @Test
    void shouldThrowNotFoundWhenMessageDoesNotExist() {
        when(messageRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> messageService.getMessageById(99L));
    }
}