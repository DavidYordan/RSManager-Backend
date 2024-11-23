package com.rsmanager.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.rsmanager.dto.tiktok.TiktokAccountDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDTO {
    private Long userId;
    private String username;
    private String fullname;
    private String regionName;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer roleId;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long teacherId;
    private String teacherName;
    private String teacherFullname;
    private Boolean status;
    private Long platformId;
    private String inviterCode;
    private String invitationCode;
    private Integer invitationType;
    private Double platformTotalRevenue;
    private Double platformRevenueBalance;
    private Double platformTotalWithdrawal;
    private Double platformMoney;
    private Integer integralNum;
    private Long inviteCount;
    private Long platformInviteCount;
    private Double projectAmountSum;
    private String paidStr;
    private TiktokAccountDTO tiktokAccountDTO;
    private List<ProfitDTO> profits1;
    private List<ProfitDTO> profits2;
}
