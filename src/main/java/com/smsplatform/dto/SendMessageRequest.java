package com.smsplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank(message = "Source number is required")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "Source number must be a valid phone number"
    )
    public String sourceNumber;

    @NotBlank(message = "Destination number is required")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "Destination number must be a valid phone number"
    )
    public String destinationNumber;

    @NotBlank(message = "Message content is required")
    @Size(max = 160, message = "Message content must not exceed 160 characters")
    public String content;
}