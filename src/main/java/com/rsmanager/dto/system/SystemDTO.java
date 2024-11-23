package com.rsmanager.dto.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemDTO {
    private Long paramId;
    private String paramName;
    private String classify;
    private String paramValue;
    private String description;
}