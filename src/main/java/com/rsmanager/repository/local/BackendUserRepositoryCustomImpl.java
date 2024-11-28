package com.rsmanager.repository.local;

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

import com.rsmanager.dto.traffic.SearchTrafficDTO;
import com.rsmanager.dto.traffic.SearchTrafficResponseDTO;
import com.rsmanager.dto.traffic.TiktokVideoDetailsDTO;
import com.rsmanager.dto.user.SearchUsersDTO;
import com.rsmanager.dto.user.SearchUsersResponseDTO;
import com.rsmanager.dto.user.ApplicationPaymentRecordDTO;
import com.rsmanager.dto.user.RolePermissionRelationshipDTO;
import com.rsmanager.model.BackendUser;
import com.rsmanager.model.InviteMoney;
import com.rsmanager.model.InviterRelationship;
import com.rsmanager.model.ManagerRelationship;
import com.rsmanager.model.RolePermissionRelationship;
import com.rsmanager.model.TbUser;
import com.rsmanager.model.TeacherRelationship;
import com.rsmanager.model.TiktokRelationship;
import com.rsmanager.model.TiktokUserDetails;
import com.rsmanager.model.UserIntegral;
import com.rsmanager.model.UserMoney;
import com.rsmanager.security.UserContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BackendUserRepositoryCustomImpl implements BackendUserRepositoryCustom {

    private final EntityManager entityManager;

    private final UserContext userContext;

    private final LocalInviteRepository localInviteRepository;
    private final LocalTbUserRepository localTbUserRepository;

    @Override
    public Page<SearchTrafficResponseDTO> searchTraffics(SearchTrafficDTO request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 主查询，用于获取数据
        CriteriaQuery<SearchTrafficResponseDTO> query = cb.createQuery(SearchTrafficResponseDTO.class);
        Root<BackendUser> root = query.from(BackendUser.class);
        query.distinct(true);

        // 构建关联（JOIN），只选择需要的关联字段
        Join<BackendUser, TbUser> tbUserJoin = root.join("tbUser", JoinType.LEFT);
        
        Join<BackendUser, RolePermissionRelationship> roleJoin = root.join("rolePermissionRelationships", JoinType.INNER);
        Subquery<Long> roleSubquery = query.subquery(Long.class);
        Root<RolePermissionRelationship> roleSubRoot = roleSubquery.from(RolePermissionRelationship.class);
        roleSubquery.select(cb.max(roleSubRoot.get("recordId")));
        roleSubquery.where(
            cb.equal(roleSubRoot.get("user"), root),
            cb.isNull(roleSubRoot.get("endDate")),
            roleSubRoot.get("roleId").in(Set.of(4, 5, 6))
        );
        roleJoin.on(
            cb.and(
                cb.isNull(roleJoin.get("endDate")),
                roleJoin.get("roleId").in(Set.of(4, 5, 6)),
                cb.equal(roleJoin.get("recordId"), roleSubquery)
            )
        );
        
        Join<BackendUser, TiktokRelationship> tiktokJoin = root.join("tiktokRelationships", JoinType.LEFT);
        tiktokJoin.on(cb.isTrue(tiktokJoin.get("status")));
        Join<TiktokRelationship, TiktokUserDetails> tiktokUserJoin =  tiktokJoin.join("tiktokUserDetails", JoinType.LEFT);
        
        Join<BackendUser, InviterRelationship> inviterJoin = root.join("inviterRelationships", JoinType.LEFT);
        inviterJoin.on(cb.isTrue(inviterJoin.get("status")));
        Join<InviterRelationship, BackendUser> inviterUserJoin = inviterJoin.join("inviter", JoinType.LEFT);
        
        Join<BackendUser, ManagerRelationship> managerJoin = root.join("managerRelationships", JoinType.LEFT);
        managerJoin.on(cb.isTrue(managerJoin.get("status")));
        Join<ManagerRelationship, BackendUser> managerUserJoin = managerJoin.join("manager", JoinType.LEFT);
        
        Join<BackendUser, TeacherRelationship> teacherJoin = root.join("teacherRelationships", JoinType.LEFT);
        teacherJoin.on(cb.isTrue(teacherJoin.get("status")));
        Join<TeacherRelationship, BackendUser> teacherUserJoin = teacherJoin.join("teacher", JoinType.LEFT);

        Long operatorId = userContext.getOperatorId();
        Integer operatorRoleId = userContext.getRoleId();

        Set<Long> allowedUserIds = new HashSet<>();
        if (operatorRoleId == 1 || operatorRoleId == 8) {
            // Super admin roles, no restrictions
        } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
            allowedUserIds = getAllSubordinateIds(new HashSet<>(Collections.singleton(operatorId)));
            cb.in(root.get("userId")).value(allowedUserIds);
        } else {
            throw new IllegalStateException("You do not have permission to search users.");
        }

        List<Predicate> predicates = buildTrafficPredicates(
            request, cb, root, tbUserJoin, roleJoin, tiktokJoin, tiktokUserJoin,
            inviterUserJoin, managerUserJoin, teacherUserJoin);

        // 选择需要的字段并构建 DTO
        query.select(cb.construct(
                SearchTrafficResponseDTO.class,
                root.get("userId"),
                root.get("username"),
                root.get("fullname"),
                root.get("regionName"),
                roleJoin.get("roleId"),
                roleJoin.get("roleName"),
                inviterUserJoin.get("userId"),
                inviterUserJoin.get("username"),
                inviterUserJoin.get("fullname"),
                managerUserJoin.get("userId"),
                managerUserJoin.get("username"),
                managerUserJoin.get("fullname"),
                teacherUserJoin.get("userId"),
                teacherUserJoin.get("username"),
                teacherUserJoin.get("fullname"),
                tbUserJoin.get("userId"),
                tbUserJoin.get("inviterCode"),
                tbUserJoin.get("invitationCode"),
                tiktokJoin.get("tiktokAccount"),
                tiktokUserJoin.get("tiktokId"),
                tiktokUserJoin.get("uniqueId"),
                tiktokUserJoin.get("nickname"),
                tiktokUserJoin.get("diggCount"),
                tiktokUserJoin.get("followerCount"),
                tiktokUserJoin.get("followingCount"),
                tiktokUserJoin.get("friendCount"),
                tiktokUserJoin.get("heartCount"),
                tiktokUserJoin.get("videoCount"),
                tiktokUserJoin.get("updatedAt"),
                tiktokUserJoin.get("comments"),
                tiktokUserJoin.get("link"),
                tiktokUserJoin.get("risk")
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
        TypedQuery<SearchTrafficResponseDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<SearchTrafficResponseDTO> resultList = typedQuery.getResultList();

        // 手动填充 tiktokVideoDetailDTOs
        List<String> tiktokIds = resultList.stream()
                                       .map(SearchTrafficResponseDTO::getTiktokId)
                                       .filter(Objects::nonNull)
                                       .distinct()
                                       .collect(Collectors.toList());
        Map<String, List<TiktokVideoDetailsDTO>> videoDetailsMap = fetchAllVideoDetails(tiktokIds);

        for (SearchTrafficResponseDTO dto : resultList) {
            if (dto.getPlatformId() != null) {
                dto.setPlatformInviteCount(localTbUserRepository.countByInviterCode(dto.getInvitationCode()));
            }
            if (dto.getTiktokId() != null) {
                dto.setTiktokVideoDetailDTOs(videoDetailsMap.getOrDefault(dto.getTiktokId(), Collections.emptyList()));
            }
        }

        // 统计总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<BackendUser> countRoot = countQuery.from(BackendUser.class);

        Join<BackendUser, TbUser> countTbUserJoin = countRoot.join("tbUser", JoinType.LEFT);
        
        Join<BackendUser, RolePermissionRelationship> countRoleJoin = countRoot.join("rolePermissionRelationships", JoinType.INNER);
        Subquery<Long> countRoleSubquery = countQuery.subquery(Long.class);
        Root<RolePermissionRelationship> countRoleSubRoot = countRoleSubquery.from(RolePermissionRelationship.class);
        countRoleSubquery.select(cb.max(countRoleSubRoot.get("recordId")));
        countRoleSubquery.where(
            cb.equal(countRoleSubRoot.get("user"), countRoot),
            cb.isNull(countRoleSubRoot.get("endDate")),
            countRoleSubRoot.get("roleId").in(Set.of(4, 5, 6))
        );
        countRoleJoin.on(
            cb.and(
                cb.isNull(countRoleJoin.get("endDate")),
                countRoleJoin.get("roleId").in(Set.of(4, 5, 6)),
                cb.equal(countRoleJoin.get("recordId"), countRoleSubquery)
            )
        );
        
        Join<BackendUser, TiktokRelationship> countTiktokJoin = countRoot.join("tiktokRelationships", JoinType.LEFT);
        countTiktokJoin.on(cb.isTrue(countTiktokJoin.get("status")));
        Join<TiktokRelationship, TiktokUserDetails> countTiktokUserJoin =  countTiktokJoin.join("tiktokUserDetails", JoinType.LEFT);
        
        Join<BackendUser, InviterRelationship> countInviterJoin = countRoot.join("inviterRelationships", JoinType.LEFT);
        countInviterJoin.on(cb.isTrue(countInviterJoin.get("status")));
        Join<InviterRelationship, BackendUser> countInviterUserJoin = countInviterJoin.join("inviter", JoinType.LEFT);
        
        Join<BackendUser, ManagerRelationship> countManagerJoin = countRoot.join("managerRelationships", JoinType.LEFT);
        countManagerJoin.on(cb.isTrue(countManagerJoin.get("status")));
        Join<ManagerRelationship, BackendUser> countManagerUserJoin = countManagerJoin.join("manager", JoinType.LEFT);
        
        Join<BackendUser, TeacherRelationship> countTeacherJoin = countRoot.join("teacherRelationships", JoinType.LEFT);
        countTeacherJoin.on(cb.isTrue(countTeacherJoin.get("status")));
        Join<TeacherRelationship, BackendUser> countTeacherUserJoin = countTeacherJoin.join("teacher", JoinType.LEFT);

        if (operatorRoleId == 1 || operatorRoleId == 8) {
            // Super admin roles, no restrictions
        } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
            cb.in(root.get("userId")).value(allowedUserIds);
        } else {
            throw new IllegalStateException("You do not have permission to search users.");
        }

        List<Predicate> countPredicates = buildTrafficPredicates(
            request, cb, countRoot, countTbUserJoin, countRoleJoin, countTiktokJoin, countTiktokUserJoin,
            countInviterUserJoin, countManagerUserJoin, countTeacherUserJoin);

        countQuery.select(cb.countDistinct(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildTrafficPredicates(SearchTrafficDTO request, CriteriaBuilder cb, Root<BackendUser> root,
                                        Join<BackendUser, TbUser> tbUserJoin,
                                        Join<BackendUser, RolePermissionRelationship> roleJoin,
                                        Join<BackendUser, TiktokRelationship> tiktokJoin,
                                        Join<TiktokRelationship, TiktokUserDetails> tiktokUserJoin,
                                        Join<InviterRelationship, BackendUser> inviterUserJoin,
                                        Join<ManagerRelationship, BackendUser> managerUserJoin,
                                        Join<TeacherRelationship, BackendUser> teacherUserJoin) {
    
        List<Predicate> predicates = new ArrayList<>();

        // 基本过滤条件
        if (request.getUserId() != null) {
            predicates.add(cb.equal(root.get("userId"), request.getUserId()));
        }
        if (StringUtils.hasText(request.getUsername())) {
            predicates.add(cb.like(root.get("username"), "%" + request.getUsername().trim() + "%"));
        }
        if (StringUtils.hasText(request.getFullname())) {
            predicates.add(cb.like(root.get("fullname"), "%" + request.getFullname().trim() + "%"));
        }
        if (request.getRoleId() != null) {
            predicates.add(cb.equal(roleJoin.get("roleId"), request.getRoleId()));
        }
        if (request.getInviterId() != null) {
            predicates.add(cb.equal(inviterUserJoin.get("userId"), request.getInviterId()));
        }
        if (StringUtils.hasText(request.getInviterName())) {
            predicates.add(cb.like(inviterUserJoin.get("username"), "%" + request.getInviterName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInviterFullname())) {
            predicates.add(cb.like(inviterUserJoin.get("fullname"), "%" + request.getInviterFullname().trim() + "%"));
        }
        if (request.getManagerId() != null) {
            predicates.add(cb.equal(managerUserJoin.get("userId"), request.getManagerId()));
        }
        if (StringUtils.hasText(request.getManagerName())) {
            predicates.add(cb.like(managerUserJoin.get("username"), "%" + request.getManagerName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getManagerFullname())) {
            predicates.add(cb.like(managerUserJoin.get("fullname"), "%" + request.getManagerFullname().trim() + "%"));
        }
        if (request.getTeacherId() != null) {
            predicates.add(cb.equal(teacherUserJoin.get("userId"), request.getTeacherId()));
        }
        if (StringUtils.hasText(request.getTeacherName())) {
            predicates.add(cb.like(teacherUserJoin.get("username"), "%" + request.getTeacherName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getTeacherFullname())) {
            predicates.add(cb.like(teacherUserJoin.get("fullname"), "%" + request.getTeacherFullname().trim() + "%"));
        }
        if (request.getPlatformId() != null) {
            predicates.add(cb.equal(tbUserJoin.get("userId"), request.getPlatformId()));
        }
        if (StringUtils.hasText(request.getTiktokAccount())) {
            predicates.add(cb.like(tiktokJoin.get("tiktokAccount"), "%" + request.getTiktokAccount().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInviterCode())) {
            predicates.add(cb.like(tbUserJoin.get("inviterCode"), "%" + request.getInviterCode().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInvitationCode())) {
            predicates.add(cb.like(tbUserJoin.get("invitationCode"), "%" + request.getInvitationCode().trim() + "%"));
        }
        if (request.getInvitationType() != null) {
            predicates.add(cb.equal(tbUserJoin.get("invitationType"), request.getInvitationType()));
        }
        if (StringUtils.hasText(request.getRegionName())) {
            predicates.add(cb.equal(root.get("regionName"), request.getRegionName().trim()));
        }
        if (StringUtils.hasText(request.getCurrencyName())) {
            predicates.add(cb.equal(root.get("currencyName"), request.getCurrencyName().trim()));
        }
        if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), request.getStatus()));
        }
        if (request.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), request.getCreatedAfter()));
        }
        if (request.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), request.getCreatedBefore()));
        }

        return predicates;
    }

    private Map<String, List<TiktokVideoDetailsDTO>> fetchAllVideoDetails(List<String> tiktokIds) {
        String jpql = """
                    SELECT new com.rsmanager.dto.traffic.TiktokVideoDetailsDTO(
                    v.tiktokVideoId, v.authorId, v.videoDesc, v.categoryType, v.collectCount,
                    v.commentCount, v.diggCount, v.playCount, v.repostCount, v.shareCount,
                    v.createTime, v.updatedAt)
                    FROM TiktokVideoDetails v WHERE v.authorId IN :authorIds
                    """;
        List<TiktokVideoDetailsDTO> details = entityManager.createQuery(jpql, TiktokVideoDetailsDTO.class)
                                                        .setParameter("authorIds", tiktokIds)
                                                        .getResultList();

        // 分组映射
        return details.stream().collect(Collectors.groupingBy(TiktokVideoDetailsDTO::getAuthorId));
    }

    @Override
    public Page<SearchUsersResponseDTO> searchUsers(SearchUsersDTO request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 主查询，用于获取数据
        CriteriaQuery<SearchUsersResponseDTO> query = cb.createQuery(SearchUsersResponseDTO.class);
        Root<BackendUser> root = query.from(BackendUser.class);
        query.distinct(true);

        // 构建关联（JOIN），只选择需要的关联字段
        Join<BackendUser, TbUser> tbUserJoin = root.join("tbUser", JoinType.LEFT);

        Join<TbUser, InviteMoney> inviteMoneyJoin = tbUserJoin.join("inviteMoney", JoinType.LEFT);

        Join<TbUser, UserMoney> userMoneyJoin = tbUserJoin.join("userMoney", JoinType.LEFT);

        Join<TbUser, UserIntegral> userIntegralJoin = tbUserJoin.join("userIntegral", JoinType.LEFT);
        
        Join<BackendUser, RolePermissionRelationship> roleJoin = root.join("rolePermissionRelationships", JoinType.LEFT);
        Subquery<Long> roleSubquery = query.subquery(Long.class);
        Root<RolePermissionRelationship> roleSubRoot = roleSubquery.from(RolePermissionRelationship.class);
        roleSubquery.select(cb.max(roleSubRoot.get("recordId")));
        roleSubquery.where(
            cb.equal(roleSubRoot.get("user"), root),
            cb.isNull(roleSubRoot.get("endDate"))
        );
        roleJoin.on(
            cb.and(
                cb.isNull(roleJoin.get("endDate")),
                cb.equal(roleJoin.get("recordId"), roleSubquery)
            )
        );

        Join<BackendUser, TiktokRelationship> tiktokJoin = root.join("tiktokRelationships", JoinType.LEFT);
        tiktokJoin.on(cb.isTrue(tiktokJoin.get("status")));
        Join<TiktokRelationship, TiktokUserDetails> tiktokUserJoin =  tiktokJoin.join("tiktokUserDetails", JoinType.LEFT);
        
        Join<BackendUser, InviterRelationship> inviterJoin = root.join("inviterRelationships", JoinType.LEFT);
        inviterJoin.on(cb.isTrue(inviterJoin.get("status")));
        Join<InviterRelationship, BackendUser> inviterUserJoin = inviterJoin.join("inviter", JoinType.LEFT);
        
        Join<BackendUser, ManagerRelationship> managerJoin = root.join("managerRelationships", JoinType.LEFT);
        managerJoin.on(cb.isTrue(managerJoin.get("status")));
        Join<ManagerRelationship, BackendUser> managerUserJoin = managerJoin.join("manager", JoinType.LEFT);
        
        Join<BackendUser, TeacherRelationship> teacherJoin = root.join("teacherRelationships", JoinType.LEFT);
        teacherJoin.on(cb.isTrue(teacherJoin.get("status")));
        Join<TeacherRelationship, BackendUser> teacherUserJoin = teacherJoin.join("teacher", JoinType.LEFT);

        // Join<BackendUser, ApplicationProcessRecord> applicationJoin = root.join("applicationProcessRecordAsUser", JoinType.LEFT);

        // Join<ApplicationProcessRecord, ApplicationProcessRecord> paymentJoin = applicationJoin.join("payment", JoinType.LEFT);
    
        Long operatorId = userContext.getOperatorId();
        Integer operatorRoleId = userContext.getRoleId();

        Set<Long> allowedUserIds = new HashSet<>();
        if (operatorRoleId == 1 || operatorRoleId == 8) {
            // Super admin roles, no restrictions
        } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
            allowedUserIds = getAllSubordinateIds(new HashSet<>(Collections.singleton(operatorId)));
            cb.in(root.get("userId")).value(allowedUserIds);
        } else {
            throw new IllegalStateException("You do not have permission to search users.");
        }

        List<Predicate> predicates = buildUserPredicates(
            request, cb, root, tbUserJoin, roleJoin, tiktokJoin, tiktokUserJoin, inviterUserJoin,
            managerUserJoin, teacherUserJoin);

        // 选择需要的字段并构建 DTO
        query.select(cb.construct(
                SearchUsersResponseDTO.class,
                root.get("userId"),
                root.get("username"),
                root.get("fullname"),
                roleJoin.get("roleId"),
                roleJoin.get("roleName"),
                root.get("regionName"),
                root.get("currencyName"),
                root.get("createdAt"),
                root.get("status"),
                inviterUserJoin.get("userId"),
                inviterUserJoin.get("username"),
                inviterUserJoin.get("fullname"),
                managerUserJoin.get("userId"),
                managerUserJoin.get("username"),
                managerUserJoin.get("fullname"),
                teacherUserJoin.get("userId"),
                teacherUserJoin.get("username"),
                teacherUserJoin.get("fullname"),
                tbUserJoin.get("userId"),
                tbUserJoin.get("inviterCode"),
                tbUserJoin.get("invitationCode"),
                tbUserJoin.get("invitationType"),
                inviteMoneyJoin.get("moneySum"),
                inviteMoneyJoin.get("money"),
                inviteMoneyJoin.get("cashOut"),
                userMoneyJoin.get("money"),
                userIntegralJoin.get("integralNum"),
                tiktokJoin.get("tiktokAccount"),
                tiktokUserJoin.get("tiktokId"),
                tiktokUserJoin.get("uniqueId"),
                tiktokUserJoin.get("nickname"),
                tiktokUserJoin.get("diggCount"),
                tiktokUserJoin.get("followerCount"),
                tiktokUserJoin.get("followingCount"),
                tiktokUserJoin.get("friendCount"),
                tiktokUserJoin.get("heartCount"),
                tiktokUserJoin.get("videoCount"),
                tiktokUserJoin.get("updatedAt"),
                tiktokUserJoin.get("comments")
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
        TypedQuery<SearchUsersResponseDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<SearchUsersResponseDTO> resultList = typedQuery.getResultList();

        // 手动填充 tiktokVideoDetailDTOs
        List<Long> userIds = resultList.stream()
                                       .map(SearchUsersResponseDTO::getUserId)
                                       .filter(Objects::nonNull)
                                       .distinct()
                                       .collect(Collectors.toList());
        Map<Long, List<ApplicationPaymentRecordDTO>> paymentRecordMap = fetchAllPaymentRecords(userIds);

        for (SearchUsersResponseDTO dto : resultList) {
            Long userId = dto.getUserId();
            if (dto.getPlatformId() != null) {
                dto.setPlatformInviteCount(localTbUserRepository.countByInviterCode(dto.getInvitationCode()));
                dto.setInviteDailyMoneySumDTOs(localInviteRepository.findDailyMoneySumByUserId(dto.getPlatformId()));
            }
            dto.setInviteCount(getInviteCount(userId));
            dto.setApplicationPaymentRecordDTOs(paymentRecordMap.getOrDefault(userId, Collections.emptyList()));
            dto.setRolePermissionRelationshipDTOs(getRolePermissionRelationships(userId));
        }

        // 统计总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<BackendUser> countRoot = countQuery.from(BackendUser.class);

        Join<BackendUser, TbUser> countTbUserJoin = countRoot.join("tbUser", JoinType.LEFT);

        countTbUserJoin.join("inviteMoney", JoinType.LEFT);

        countTbUserJoin.join("userMoney", JoinType.LEFT);

        countTbUserJoin.join("userIntegral", JoinType.LEFT);

        Join<BackendUser, RolePermissionRelationship> countRoleJoin = countRoot.join("rolePermissionRelationships", JoinType.LEFT);
        Subquery<Long> countRoleSubquery = countQuery.subquery(Long.class);
        Root<RolePermissionRelationship> countRoleSubRoot = countRoleSubquery.from(RolePermissionRelationship.class);
        countRoleSubquery.select(cb.max(countRoleSubRoot.get("recordId")));
        countRoleSubquery.where(
            cb.equal(countRoleSubRoot.get("user"), countRoot),
            cb.isNull(countRoleSubRoot.get("endDate"))
        );
        countRoleJoin.on(
            cb.and(
                cb.isNull(countRoleJoin.get("endDate")),
                cb.equal(countRoleJoin.get("recordId"), countRoleSubquery)
            )
        );

        Join<BackendUser, TiktokRelationship> countTiktokJoin = countRoot.join("tiktokRelationships", JoinType.LEFT);
        countTiktokJoin.on(cb.isTrue(countTiktokJoin.get("status")));
        Join<TiktokRelationship, TiktokUserDetails> countTiktokUserJoin =  countTiktokJoin.join("tiktokUserDetails", JoinType.LEFT);

        Join<BackendUser, InviterRelationship> countInviterJoin = countRoot.join("inviterRelationships", JoinType.LEFT);
        countInviterJoin.on(cb.isTrue(countInviterJoin.get("status")));
        Join<InviterRelationship, BackendUser> countInviterUserJoin = countInviterJoin.join("inviter", JoinType.LEFT);

        Join<BackendUser, ManagerRelationship> countManagerJoin = countRoot.join("managerRelationships", JoinType.LEFT);
        countManagerJoin.on(cb.isTrue(countManagerJoin.get("status")));
        Join<ManagerRelationship, BackendUser> countManagerUserJoin = countManagerJoin.join("manager", JoinType.LEFT);

        Join<BackendUser, TeacherRelationship> countTeacherJoin = countRoot.join("teacherRelationships", JoinType.LEFT);
        countTeacherJoin.on(cb.isTrue(countTeacherJoin.get("status")));
        Join<TeacherRelationship, BackendUser> countTeacherUserJoin = countTeacherJoin.join("teacher", JoinType.LEFT);

        if (operatorRoleId == 1 || operatorRoleId == 8) {
            // Super admin roles, no restrictions
        } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
            cb.in(countRoot.get("userId")).value(allowedUserIds);
        } else {
            throw new IllegalStateException("You do not have permission to search users.");
        }

        List<Predicate> countPredicates = buildUserPredicates(
            request, cb, countRoot, countTbUserJoin, countRoleJoin, countTiktokJoin, countTiktokUserJoin,
            countInviterUserJoin, countManagerUserJoin, countTeacherUserJoin);

        countQuery.select(cb.countDistinct(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildUserPredicates(SearchUsersDTO request, CriteriaBuilder cb, Root<BackendUser> root,
                                        Join<BackendUser, TbUser> tbUserJoin,
                                        Join<BackendUser, RolePermissionRelationship> roleJoin,
                                        Join<BackendUser, TiktokRelationship> tiktokJoin,
                                        Join<TiktokRelationship, TiktokUserDetails> tiktokUserJoin,
                                        Join<InviterRelationship, BackendUser> inviterUserJoin,
                                        Join<ManagerRelationship, BackendUser> managerUserJoin,
                                        Join<TeacherRelationship, BackendUser> teacherUserJoin) {
    
        List<Predicate> predicates = new ArrayList<>();

        // 基本过滤条件
        if (request.getUserId() != null) {
            predicates.add(cb.equal(root.get("userId"), request.getUserId()));
        }
        if (StringUtils.hasText(request.getUsername())) {
            predicates.add(cb.like(root.get("username"), "%" + request.getUsername().trim() + "%"));
        }
        if (StringUtils.hasText(request.getFullname())) {
            predicates.add(cb.like(root.get("fullname"), "%" + request.getFullname().trim() + "%"));
        }
        if (request.getRoleId() != null) {
            predicates.add(cb.equal(roleJoin.get("roleId"), request.getRoleId()));
        }
        if (request.getInviterId() != null) {
            predicates.add(cb.equal(inviterUserJoin.get("userId"), request.getInviterId()));
        }
        if (StringUtils.hasText(request.getInviterName())) {
            predicates.add(cb.like(inviterUserJoin.get("username"), "%" + request.getInviterName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInviterFullname())) {
            predicates.add(cb.like(inviterUserJoin.get("fullname"), "%" + request.getInviterFullname().trim() + "%"));
        }
        if (request.getInviterNotExists() != null) {
            if (request.getInviterNotExists()) {
                predicates.add(cb.isNotNull(inviterUserJoin));
            } else {
                predicates.add(cb.isNull(inviterUserJoin));
            }
        }
        if (request.getManagerId() != null) {
            predicates.add(cb.equal(managerUserJoin.get("userId"), request.getManagerId()));
        }
        if (StringUtils.hasText(request.getManagerName())) {
            predicates.add(cb.like(managerUserJoin.get("username"), "%" + request.getManagerName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getManagerFullname())) {
            predicates.add(cb.like(managerUserJoin.get("fullname"), "%" + request.getManagerFullname().trim() + "%"));
        }
        if (request.getTeacherId() != null) {
            predicates.add(cb.equal(teacherUserJoin.get("userId"), request.getTeacherId()));
        }
        if (StringUtils.hasText(request.getTeacherName())) {
            predicates.add(cb.like(teacherUserJoin.get("username"), "%" + request.getTeacherName().trim() + "%"));
        }
        if (StringUtils.hasText(request.getTeacherFullname())) {
            predicates.add(cb.like(teacherUserJoin.get("fullname"), "%" + request.getTeacherFullname().trim() + "%"));
        }
        if (StringUtils.hasText(request.getTiktokAccount())) {
            predicates.add(cb.like(tiktokJoin.get("tiktokAccount"), "%" + request.getTiktokAccount().trim() + "%"));
        }
        if (request.getPlatformId() != null) {
            predicates.add(cb.equal(tbUserJoin.get("userId"), request.getPlatformId()));
        }
        if (StringUtils.hasText(request.getInviterCode())) {
            predicates.add(cb.like(tbUserJoin.get("inviterCode"), "%" + request.getInviterCode().trim() + "%"));
        }
        if (StringUtils.hasText(request.getInvitationCode())) {
            predicates.add(cb.like(tbUserJoin.get("invitationCode"), "%" + request.getInvitationCode().trim() + "%"));
        }
        if (request.getInvitationType() != null) {
            predicates.add(cb.equal(tbUserJoin.get("invitationType"), request.getInvitationType()));
        }
        if (StringUtils.hasText(request.getRegionName())) {
            predicates.add(cb.equal(root.get("regionName"), request.getRegionName().trim()));
        }
        if (StringUtils.hasText(request.getCurrencyName())) {
            predicates.add(cb.equal(root.get("currencyName"), request.getCurrencyName().trim()));
        }
        if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), request.getStatus()));
        }
        if (request.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), request.getCreatedAfter()));
        }
        if (request.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), request.getCreatedBefore()));
        }

        return predicates;
    }

    private Map<Long, List<ApplicationPaymentRecordDTO>> fetchAllPaymentRecords(List<Long> userIds) {
        String jpql = """
                    SELECT new com.rsmanager.dto.user.ApplicationPaymentRecordDTO(
                        u.userId, p.regionName, p.currencyName, cr.rate, p.projectName, p.projectAmount,
                        p.paymentMethod, p.paymentAmount, p.fee, p.actual, p.paymentDate, apr.currencyName, mcr.rate
                    )
                    FROM BackendUser u
                    JOIN u.applicationProcessRecordAsUser apr
                    JOIN apr.applicationPaymentRecords p
                    JOIN UsdRate cr WITH cr.date = p.paymentDate AND cr.currencyCode = p.currencyCode
                    JOIN UsdRate mcr WITH mcr.date = p.paymentDate AND mcr.currencyCode = apr.currencyCode
                    WHERE u.userId IN :userIds
                    """;;
        List<ApplicationPaymentRecordDTO> details = entityManager.createQuery(jpql, ApplicationPaymentRecordDTO.class)
                                                        .setParameter("userIds", userIds)
                                                        .getResultList();

        // 分组映射
        return details.stream().collect(Collectors.groupingBy(ApplicationPaymentRecordDTO::getUserId));
    }

    /**
     * Helper方法：获取所有下级用户IDs（Managers）
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
    
            // 添加到已访问和下级 ID 集合中
            visited.addAll(directSubordinateIds);
            subordinateIds.addAll(directSubordinateIds);
    
            // 更新 managerIds 为下一层的下级 IDs
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

    /**
     * Helper方法：获取邀请用户数量
     */
    private Integer getInviteCount(Long inviterId) {
        String jpql = "SELECT COUNT(r) FROM InviterRelationship r WHERE r.inviter.userId = :inviterId AND r.status = true";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("inviterId", inviterId);
        Long result = query.getSingleResult();
        return result != null ? result.intValue() : 0;
    }

    /**
     * Helper方法：获取角色权限关系
     */
    private List<RolePermissionRelationshipDTO> getRolePermissionRelationships(Long userId) {
        String jpql = """
                    SELECT new com.rsmanager.dto.user.RolePermissionRelationshipDTO(
                    r.recordId, r.roleId, r.roleName, r.permissionId, r.permissionName,
                    r.rate1, r.rate2, r.startDate, r.endDate, r.status)
                    FROM RolePermissionRelationship r WHERE r.user.userId = :userId
                    """;
        return entityManager.createQuery(jpql, RolePermissionRelationshipDTO.class)
                            .setParameter("userId", userId)
                            .getResultList();
    }
}
