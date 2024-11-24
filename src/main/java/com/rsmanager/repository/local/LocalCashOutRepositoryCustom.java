package com.rsmanager.repository.local;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

@Repository
public interface LocalCashOutRepositoryCustom {
    Page<Object[]> findCashOutWithUser(String whereClause, Object[] params, Pageable pageable);
}
