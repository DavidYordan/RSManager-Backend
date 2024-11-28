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
public class TiktokVideoDetails {

    @Id
    @Column(name = "tiktok_video_id")
    private String tiktokVideoId;

    @Column(name = "author_id")
    private String authorId;

    @Transient
    @Column(name = "AIGCDescription")
    private String AIGCDescription;

    @Column(name = "categoryType")
    private Integer categoryType;

    @Transient
    @Column(name = "backendSourceEventTracking")
    private String backendSourceEventTracking;

    @Transient
    @Column(name = "collected")
    private Boolean collected;

    @Column(name = "createTime")
    private Long createTime;

    @Column(name = "video_desc")
    private String videoDesc;

    @Transient
    @Column(name = "digged")
    private Boolean digged;

    @Transient
    @Column(name = "diversificationId")
    private Integer diversificationId;

    @Transient
    @Column(name = "duetDisplay")
    private Integer duetDisplay;

    @Transient
    @Column(name = "duetEnabled")
    private Boolean duetEnabled;

    @Transient
    @Column(name = "forFriend")
    private Boolean forFriend;

    @Transient
    @Column(name = "itemCommentStatus")
    private Integer itemCommentStatus;

    @Transient
    @Column(name = "officalItem")
    private Boolean officalItem;

    @Transient
    @Column(name = "originalItem")
    private Boolean originalItem;

    @Transient
    @Column(name = "privateItem")
    private Boolean privateItem;

    @Transient
    @Column(name = "secret")
    private Boolean secret;

    @Transient
    @Column(name = "shareEnabled")
    private Boolean shareEnabled;

    @Transient
    @Column(name = "stitchDisplay")
    private Integer stitchDisplay;

    @Transient
    @Column(name = "stitchEnabled")
    private Boolean stitchEnabled;

    @Transient
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
