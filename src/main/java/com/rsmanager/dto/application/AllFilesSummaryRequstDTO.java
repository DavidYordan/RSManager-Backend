package com.rsmanager.dto.application;

import java.util.List;

import lombok.*;

@Setter
@Getter
public class AllFilesSummaryRequstDTO {
    Long processId;
    List<Long> paymentIds;
}
