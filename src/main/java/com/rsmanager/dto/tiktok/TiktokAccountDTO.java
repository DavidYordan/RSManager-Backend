package com.rsmanager.dto.tiktok;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokAccountDTO {
    private String tiktokAccount;
    private String tiktokId;
    private Integer diggCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer friendCount;
    private Integer heartCount;
    private Integer videoCount;
    private LocalDateTime accountCreatedAt;
}

