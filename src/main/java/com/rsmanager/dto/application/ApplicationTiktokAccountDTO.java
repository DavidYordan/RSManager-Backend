package com.rsmanager.dto.application;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationTiktokAccountDTO {
    private Long processId;
    private String tiktokAccount;
    private LocalDateTime createdAt;
}
