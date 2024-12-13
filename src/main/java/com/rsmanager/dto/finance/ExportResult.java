package com.rsmanager.dto.finance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResult {
    private String fileName;
    private byte[] data;
}
