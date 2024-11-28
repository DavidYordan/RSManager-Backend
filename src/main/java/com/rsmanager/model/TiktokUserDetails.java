package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "tiktok_user_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokUserDetails {

    @Id
    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;

    @Column(name = "tiktok_account", length = 100)
    private String tiktokAccount;

    @OneToOne(mappedBy = "tiktokUserDetails", fetch = FetchType.LAZY)
    private TiktokRelationship tiktokRelationship;

    @Column(name = "unique_id", length = 50)
    private String uniqueId;

    @Column(name = "nickname")
    private String nickname;

    @Transient
    @Column(name = "avatar_larger")
    private String avatarLarger;

    @Transient
    @Column(name = "avatar_medium")
    private String avatarMedium;

    @Transient
    @Column(name = "avatar_thumb")
    private String avatarThumb;

    @Transient
    @Column(name = "signature")
    private String signature;

    @Transient
    @Column(name = "verified")
    private Boolean verified;

    @Transient
    @Column(name = "sec_uid")
    private String secUid;

    @Transient
    @Column(name = "private_account")
    private Boolean privateAccount;

    @Transient
    @Column(name = "following_visibility")
    private Integer followingVisibility;

    @Transient
    @Column(name = "comment_setting")
    private Integer commentSetting;

    @Column(name = "duet_setting")
    private Integer duetSetting;

    @Transient
    @Column(name = "stitch_setting")
    private Integer stitchSetting;

    @Transient
    @Column(name = "download_setting")
    private Integer downloadSetting;

    @Transient
    @Column(name = "profile_embed_permission")
    private Integer profileEmbedPermission;

    @Transient
    @Column(name = "profile_tab_show_playlist_tab")
    private Boolean profileTabShowPlaylistTab;

    @Transient
    @Column(name = "commerce_user")
    private Boolean commerceUser;

    @Transient
    @Column(name = "tt_seller")
    private Boolean ttSeller;

    @Transient
    @Column(name = "relation")
    private Integer relation;

    @Transient
    @Column(name = "is_ad_virtual")
    private Boolean isAdVirtual;

    @Transient
    @Column(name = "is_embed_banned")
    private Boolean isEmbedBanned;

    @Transient
    @Column(name = "open_favorite")
    private Boolean openFavorite;

    @Transient
    @Column(name = "nick_name_modify_time")
    private Long nickNameModifyTime;

    @Transient
    @Column(name = "can_exp_playlist")
    private Boolean canExpPlaylist;

    @Transient
    @Column(name = "secret")
    private Boolean secret;

    @Transient
    @Column(name = "ftc")
    private Boolean ftc;

    @Column(name = "link")
    private String link;

    @Column(name = "risk")
    private Long risk;

    @Column(name = "digg_count")
    private Integer diggCount;

    @Column(name = "follower_count")
    private Integer followerCount;

    @Column(name = "following_count")
    private Integer followingCount;

    @Column(name = "friend_count")
    private Integer friendCount;

    @Column(name = "heart_count")
    private Integer heartCount;

    @Column(name = "video_count")
    private Integer videoCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "comments")
    private String comments;
}
