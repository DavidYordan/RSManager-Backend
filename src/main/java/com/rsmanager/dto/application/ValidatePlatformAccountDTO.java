package com.rsmanager.dto.application;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatePlatformAccountDTO {
    private Long platformId;
    private String platformAccount;
    private String message;
}
