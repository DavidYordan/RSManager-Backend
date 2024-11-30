package com.rsmanager.dto.tbuser;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteDailyMoneySumMiddleDTO {
    Long userId;
    Boolean fake;
    LocalDate date;
    Double sum;
}
