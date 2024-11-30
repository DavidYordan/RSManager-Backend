package com.rsmanager.dto.tbuser;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbuserInfoDTO {
    private Long userId;
    private String username;
    private String phone;
    private String password;
    private String created_time;
    private String update_time;
    private Integer sys_phone;
    private Integer status;
    private String platform;
    private String invitation_code;
    private Integer invitation_type;
    private String inviter_code;
    private Integer inviter_type;
    private Long inviter_custom_id;
    private String inviter_url;
    private String clientid;
    private String on_line_time;
    private String recipient;
    private String bank_number;
    private String bank_name;
    private String bank_address;
    private String bank_code;
    private Double agent0_money;
    private Double agent1_money;
    private Double agent0_money_delete;
    private Double agent1_money_delete;
    private String first_name;
    private String last_name;
    private String email;
    private Integer area_code;
    private Boolean fake;
}
