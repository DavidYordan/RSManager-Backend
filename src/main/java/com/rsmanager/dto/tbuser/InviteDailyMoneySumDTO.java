package com.rsmanager.dto.tbuser;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteDailyMoneySumDTO {
    LocalDate date;
    Double sum;
}
