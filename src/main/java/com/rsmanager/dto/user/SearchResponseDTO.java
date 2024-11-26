package com.rsmanager.dto.user;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.rsmanager.dto.tbuser.InviteDailyMoneySumDTO;

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
    private String currencyName;
    private Long inviteCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean status;
    private CreaterDTO createrDTO;
    private InviterDTO inviterDTO;
    private ManagerDTO managerDTO;
    private TeacherDTO teacherDTO;
    private TbUserDTO tbUserDTO;
    private TiktokAccountDTO tiktokAccountDTO;
    private List<RolePermissionRelationshipDTO> rolePermissionRelationshipDTOs;
    private List<ApplicationPaymentRecordDTO> applicationPaymentRecordDTOs;
    private List<ProfitDTO> profits1;
    private List<ProfitDTO> profits2;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreaterDTO {
        private Long userId;
        private String username;
        private String fullname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InviterDTO {
        private Long userId;
        private String username;
        private String fullname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDTO {
        private Long userId;
        private String username;
        private String fullname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherDTO {
        private Long userId;
        private String username;
        private String fullname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TbUserDTO {
        private Long userId;
        private String inviterCode;
        private String invitationCode;
        private Integer invitationType;
        private Double moneySum;
        private Double money;
        private Double cashOut;
        private Double userMoney;
        private Integer userIntegral;
        private Long inviteCount;
        private List<InviteDailyMoneySumDTO> inviteDailyMoneySumDTOs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InviteeDTO {
        private Long userId;
        private String username;
        private String fullname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationPaymentRecordDTO {
        private String regionName;
        private String currencyName;
        private String currencyCode;
        private String projectName;
        private Double projectAmount;
        private String paymentMethod;
        private Double paymentAmount;
        private Double fee;
        private Double actual;
        private LocalDate paymentDate;
        private Double rate;
    }
}
