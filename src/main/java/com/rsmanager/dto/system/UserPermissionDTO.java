package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionDTO {
    private Long userId;
    private String username;
    private String permissionName;
    private String classify;
    private Double rate;
    private Boolean isEnabled;
    private String description;

    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 100;
}
