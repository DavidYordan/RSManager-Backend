package com.rsmanager.dto.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaginationDTO {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
