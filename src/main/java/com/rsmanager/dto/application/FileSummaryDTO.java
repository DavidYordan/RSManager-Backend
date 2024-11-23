package com.rsmanager.dto.application;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSummaryDTO {
    private int fileCount;
    private List<String> filePaths;
}
