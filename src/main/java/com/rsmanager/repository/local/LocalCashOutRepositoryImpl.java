package com.rsmanager.repository.local;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

@Repository
public class LocalCashOutRepositoryImpl implements LocalCashOutRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Object[]> findCashOutWithUser(String whereClause, Object[] params, Pageable pageable) {
        String baseQuery = """
                SELECT
                    c.id AS id,                              -- Index 0
                    c.create_at AS createAt,                 -- Index 1
                    c.money AS money,                        -- Index 2
                    c.out_at AS outAt,                       -- Index 3
                    c.user_id AS platformUserId,             -- Index 4
                    u.user_id AS userId,                     -- Index 5
                    t.phone AS username,                     -- Index 6
                    u.fullname AS fullname,                  -- Index 7
                    inviter.user_id AS inviterId,            -- Index 8
                    inviter.username AS inviterName,         -- Index 9
                    inviter.fullname AS inviterFullName,     -- Index 10
                    manager.user_id AS managerId,            -- Index 11
                    manager.username AS managerName,         -- Index 12
                    manager.fullname AS managerFullName,     -- Index 13
                    c.order_number AS orderNumber,           -- Index 14
                    c.state AS state,                        -- Index 15
                    c.refund AS refund,                      -- Index 16
                    c.classify AS classify,                  -- Index 17
                    c.rate AS rate,                          -- Index 18
                    c.recipient AS recipient,                -- Index 19
                    c.bank_number AS bankNumber,             -- Index 20
                    c.bank_name AS bankName,                 -- Index 21
                    c.bank_address AS bankAddress,           -- Index 22
                    c.bank_code AS bankCode,                 -- Index 23
                    c.type AS type                           -- Index 24
                FROM cash_out c
                LEFT JOIN tb_user t ON c.user_id = t.user_id
                LEFT JOIN backend_user u ON c.user_id = u.platform_id
                LEFT JOIN inviter_relationship ir ON u.user_id = ir.user_id AND ir.status = TRUE
                LEFT JOIN backend_user inviter ON ir.inviter_id = inviter.user_id
                LEFT JOIN manager_relationship mr ON u.user_id = mr.user_id AND mr.status = TRUE
                LEFT JOIN backend_user manager ON mr.manager_id = manager.user_id
                WHERE 1=1
                """ + whereClause + "ORDER BY c.id ASC";

        String countQueryStr = """
            SELECT COUNT(*)
            FROM cash_out c
            LEFT JOIN tb_user t ON c.user_id = t.user_id
            LEFT JOIN backend_user u ON c.user_id = u.platform_id
            LEFT JOIN inviter_relationship ir ON u.user_id = ir.user_id AND ir.status = TRUE
            LEFT JOIN backend_user inviter ON ir.inviter_id = inviter.user_id
            LEFT JOIN manager_relationship mr ON u.user_id = mr.user_id AND mr.status = TRUE
            LEFT JOIN backend_user manager ON mr.manager_id = manager.user_id
            WHERE 1=1
            """ + whereClause + "ORDER BY c.id ASC";

        Query query = entityManager.createNativeQuery(baseQuery);
        Query countQuery = entityManager.createNativeQuery(countQueryStr);

        // 设置参数
        int index = 1;
        for (Object param : params) {
            query.setParameter(index, param);
            countQuery.setParameter(index, param);
            index++;
        }

        // 设置分页
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();

        // logger.info("resultList: {}", resultList);

        Object countResult = countQuery.getSingleResult();
        long totalElements;

        if (countResult instanceof Number) {
            totalElements = ((Number) countResult).longValue();
        } else {
            throw new IllegalStateException("Unexpected count query result type: " + countResult.getClass());
        }

        return new PageImpl<>(resultList, pageable, totalElements);
    }
}
