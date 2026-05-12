package com.smsplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API error response returned when a request fails.
 */
public class ErrorResponse {

    public LocalDateTime timestamp;
    public int status;
    public String error;
    public String message;
    public List<String> details;

    public ErrorResponse(int status, String error, String message, List<String> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }
}