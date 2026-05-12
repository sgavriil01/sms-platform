package com.smsplatform.controller;

import com.smsplatform.dto.MessageResponse;
import com.smsplatform.dto.SendMessageRequest;
import com.smsplatform.model.MessageStatus;
import com.smsplatform.service.MessageService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

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
    @Operation(summary = "Send a new SMS message")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Message created and processed"),
        @APIResponse(responseCode = "400", description = "Invalid message request")
})
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
    @Operation(summary = "List all stored messages")
    @APIResponse(responseCode = "200", description = "Messages returned successfully")
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
    @Operation(summary = "Search messages using optional filters")
    @APIResponse(responseCode = "200", description = "Messages returned successfully")
    public List<MessageResponse> searchMessages(
            @QueryParam("sourceNumber") String sourceNumber,
            @QueryParam("destinationNumber") String destinationNumber,
            @QueryParam("status") MessageStatus status
    ) {
        return messageService.searchMessages(sourceNumber, destinationNumber, status);
    }
}