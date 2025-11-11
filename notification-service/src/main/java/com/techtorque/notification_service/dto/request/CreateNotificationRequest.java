package com.techtorque.notification_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Type is required")
    private String type;  // INFO, WARNING, ERROR, SUCCESS

    @NotBlank(message = "Message is required")
    private String message;

    private String details;

    private String relatedEntityId;

    private String relatedEntityType;
}
