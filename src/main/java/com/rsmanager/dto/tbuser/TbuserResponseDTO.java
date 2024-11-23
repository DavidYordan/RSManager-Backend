package com.rsmanager.dto.tbuser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TbuserResponseDTO {
    private Long userId;
    private String username;
    private String phone;
    private String created_time;
    private Integer status;
    private String invitation_code;
    private Integer invitation_type;
    private String first_name;
    private String last_name;
    private String email;
    private Integer area_code;
    private Integer fake;
}
