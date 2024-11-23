package com.rsmanager.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindUserDTO {
    Long userId;
    String username;
    String fullname;
}
