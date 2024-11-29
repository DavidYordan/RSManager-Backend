package com.rsmanager.dto.user;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

import com.rsmanager.dto.tbuser.InviteDailyMoneySumDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUsersResponseDTO {
    private Long userId;
    private String username;
    private String fullname;
    private Integer roleId;
    private String roleName;
    private String regionName;
    private String currencyName;
    private Integer inviteCount;
    private Instant createdAt;
    private Boolean status;
    private String projectCurrencyName;
    private String projectCurrencyCode;
    private Double projectAmount;
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
    private Integer invitationType;
    private Double moneySum;
    private Double money;
    private Double cashOut;
    private Double userMoney;
    private Integer userIntegral;
    private Integer platformInviteCount;
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
    @Builder.Default
    private List<InviteDailyMoneySumDTO> inviteDailyMoneySumDTOs = new ArrayList<>();
    @Builder.Default
    private List<RolePermissionRelationshipDTO> rolePermissionRelationshipDTOs = new ArrayList<>();
    @Builder.Default
    private List<ApplicationPaymentRecordDTO> applicationPaymentRecordDTOs = new ArrayList<>();
    @Builder.Default
    private List<ProfitDTO> profits1 = new ArrayList<>();
    @Builder.Default
    private List<ProfitDTO> profits2 = new ArrayList<>();

    public SearchUsersResponseDTO(
        Long userId,
        String username,
        String fullname,
        Integer roleId,
        String roleName,
        String regionName,
        String currencyName,
        Instant createdAt,
        Boolean status,
        String projectCurrencyName,
        String projectCurrencyCode,
        Double projectAmount,
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
        Integer invitationType,
        Double moneySum,
        Double money,
        Double cashOut,
        Double userMoney,
        Integer userIntegral,
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
        String comments
    ) {
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.roleId = roleId;
        this.roleName = roleName;
        this.regionName = regionName;
        this.currencyName = currencyName;
        this.createdAt = createdAt;
        this.status = status;
        this.projectCurrencyName = projectCurrencyName;
        this.projectCurrencyCode = projectCurrencyCode;
        this.projectAmount = projectAmount;
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
        this.invitationType = invitationType;
        this.moneySum = moneySum;
        this.money = money;
        this.cashOut = cashOut;
        this.userMoney = userMoney;
        this.userIntegral = userIntegral;
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
    }
}
