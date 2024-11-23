package com.rsmanager.dto.api;

import lombok.*;

@Getter
@Setter
@Builder
public class ServiceResponseDTO {
    private boolean success;
    private String message;
}
