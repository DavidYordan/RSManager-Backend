package com.rsmanager.dto.finance;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * External API Response Data Transfer Object
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformResponseDTO {
    private int code;
    private String msg;
}
