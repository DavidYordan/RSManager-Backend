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
public class TiktokUserDetailsRemote {
    // tiktok_id VARCHAR(50) PRIMARY KEY, -- TikTok平台的用户ID
    // tiktok_account VARCHAR(100), -- TikTok账号
    // unique_id VARCHAR(50), -- TikTok平台的唯一用户ID
    // nickname VARCHAR(100), -- 用户昵称
    // avatar_larger TEXT, -- 大图头像URL
    // avatar_medium TEXT, -- 中图头像URL
    // avatar_thumb TEXT, -- 小图头像URL
    // signature TEXT, -- 用户签名
    // verified BOOLEAN, -- 是否认证
    // sec_uid VARCHAR(255), -- 安全UID
    // private_account BOOLEAN, -- 是否为私密账户
    // following_visibility INT, -- 关注可见性设置
    // comment_setting INT, -- 评论设置
    // duet_setting INT, -- 合拍设置
    // stitch_setting INT, -- 拼接设置
    // download_setting INT, -- 下载设置
    // profile_embed_permission INT, -- 个人资料嵌入权限
    // profile_tab_show_playlist_tab BOOLEAN, -- 是否展示播放列表标签
    // commerce_user BOOLEAN, -- 是否为商业用户
    // tt_seller BOOLEAN, -- 是否为TikTok卖家
    // relation INT, -- 用户关系
    // is_ad_virtual BOOLEAN, -- 是否为虚拟广告用户
    // is_embed_banned BOOLEAN, -- 是否禁用嵌入
    // open_favorite BOOLEAN, -- 是否开放收藏
    // nick_name_modify_time BIGINT, -- 昵称修改时间戳
    // can_exp_playlist BOOLEAN, -- 是否可以播放列表
    // secret Boolean, -- 是否为私密账户
    // ftc Boolean, -- 是否为FTC
    // link TEXT, -- 用户链接
    // risk BIGINT, -- 风险等级

    // -- 统计信息字段
    // digg_count INT, -- 点赞数
    // follower_count INT, -- 粉丝数
    // following_count INT, -- 关注数
    // friend_count INT, -- 好友数
    // heart_count INT, -- 获赞总数
    // video_count INT, -- 视频总数

    // created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 记录创建时间
    // updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 记录更新时间
    // comments VARCHAR(64) -- 备注

    @Id
    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;

    @Column(name = "tiktok_account", length = 100)
    private String tiktokAccount;

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

    @Column(name = "secret")
    private Boolean secret;

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
