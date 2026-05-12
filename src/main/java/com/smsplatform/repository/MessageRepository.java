package com.smsplatform.repository;

import com.smsplatform.model.Message;
import com.smsplatform.model.MessageStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {

    /**
     * Searches messages using optional filters.
     *
     * If a filter is null or blank, it is ignored.
     * This allows the same method to support:
     * - list all messages
     * - search by source number
     * - search by destination number
     * - search by status
     * - any combination of the above
     */
    public List<Message> search(String sourceNumber,
                                String destinationNumber,
                                MessageStatus status) {

        StringBuilder query = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (sourceNumber != null && !sourceNumber.isBlank()) {
            query.append(" and sourceNumber = :sourceNumber");
            params.put("sourceNumber", sourceNumber);
        }

        if (destinationNumber != null && !destinationNumber.isBlank()) {
            query.append(" and destinationNumber = :destinationNumber");
            params.put("destinationNumber", destinationNumber);
        }

        if (status != null) {
            query.append(" and status = :status");
            params.put("status", status);
        }

        return list(query.toString(), params);
    }
}