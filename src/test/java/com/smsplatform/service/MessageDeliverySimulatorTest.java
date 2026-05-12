package com.smsplatform.service;

import com.smsplatform.model.MessageStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageDeliverySimulatorTest {

    private final MessageDeliverySimulator simulator = new MessageDeliverySimulator();

    @Test
    void shouldDeliverMessageWhenDestinationDoesNotEndWith0000() {
        DeliveryResult result = simulator.simulate("+35799123456");

        assertEquals(MessageStatus.DELIVERED, result.getStatus());
        assertNull(result.getErrorMessage());
    }

    @Test
    void shouldFailMessageWhenDestinationEndsWith0000() {
        DeliveryResult result = simulator.simulate("+3579900000");

        assertEquals(MessageStatus.FAILED, result.getStatus());
        assertEquals("Simulated delivery failure for destination number", result.getErrorMessage());
    }

    @Test
    void shouldDeliverWhenDestinationContainsZerosButDoesNotEndWith0000() {
        DeliveryResult result = simulator.simulate("+35799000123");

        assertEquals(MessageStatus.DELIVERED, result.getStatus());
        assertNull(result.getErrorMessage());
    }
}