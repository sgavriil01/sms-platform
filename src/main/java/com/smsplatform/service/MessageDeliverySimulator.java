package com.smsplatform.service;

import com.smsplatform.model.MessageStatus;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simulates SMS delivery without calling an external SMS provider.
 *
 * The rule is deterministic so tests can reliably predict the result:
 * destination numbers ending in "0000" fail, all others are delivered.
 */
@ApplicationScoped
public class MessageDeliverySimulator {

    public DeliveryResult simulate(String destinationNumber) {
        if (destinationNumber.endsWith("0000")) {
            return new DeliveryResult(
                    MessageStatus.FAILED,
                    "Simulated delivery failure for destination number"
            );
        }

        return new DeliveryResult(MessageStatus.DELIVERED, null);
    }
}