package com.rsmanager.repository.local;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocalCashOutRepositoryCustom {
    Page<Object[]> findCashOutWithUser(String whereClause, Object[] params, Pageable pageable);
}
