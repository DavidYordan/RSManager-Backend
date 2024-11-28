package com.rsmanager.dto.traffic;

import java.time.Instant;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokVideoDetailsDTO {
    private String tiktokVideoId;
    private String authorId;
    private String videoDesc;
    private Integer categoryType;
    private Integer collectCount;
    private Integer commentCount;
    private Integer diggCount;
    private Integer playCount;
    private Integer repostCount;
    private Integer shareCount;
    private Long createTime;
    private Instant updatedAt;
}   
