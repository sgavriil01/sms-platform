package com.smsplatform.controller;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.service.MessageService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Sends a new SMS message and returns the simulated delivery result.
     */
    @POST
    public Response sendMessage(@Valid SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);

        return Response
                .status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    /**
     * Returns all stored messages.
     */
    @GET
    public List<MessageResponse> listMessages() {
        return messageService.listMessages();
    }

    /**
     * Searches messages using optional filters.
     *
     * Example:
     * /api/messages/search?sourceNumber=+35799123456&status=DELIVERED
     */
    @GET
    @Path("/search")
    public List<MessageResponse> searchMessages(
            @QueryParam("sourceNumber") String sourceNumber,
            @QueryParam("destinationNumber") String destinationNumber,
            @QueryParam("status") MessageStatus status
    ) {
        return messageService.searchMessages(sourceNumber, destinationNumber, status);
    }
}