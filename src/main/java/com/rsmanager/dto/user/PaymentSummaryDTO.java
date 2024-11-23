package com.rsmanager.dto.user;

import java.time.LocalDate;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryDTO {
    private LocalDate paymentDate;
    private Double paymentAmount;
    private Double profit;
}