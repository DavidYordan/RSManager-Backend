package com.rsmanager.dto.user;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
public class BackendUserInfoResponseDTO {
    private Long userId;
    private String username;
    private String backendRole;
    private String fullname;
    private LocalDateTime createdAt;
    private String createdBy;
    private Double totalLearningCost;
    private Double totalRevenue;
    private Double revenueBalance;
    private Double totalWithdrawal;
    private Long inviterId;
    private String inviterName;
    private Long platformId;
    private Boolean status;
    private List<String> userPermissions;
    private List<String> tiktokAccounts;
}
