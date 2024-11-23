package com.rsmanager.dto.tbuser;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbuserFindIdByPhoneResponseDTO {
    private Long platformId;
    private String platformAccount;
}
