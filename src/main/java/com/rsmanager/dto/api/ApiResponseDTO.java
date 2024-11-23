package com.rsmanager.dto.api;

import lombok.*;

/**
 * API Response Data Transfer Object with Generic Type
 */
@Getter
@Setter
@Builder
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private PaginationDTO paginationDTO;

    // Constructors
    public ApiResponseDTO() {}

    public ApiResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponseDTO(boolean success, String message, T data, PaginationDTO paginationDTO) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.paginationDTO = paginationDTO;
    }
}
