package com.rsmanager.dto.traffic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchTrafficResponseDTO {
    private Long userId;
    private String username;
    private String fullname;
    private String regionName;
    private Integer roleId;
    private String roleName;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long teacherId;
    private String teacherName;
    private String teacherFullname;
    private Long platformId;
    private String inviterCode;
    private String invitationCode;
    @Builder.Default
    private Integer platformInviteCount = 0;
    private String tiktokAccount;
    private String tiktokId;
    private String uniqueId;
    private String nickname;
    private Integer diggCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer friendCount;
    private Integer heartCount;
    private Integer videoCount;
    private Instant updatedAt;
    private String comments;
    private String link;
    private Long risk;
    @Builder.Default
    private List<TiktokVideoDetailsDTO> tiktokVideoDetailDTOs = new ArrayList<>();

    public SearchTrafficResponseDTO(
        Long userId,
        String username,
        String fullname,
        String regionName,
        Integer roleId,
        String roleName,
        Long inviterId,
        String inviterName,
        String inviterFullname,
        Long managerId,
        String managerName,
        String managerFullname,
        Long teacherId,
        String teacherName,
        String teacherFullname,
        Long platformId,
        String inviterCode,
        String invitationCode,
        String tiktokAccount,
        String tiktokId,
        String uniqueId,
        String nickname,
        Integer diggCount,
        Integer followerCount,
        Integer followingCount,
        Integer friendCount,
        Integer heartCount,
        Integer videoCount,
        Instant updatedAt,
        String comments,
        String link,
        Long risk
    ) {
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.regionName = regionName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.inviterId = inviterId;
        this.inviterName = inviterName;
        this.inviterFullname = inviterFullname;
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerFullname = managerFullname;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.teacherFullname = teacherFullname;
        this.platformId = platformId;
        this.inviterCode = inviterCode;
        this.invitationCode = invitationCode;
        this.tiktokAccount = tiktokAccount;
        this.tiktokId = tiktokId;
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.diggCount = diggCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.friendCount = friendCount;
        this.heartCount = heartCount;
        this.videoCount = videoCount;
        this.updatedAt = updatedAt;
        this.comments = comments;
        this.link = link;
        this.risk = risk;
        // this.tiktokVideoDetailDTOs = new ArrayList<>();
    }
}
