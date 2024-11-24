package com.rsmanager.dto.application;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationFlowRecordDTO {
    private Long flowId;
    private Long processId;
    private String action;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Instant createdAt;
    private String comments;
}
