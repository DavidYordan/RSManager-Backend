package com.rsmanager.dto.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetFileDTO {
    private Long processId;
    private Long paymentId;
    private String targetDir;
    private String fileName;
}
