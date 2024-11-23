package com.rsmanager.dto.tbuser;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TbuserUserIdsRequestDTO {
    private List<Long> userIds;
}
