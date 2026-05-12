package com.smsplatform.controller;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.service.MessageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class MessageControllerTest {

    @InjectMock
    MessageService messageService;

    @Test
    void shouldSendMessageAndReturnCreated() {
        MessageResponse response = new MessageResponse(
                1L,
                "+35799123456",
                "+35799876543",
                "Hello",
                MessageStatus.DELIVERED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(messageService.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(response);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "sourceNumber": "+35799123456",
                          "destinationNumber": "+35799876543",
                          "content": "Hello"
                        }
                        """)
                .when()
                .post("/api/messages")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("sourceNumber", is("+35799123456"))
                .body("destinationNumber", is("+35799876543"))
                .body("content", is("Hello"))
                .body("status", is("DELIVERED"))
                .body("errorMessage", nullValue());
    }

    @Test
    void shouldReturnBadRequestForInvalidMessage() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "sourceNumber": "",
                          "destinationNumber": "abc",
                          "content": ""
                        }
                        """)
                .when()
                .post("/api/messages")
                .then()
                .statusCode(400)
                .body("error", is("Validation failed"))
                .body("message", is("The request contains invalid fields"))
                .body("details", not(empty()));
    }

    @Test
    void shouldListMessages() {
        MessageResponse response = new MessageResponse(
                1L,
                "+35799123456",
                "+35799876543",
                "Hello",
                MessageStatus.DELIVERED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(messageService.listMessages())
                .thenReturn(List.of(response));

        given()
                .when()
                .get("/api/messages")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].status", is("DELIVERED"));
    }

    @Test
    void shouldSearchMessagesByStatus() {
        MessageResponse response = new MessageResponse(
                1L,
                "+35799123456",
                "+35799876543",
                "Hello",
                MessageStatus.DELIVERED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(messageService.searchMessages(null, null, MessageStatus.DELIVERED))
                .thenReturn(List.of(response));

        given()
                .queryParam("status", "DELIVERED")
                .when()
                .get("/api/messages/search")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("DELIVERED"));
    }
}