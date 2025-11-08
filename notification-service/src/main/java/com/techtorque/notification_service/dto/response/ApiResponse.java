package com.techtorque.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private String message;
    private Object data;
    
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .message(message)
                .build();
    }
    
    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .message(message)
                .data(data)
                .build();
    }
}
