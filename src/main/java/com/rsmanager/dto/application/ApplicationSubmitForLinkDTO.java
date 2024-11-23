package com.rsmanager.dto.application;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSubmitForLinkDTO {
    private Long processId;
    private String username;
    private Long platformId;
    private String tiktokAccount;
    private String comments;
}

    