package com.rsmanager.repository.local;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.rsmanager.dto.application.ApplicationFlowRecordDTO;
import com.rsmanager.dto.application.ApplicationPaymentRecordDTO;
import com.rsmanager.dto.application.ApplicationResponseDTO;
import com.rsmanager.dto.application.ApplicationSearchDTO;
import com.rsmanager.model.ApplicationProcessRecord;
import com.rsmanager.model.BackendUser;
import com.rsmanager.model.TbUser;
import com.rsmanager.security.UserContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ApplicationProcessRecordRepositoryCustomImpl implements ApplicationProcessRecordRepositoryCustom {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationProcessRecordRepositoryCustomImpl.class);

    private final EntityManager entityManager;

    private final UserContext userContext;

    @Override
    public Page<ApplicationResponseDTO> searchApplications(ApplicationSearchDTO request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 主查询，用于获取数据
        CriteriaQuery<ApplicationResponseDTO> query = cb.createQuery(ApplicationResponseDTO.class);
        Root<ApplicationProcessRecord> root = query.from(ApplicationProcessRecord.class);
        query.distinct(true);

        Join<ApplicationProcessRecord, BackendUser> userJoin = root.join("user", JoinType.LEFT);

        Join<ApplicationProcessRecord, BackendUser> inviterJoin = root.join("inviter", JoinType.LEFT);

        Join<ApplicationProcessRecord, BackendUser> managerJoin = root.join("manager", JoinType.LEFT);

        Join<ApplicationProcessRecord, TbUser> tbUserJoin = root.join("tbUser", JoinType.LEFT);

        logger.info("User ID: {}, Role ID: {}", userContext.getOperatorId(), userContext.getRoleId());

        Long operatorId = userContext.getOperatorId();
        Integer operatorRoleId = userContext.getRoleId();

        Set<Long> allowedUserIds = new HashSet<>();
        if (operatorRoleId == 1 || operatorRoleId == 8) {
            // Super admin roles, no restrictions
        } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
            allowedUserIds = getAllSubordinateIds(new HashSet<>(Collections.singleton(operatorId)));
            cb.in(managerJoin.get("userId")).value(allowedUserIds);
        } else {
            throw new IllegalStateException("You do not have permission to search users.");
        }

        logger.info("Allowed user IDs: {}", allowedUserIds);

        List<Predicate> predicates = buildTrafficPredicates(
            request, cb, root, userJoin, inviterJoin, managerJoin, tbUserJoin);
        logger.info("Predicates: {}", predicates);

        // 选择需要的字段并构建 DTO
        query.select(cb.construct(
                ApplicationResponseDTO.class,
                root.get("processId"),
                userJoin.get("userId"),
                userJoin.get("username") == null ? userJoin.get("username") : root.get("username"),
                userJoin.get("fullname") == null ? userJoin.get("fullname") : root.get("fullname"),
                tbUserJoin.get("userId"),
                tbUserJoin.get("invitationCode"),
                tbUserJoin.get("inviterCode"),
                root.get("roleId"),
                inviterJoin.get("userId"),
                inviterJoin.get("username"),
                inviterJoin.get("fullname"),
                root.get("inviterName"),
                managerJoin.get("userId"),
                managerJoin.get("username"),
                managerJoin.get("fullname"),
                root.get("rateA"),
                root.get("rateB"),
                root.get("startDate"),
                root.get("tiktokAccount"),
                root.get("regionName"),
                root.get("currencyName"),
                root.get("currencyCode"),
                root.get("projectName"),
                root.get("projectAmount"),
                root.get("paymentMethod"),
                root.get("processStatus"),
                root.get("comments"),
                root.get("actionStr"),
                root.get("createdAt")
        )).where(cb.and(predicates.toArray(new Predicate[0])));

        // 设置排序
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // 创建 TypedQuery 并设置分页参数
        TypedQuery<ApplicationResponseDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        logger.info("Executing query: {}", typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());

        List<ApplicationResponseDTO> resultList = typedQuery.getResultList();

        logger.info("Query result size: {}", resultList.size());

        // 手动填充 DTOs
        Set<Long> processIds = resultList.stream()
                                        .map(ApplicationResponseDTO::getProcessId)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toSet());

        logger.info("Fetched process IDs: {}", processIds);

        // 填充记录
        Map<Long, List<ApplicationPaymentRecordDTO>> paymentRecordMap = fetchAllPaymentRecords(processIds);
        Map<Long, List<ApplicationFlowRecordDTO>> flowRecordMap = fetchAllFlowRecords(processIds);

        logger.info("Fetched payment records: {}", paymentRecordMap);

        for (ApplicationResponseDTO dto : resultList) {
            Long processId = dto.getProcessId();
            dto.setApplicationPaymentRecordDTOs(paymentRecordMap.getOrDefault(processId, Collections.emptyList()));
            dto.setApplicationFlowRecordDTOs(flowRecordMap.getOrDefault(processId, Collections.emptyList()));
        }

        // 统计总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ApplicationProcessRecord> countRoot = countQuery.from(ApplicationProcessRecord.class);

        Join<ApplicationProcessRecord, BackendUser> countUserJoin = countRoot.join("user", JoinType.LEFT);

        Join<ApplicationProcessRecord, BackendUser> countInviterJoin = countRoot.join("inviter", JoinType.LEFT);

        Join<ApplicationProcessRecord, BackendUser> countManagerJoin = countRoot.join("manager", JoinType.LEFT);

        Join<ApplicationProcessRecord, TbUser> countTbUserJoin = countRoot.join("tbUser", JoinType.LEFT);

        if (!allowedUserIds.isEmpty()) {
            cb.in(countManagerJoin.get("userId")).value(allowedUserIds);
        }

        List<Predicate> countPredicates = buildTrafficPredicates(
            request, cb, countRoot, countUserJoin, countInviterJoin, countManagerJoin, countTbUserJoin);

        countQuery.select(cb.countDistinct(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildTrafficPredicates(ApplicationSearchDTO request, CriteriaBuilder cb, Root<ApplicationProcessRecord> root,
                    Join<ApplicationProcessRecord, BackendUser> userJoin,
                    Join<ApplicationProcessRecord, BackendUser> inviterJoin,
                    Join<ApplicationProcessRecord, BackendUser> managerJoin,
                    Join<ApplicationProcessRecord, TbUser> tbUserJoin) {
    
        List<Predicate> predicates = new ArrayList<>();

        // 基本过滤条件
        if (request.getProcessId() != null) {
            predicates.add(cb.equal(root.get("processId"), request.getProcessId()));
        }
        if (request.getUserId() != null) {
            predicates.add(cb.equal(userJoin.get("userId"), request.getUserId()));
        }
        if (StringUtils.hasText(request.getUsername())) {
            predicates.add(cb.like(cb.coalesce(userJoin.get("username"), root.get("username")), "%" + request.getUsername().trim() + "%"));
        }
        if (StringUtils.hasText(request.getFullname())) {
            predicates.add(cb.like(cb.coalesce(userJoin.get("fullname"), root.get("fullname")), "%" + request.getFullname().trim() + "%"));
        }
        if (request.getRoleId() != null) {
            predicates.add(cb.equal(root.get("roleId"), request.getRoleId()));
        }
        if (request.getInviterId() != null) {
            predicates.add(cb.equal(inviterJoin.get("userId"), request.getInviterId()));
        }
        if (StringUtils.hasText(request.getInviterName())) {
            predicates.add(cb.like(inviterJoin.get("username"), "%" + request.getInviterName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInviterFullname())) {
            predicates.add(cb.like(inviterJoin.get("fullname"), "%" + request.getInviterFullname().trim() + "%"));
        }
        if (request.getManagerId() != null) {
            predicates.add(cb.equal(managerJoin.get("userId"), request.getManagerId()));
        }
        if (StringUtils.hasText(request.getManagerName())) {
            predicates.add(cb.like(managerJoin.get("username"), "%" + request.getManagerName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getManagerFullname())) {
            predicates.add(cb.like(managerJoin.get("fullname"), "%" + request.getManagerFullname().trim() + "%"));
        }
        if (request.getPlatformId() != null) {
            predicates.add(cb.equal(tbUserJoin.get("userId"), request.getPlatformId()));
        }
        if (StringUtils.hasText(request.getInvitationCode())) {
            predicates.add(cb.like(tbUserJoin.get("invitationCode"), "%" + request.getInvitationCode().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInviterCode())) {
            predicates.add(cb.like(tbUserJoin.get("inviterCode"), "%" + request.getInviterCode().trim() + "%"));
        }
        if (StringUtils.hasText(request.getTiktokAccount())) {
            predicates.add(cb.like(root.get("tiktokAccount"), "%" + request.getTiktokAccount().trim() + "%"));
        }
        if (StringUtils.hasText(request.getRegionName())) {
            predicates.add(cb.equal(root.get("regionName"), request.getRegionName().trim()));
        }
        if (StringUtils.hasText(request.getCurrencyName())) {
            predicates.add(cb.equal(root.get("currencyName"), request.getCurrencyName().trim()));
        }
        if (StringUtils.hasText(request.getCurrencyCode())) {
            predicates.add(cb.equal(root.get("currencyCode"), request.getCurrencyCode().trim()));
        }
        if (StringUtils.hasText(request.getProjectName())) {
            predicates.add(cb.like(root.get("projectName"), "%" + request.getProjectName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getPaymentMethod())) {
            predicates.add(cb.like(root.get("paymentMethod"), "%" + request.getPaymentMethod().trim() + "%"));
        }
        if (request.getProcessStatuses() != null && !request.getProcessStatuses().isEmpty()) {
            predicates.add(root.get("processStatus").in(request.getProcessStatuses()));
        }
        if (request.getStartAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), request.getStartAfter()));
        }
        if (request.getStartBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), request.getStartBefore()));
        }
        if (request.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter()));
        }
        if (request.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedBefore()));
        }

        return predicates;
    }

    private Map<Long, List<ApplicationPaymentRecordDTO>> fetchAllPaymentRecords(Set<Long> processIds) {

        LocalDate latestDate = LocalDate.of(1970, 1, 1);

        String jpql = """
                    SELECT new com.rsmanager.dto.application.ApplicationPaymentRecordDTO(
                        p.paymentId, r.processId, p.regionName, p.currencyName, p.currencyCode, cr.rate,
                        p.projectName, p.projectAmount, p.projectCurrencyName, p.projectCurrencyCode, mcr.rate,
                        p.paymentMethod, p.paymentAmount, p.fee, p.actual, p.paymentDate, p.createrId, p.createrName,
                        p.createrFullname, p.createdAt, p.financeId, p.financeName, p.financeFullname,
                        p.financeApprovalTime, p.comments, p.status)
                    FROM ApplicationProcessRecord r
                    JOIN r.applicationPaymentRecords p
                    LEFT JOIN UsdRate cr WITH cr.date = p.paymentDate AND cr.currencyCode = p.currencyCode
                    LEFT JOIN UsdRate mcr WITH mcr.date = :latestDate AND mcr.currencyCode = p.projectCurrencyCode
                    WHERE r.processId IN :processIds
                    """;
        List<ApplicationPaymentRecordDTO> details = entityManager.createQuery(jpql, ApplicationPaymentRecordDTO.class)
                                                        .setParameter("processIds", processIds)
                                                        .setParameter("latestDate", latestDate)
                                                        .getResultList();

        return details.stream().collect(Collectors.groupingBy(ApplicationPaymentRecordDTO::getProcessId));
    }

    private Map<Long, List<ApplicationFlowRecordDTO>> fetchAllFlowRecords(Set<Long> processIds) {

        String jpql = """
                    SELECT new com.rsmanager.dto.application.ApplicationFlowRecordDTO(
                        f.flowId, r.processId, f.action, f.createrId, f.createrName, f.createrFullname,
                        f.createdAt, f.comments)
                    FROM ApplicationProcessRecord r
                    JOIN r.applicationFlowRecords f
                    WHERE r.processId IN :processIds
                    """;

        List<ApplicationFlowRecordDTO> details = entityManager.createQuery(jpql, ApplicationFlowRecordDTO.class)
                                                        .setParameter("processIds", processIds)
                                                        .getResultList();

        return details.stream().collect(Collectors.groupingBy(ApplicationFlowRecordDTO::getProcessId));
    }

    /**
     * Helper方法：获取所有下属用户IDs（Managers）
     */
    private Set<Long> getAllSubordinateIds(Set<Long> initialManagerIds) {
        Set<Long> visited = new HashSet<>();
        Set<Long> subordinateIds = new HashSet<>(initialManagerIds);
    
        while (!initialManagerIds.isEmpty()) {
            Set<Long> directSubordinateIds = getSubordinateIds(initialManagerIds, visited);
            // 移除已访问的 ID，防止循环
            directSubordinateIds.removeAll(visited);
    
            if (directSubordinateIds.isEmpty()) {
                break;
            }
    
            // 添加到已访问和下属 ID 集合中
            visited.addAll(directSubordinateIds);
            subordinateIds.addAll(directSubordinateIds);
    
            // 更新 managerIds 为下一层的下属 IDs
            initialManagerIds = directSubordinateIds;
        }
    
        return subordinateIds;
    }

    /**
     * Helper方法：获取直接下属用户IDs（Managers）
     */
    public Set<Long> getSubordinateIds(Set<Long> managerIds, Set<Long> visited) {
        if (managerIds == null || managerIds.isEmpty()) {
            return Collections.emptySet();
        }

        String jpql = "SELECT m.user.userId FROM ManagerRelationship m WHERE m.manager.userId IN :managerIds AND m.status = true";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("managerIds", managerIds);
        return new HashSet<>(query.getResultList());
    }
}
