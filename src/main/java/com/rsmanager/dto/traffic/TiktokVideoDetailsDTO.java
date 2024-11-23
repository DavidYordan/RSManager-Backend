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
    private String AIGCDescription;
    private Integer CategoryType;
    private String backendSourceEventTracking;
    private Boolean collected;
    private Long createTime;
    private String videoDesc;
    private Boolean digged;
    private Integer diversificationId;
    private Integer duetDisplay;
    private Boolean duetEnabled;
    private Boolean forFriend;
    private Integer itemCommentStatus;
    private Boolean officalItem;
    private Boolean originalItem;
    private Boolean privateItem;
    private Boolean secret;
    private Boolean shareEnabled;
    private Integer stitchDisplay;
    private Boolean stitchEnabled;
    private Boolean canRepost;
    private Integer collectCount;
    private Integer commentCount;
    private Integer diggCount;
    private Integer playCount;
    private Integer repostCount;
    private Integer shareCount;
    private Instant createdAt;
    private Instant updatedAt;
}   
