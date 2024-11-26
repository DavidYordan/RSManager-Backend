package com.rsmanager.dto.user;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokAccountDTO {
    private String tiktokAccount;
    private String tiktokId;
    private String uniqueId;
    private Integer diggCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer friendCount;
    private Integer heartCount;
    private Integer videoCount;
    private Instant updatedAt;
}