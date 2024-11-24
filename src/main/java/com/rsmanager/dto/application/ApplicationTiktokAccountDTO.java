package com.rsmanager.dto.application;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationTiktokAccountDTO {
    private Long processId;
    private String tiktokAccount;
    private Instant createdAt;
}
