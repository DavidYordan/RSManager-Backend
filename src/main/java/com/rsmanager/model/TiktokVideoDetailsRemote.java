package com.rsmanager.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tiktok_video_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokVideoDetailsRemote {

    @Id
    @Column(name = "tiktok_video_id")
    private String tiktokVideoId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "AIGCDescription")
    private String AIGCDescription;

    @Column(name = "CategoryType")
    private Integer CategoryType;

    @Column(name = "backendSourceEventTracking")
    private String backendSourceEventTracking;

    @Column(name = "collected")
    private Boolean collected;

    @Column(name = "createTime")
    private Long createTime;

    @Column(name = "video_desc")
    private String videoDesc;

    @Column(name = "digged")
    private Boolean digged;

    @Column(name = "diversificationId")
    private Integer diversificationId;

    @Column(name = "duetDisplay")
    private Integer duetDisplay;

    @Column(name = "duetEnabled")
    private Boolean duetEnabled;

    @Column(name = "forFriend")
    private Boolean forFriend;

    @Column(name = "itemCommentStatus")
    private Integer itemCommentStatus;

    @Column(name = "officalItem")
    private Boolean officalItem;

    @Column(name = "originalItem")
    private Boolean originalItem;

    @Column(name = "privateItem")
    private Boolean privateItem;

    @Column(name = "secret")
    private Boolean secret;

    @Column(name = "shareEnabled")
    private Boolean shareEnabled;

    @Column(name = "stitchDisplay")
    private Integer stitchDisplay;

    @Column(name = "stitchEnabled")
    private Boolean stitchEnabled;

    @Column(name = "can_repost")
    private Boolean canRepost;

    @Column(name = "collectCount")
    private Integer collectCount;

    @Column(name = "commentCount")
    private Integer commentCount;

    @Column(name = "diggCount")
    private Integer diggCount;

    @Column(name = "playCount")
    private Integer playCount;

    @Column(name = "repostCount")
    private Integer repostCount;

    @Column(name = "shareCount")
    private Integer shareCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}   
