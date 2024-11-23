package com.rsmanager.dto.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentActionDTO {
    private Long processId;
    private Long paymentId;
    private String comments;
}
