package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiktok_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokAccount {

    @Id
    @Column(name = "tiktok_account", length = 100)
    private String tiktokAccount;

    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;

    @Column(name = "unique_id", length = 50)
    private String uniqueId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar_larger")
    private String avatarLarger;

    @Column(name = "avatar_medium")
    private String avatarMedium;

    @Column(name = "avatar_thumb")
    private String avatarThumb;

    @Column(name = "signature")
    private String signature;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "sec_uid")
    private String secUid;

    @Column(name = "private_account")
    private Boolean privateAccount;

    @Column(name = "following_visibility")
    private Integer followingVisibility;

    @Column(name = "comment_setting")
    private Integer commentSetting;

    @Column(name = "duet_setting")
    private Integer duetSetting;

    @Column(name = "stitch_setting")
    private Integer stitchSetting;

    @Column(name = "download_setting")
    private Integer downloadSetting;

    @Column(name = "profile_embed_permission")
    private Integer profileEmbedPermission;

    @Column(name = "profile_tab_show_playlist_tab")
    private Boolean profileTabShowPlaylistTab;

    @Column(name = "commerce_user")
    private Boolean commerceUser;

    @Column(name = "tt_seller")
    private Boolean ttSeller;

    @Column(name = "relation")
    private Integer relation;

    @Column(name = "is_ad_virtual")
    private Boolean isAdVirtual;

    @Column(name = "is_embed_banned")
    private Boolean isEmbedBanned;

    @Column(name = "open_favorite")
    private Boolean openFavorite;

    @Column(name = "nick_name_modify_time")
    private Long nickNameModifyTime;

    @Column(name = "can_exp_playlist")
    private Boolean canExpPlaylist;

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
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "comments")
    private String comments;
}
