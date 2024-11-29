package com.rsmanager.dto.user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomInviteRelationshipDTO {
    private Long inviterId;
    private Long inviteeId;
    private Integer level;
}
