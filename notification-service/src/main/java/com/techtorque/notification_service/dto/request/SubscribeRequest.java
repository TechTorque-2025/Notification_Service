package com.techtorque.notification_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Platform is required")
    @Pattern(regexp = "WEB|IOS|ANDROID", message = "Platform must be WEB, IOS, or ANDROID")
    private String platform;
}
