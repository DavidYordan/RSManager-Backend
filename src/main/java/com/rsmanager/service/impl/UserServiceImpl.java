package com.rsmanager.service.impl;

import com.rsmanager.dto.api.ServiceResponseDTO;
import com.rsmanager.dto.user.*;
import com.rsmanager.dto.user.OwnerSummaryDTO.GrowthDataDTO;
import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.service.UserService;

import com.rsmanager.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final BackendUserRepository backendUserRepository;
    private final InviterRelationshipRepository inviterRelationshipRepository;
    private final LocalInviteRepository localInviteRepository;
    private final LocalTbUserRepository localTbUserRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UsdRateRepository usdRateRepository;
    private final TikTokRelationshipRepository tikTokRelationshipRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public OwnerSummaryDTO getOwnerSummary(YearMonth selectedMonth) {
        Long userId = authService.getOperatorId();

        Page<SearchUsersResponseDTO> searchResult = searchUsers(SearchUsersDTO.builder()
                .userId(userId)
                .page(0)
                .size(500)
                .build());

        if (searchResult.getContent().isEmpty()) {
            return OwnerSummaryDTO.builder().build();
        }

        return calculateOwnerSummaryDTO(searchResult.getContent().get(0), selectedMonth);
    }

    // 只统计当前月份的数据
    private OwnerSummaryDTO calculateOwnerSummaryDTO(SearchUsersResponseDTO responseDTO, YearMonth selectedMonth) {
        LocalDate startOfMonth = selectedMonth.atDay(1);
        LocalDate endOfMonth = selectedMonth.atEndOfMonth();
    
        // 存储历史总收益和付款人数，按货币分组
        Map<String, Double> historyTotalProfitByCurrency = new HashMap<>();
        Map<String, Integer> historyTotalInvitesByCurrency = new HashMap<>();
        Map<String, Set<String>> userFullnamesByCurrency = new HashMap<>();
        Map<String, List<ProfitDTO>> profitsByCurrency = new HashMap<>();
    
        List<ProfitDTO> allProfits = new ArrayList<>();
        allProfits.addAll(responseDTO.getProfits1());
        allProfits.addAll(responseDTO.getProfits2());
    
        for (ProfitDTO profit : allProfits) {
            String currencyName = profit.getCurrencyName();
    
            if (profit.getPaymentDate().isBefore(startOfMonth)) {
                // 更新历史总收益
                historyTotalProfitByCurrency.put(currencyName,
                    historyTotalProfitByCurrency.getOrDefault(currencyName, 0.0) + profit.getProfit());
    
                // 更新历史付款人数
                userFullnamesByCurrency.computeIfAbsent(currencyName, k -> new HashSet<>());
                Set<String> userFullnames = userFullnamesByCurrency.get(currencyName);
                if (userFullnames.add(profit.getUserFullname())) {
                    historyTotalInvitesByCurrency.put(currencyName,
                        historyTotalInvitesByCurrency.getOrDefault(currencyName, 0) + 1);
                }
            } else if (!profit.getPaymentDate().isAfter(endOfMonth)) {
                // 当前月份的收益，按货币分组
                profitsByCurrency.computeIfAbsent(currencyName, k -> new ArrayList<>()).add(profit);
            }
        }
    
        // 构建按货币分组的收益和增长数据
        List<OwnerSummaryDTO.CurrencyProfitData> currencyProfits = new ArrayList<>();
    
        for (Map.Entry<String, List<ProfitDTO>> entry : profitsByCurrency.entrySet()) {
            String currencyName = entry.getKey();
            List<ProfitDTO> profitList = entry.getValue();
    
            // 获取历史数据
            Double historyTotalProfit = historyTotalProfitByCurrency.getOrDefault(currencyName, 0.0);
            Integer historyTotalInvites = historyTotalInvitesByCurrency.getOrDefault(currencyName, 0);
            Set<String> userFullnames = userFullnamesByCurrency.getOrDefault(currencyName, new HashSet<>());
    
            // 计算增长数据
            List<GrowthDataDTO> growthData = computeGrowthData(profitList, selectedMonth, historyTotalProfit, historyTotalInvites, userFullnames);
    
            // 构建 CurrencyProfitData
            OwnerSummaryDTO.CurrencyProfitData currencyProfitData = OwnerSummaryDTO.CurrencyProfitData.builder()
                .currencyName(currencyName)
                .profits(profitList)
                .growthDatas(growthData)
                .build();
    
            currencyProfits.add(currencyProfitData);
        }
    
        return OwnerSummaryDTO.builder()
                .username(responseDTO.getUsername())
                .regionName(responseDTO.getRegionName())
                // .platformTotalRevenue(responseDTO.getPlatformTotalRevenue())
                // .platformRevenueBalance(responseDTO.getPlatformRevenueBalance())
                // .platformTotalWithdrawal(responseDTO.getPlatformTotalWithdrawal())
                // .platformMoney(responseDTO.getPlatformMoney())
                // .inviteCount(responseDTO.getInviteCount())
                // .platformInviteCount(responseDTO.getPlatformInviteCount())
                .currencyProfits(currencyProfits)
                .build();
    }
    

    /**
     * 计算增长数据，按天汇总利润和邀请人数
     */
    private List<GrowthDataDTO> computeGrowthData(
            List<ProfitDTO> profitDTOs,
            YearMonth selectedMonth,
            Double historyTotalProfit,
            Integer historyTotalInvites,
            Set<String> existingUserFullnames) {

        List<GrowthDataDTO> growthDataList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 按支付日期排序
        profitDTOs.sort(Comparator.comparing(ProfitDTO::getPaymentDate));

        double cumulativeProfit = historyTotalProfit;
        int cumulativeInvites = historyTotalInvites;

        int currentProfitIndex = 0;
        int totalProfits = profitDTOs.size();

        for (int day = 1; day <= selectedMonth.lengthOfMonth(); day++) {
            LocalDate date = selectedMonth.atDay(day);
            String formattedDate = date.format(formatter);

            while (currentProfitIndex < totalProfits &&
                    profitDTOs.get(currentProfitIndex).getPaymentDate().equals(date)) {
                ProfitDTO profit = profitDTOs.get(currentProfitIndex);
                if (profit.getProfit() != null) {
                    cumulativeProfit += profit.getProfit();
                }
                if (profit.getUserFullname() != null && !existingUserFullnames.contains(profit.getUserFullname())) {
                    cumulativeInvites += 1;
                    existingUserFullnames.add(profit.getUserFullname());
                }
                currentProfitIndex++;
            }

            GrowthDataDTO growthData = GrowthDataDTO.builder()
                    .date(formattedDate)
                    .profit(cumulativeProfit)
                    .invites(cumulativeInvites)
                    .build();

            growthDataList.add(growthData);
        }

        return growthDataList;
    }


    /**
     * 强制创建用户
     *
     * @param request
     * @param authentication
     * @return
     */
    @Transactional
    @Override
    public ServiceResponseDTO superCreateUser(SuperCreateUserDTO request) {

        String username = request.getUsername().trim();
        if (backendUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("user '" + username + "' is already exists.");
        }

        Long operatorId = authService.getOperatorId();
        LocalDate startDate = request.getStartDate();
        Integer roleId = request.getRoleId();

        BackendUser newUser = BackendUser.builder()
                .username(username)
                .password(passwordEncoder.encode("123456"))
                .fullname(request.getFullname())
                .regionName(request.getRegionName())
                .currencyName(request.getCurrencyName())
                .status(true)
                .build();

        newUser.getCreaterRelationships().add(CreaterRelationship.builder()
                .user(newUser)
                .creater(backendUserRepository.findById(operatorId)
                        .orElseThrow(() -> new NoSuchElementException("Creater not found.")))
                .startDate(startDate)
                .build());

        if (request.getInviterId() != null) {
            newUser.getInviterRelationships().add(InviterRelationship.builder()
                    .user(newUser)
                    .inviter(backendUserRepository.findById(request.getInviterId())
                            .orElseThrow(() -> new NoSuchElementException("Inviter not found.")))
                    .startDate(startDate)
                    .status(true)
                    .createrId(operatorId)
                    .build());
        }

        if (request.getManagerId() != null) {
            newUser.getManagerRelationships().add(ManagerRelationship.builder()
                    .user(newUser)
                    .manager(backendUserRepository.findById(request.getManagerId())
                            .orElseThrow(() -> new NoSuchElementException("Manager not found.")))
                    .startDate(startDate)
                    .status(true)
                    .createrId(operatorId)
                    .build());
        }

        if (StringUtils.hasText(request.getTiktokAccount())) {
            newUser.getTiktokRelationships().add(TiktokRelationship.builder()
                    .user(newUser)
                    .tiktokAccount(request.getTiktokAccount())
                    .startDate(startDate)
                    .status(true)
                    .createrId(operatorId)
                    .build());
        }

        backendUserRepository.save(newUser);

        createRoleAndPermissions(newUser, roleId, startDate);

        return ServiceResponseDTO.builder()
                .success(true)
                .message("User created successfully.")
                .build();
    }

    /**
     * 查找用户
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<FindUserDTO> findUser(FindUserDTO request) {
        Optional<BackendUser> userOptional = Optional.empty();

        if (request.getUserId() != null) {
            userOptional = backendUserRepository.findById(request.getUserId());
        } else if (StringUtils.hasText(request.getUsername())) {
            userOptional = backendUserRepository.findByUsername(request.getUsername().trim());
        } else if (StringUtils.hasText(request.getFullname())) {
            userOptional = backendUserRepository.findByFullname(request.getFullname());
        }

        return userOptional.map(user -> new FindUserDTO(user.getUserId(), user.getUsername(), user.getFullname()));
    }

    /**
     * 根据用户名获取用户ID
     */
    @Transactional(readOnly = true)
    @Override
    public Long findIdByUsername(String username) {
        if (StringUtils.hasText(username)) {
            return backendUserRepository.findByUsername(username.trim())
                .map(BackendUser::getUserId)
                .orElse(null);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BackendUser> findByUserId(Long userId) {
        return backendUserRepository.findById(userId);
    }

    /**
     * 检查用户是否存在
     *
     * @param username
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Boolean userExists(String username) {
        if (StringUtils.hasText(username)) {
            return backendUserRepository.findByUsername(username.trim()).isPresent();
        } else {
            return false;
        }
    }

    /**
     * 重置用户密码
     */
    @Transactional
    @Override
    public ServiceResponseDTO resetPassword(BackendUserResetPasswordDTO request) {
        BackendUser currentUser = backendUserRepository.findById(authService.getOperatorId())
                .orElseThrow(() -> new NoSuchElementException("Current user not found."));

        if (request.getOldPassword() != null && !passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        backendUserRepository.save(currentUser);

        return ServiceResponseDTO.builder()
                .success(true)
                .message("Password reset successfully.")
                .build();
    }

    /**
     * 更新用户信息
     */
    @Transactional
    @Override
    public Boolean updateUser(BackendUserUpdateDTO request) {

        BackendUser backendUser = backendUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Current user not found."));

        if (!(authService.getOperatorRoleId() == 1 || isManager(authService.getOperatorId(), backendUser.getManagerRelationships().stream()
                .filter(mr -> mr.getEndDate() == null)
                .findFirst()
                .get()
                .getManager()))) {
            throw new IllegalStateException("You cannot update this user.");
        }

        LocalDate startDate = request.getStartDate();

        // 更新用户信息
        if (StringUtils.hasText(request.getFullname())) {
            backendUser.setFullname(request.getFullname().trim());
        }

        // 更新 tiktok
        String tiktokAccountString = request.getTiktokAccount();
        if (StringUtils.hasText(tiktokAccountString)) {
            handleTiktokChange(backendUser, tiktokAccountString.trim());
        }

        // 更新 inviter
        Long inviterId = request.getInviterId();
        if (inviterId != null) {
            handleInviterChange(backendUser, inviterId, startDate);
        }

        // 更新 manager
        Long managerId = request.getManagerId();
        if (managerId != null) {
            handleManagerChange(backendUser, managerId, startDate);
        }

        // 更新 teacher
        Long teacherId = request.getTeacherId();
        if (teacherId != null) {
            handleTeacherChange(backendUser, teacherId, startDate);
        }

        if (request.getStatus() != null) {
            backendUser.setStatus(request.getStatus());
        }

        return true;
    }

    /**
     * 根据查询条件搜索用户，支持分页
     */
    @Transactional(readOnly = true)
    @Override
    public Page<SearchUsersResponseDTO> searchUsers(SearchUsersDTO request) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("userId").ascending());


        Page<SearchUsersResponseDTO> resultPage = backendUserRepository.searchUsers(request, pageable);

        return resultPage;
    }

    /**
     * 根据查询条件搜索用户，支持分页
     */
    @Transactional(readOnly = true)
    @Override
    public Page<OldSearchUsersResponseDTO> oldSearchUsers(SearchUsersDTO searchDTO) {

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), Sort.by("userId").ascending());

        Specification<BackendUser> spec = buildSpecification(searchDTO);

        Page<BackendUser> resultPage = backendUserRepository.findAll(spec, pageable);

        List<OldSearchUsersResponseDTO> dtoList = resultPage.getContent().stream()
            .map(searchUser -> {

                OldSearchUsersResponseDTO searchResponseDTO = mapToDTO(searchUser);

                List<ProfitDTO> profits1 = new ArrayList<>();
                List<ProfitDTO> profits2 = new ArrayList<>();

                List<RolePermissionRelationship> rolePermissionRelationships = searchUser.getRolePermissionRelationships();
        
                if (!hasNecessaryPermissions(rolePermissionRelationships, 1)) {
                    searchResponseDTO.setProfits1(profits1);
                    searchResponseDTO.setProfits2(profits2);
                    return searchResponseDTO;
                }

                List<BackendUser> firstLevelInvitees = inviterRelationshipRepository.findUserByInviterId(searchUser.getUserId());

                calculateProfits(searchUser, firstLevelInvitees, profits1, 1);

                if (!hasNecessaryPermissions(rolePermissionRelationships, 2)) {
                    searchResponseDTO.setProfits1(profits1);
                    searchResponseDTO.setProfits2(profits2);
                    return searchResponseDTO;
                }

                List<BackendUser> secondLevelInvitees = inviterRelationshipRepository.findUserByInviterIds(firstLevelInvitees.stream()
                    .map(BackendUser::getUserId)
                    .collect(Collectors.toSet()));

                calculateProfits(searchUser, secondLevelInvitees, profits2, 2);

                searchResponseDTO.setProfits1(profits1);
                searchResponseDTO.setProfits2(profits2);

                return searchResponseDTO;
            })
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, resultPage.getTotalElements());
    }

    private Specification<BackendUser> buildSpecification(SearchUsersDTO request) {

        return (Root<BackendUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
        
            Long operatorId = authService.getOperatorId();
            Integer operatorRoleId = authService.getOperatorRoleId();

            Set<Long> allowedUserIds = new HashSet<>();
            if (operatorRoleId == 1 || operatorRoleId == 8) {
            } else if (operatorRoleId == 2 || operatorRoleId == 3 || operatorRoleId == 4 || operatorRoleId == 5) {
                allowedUserIds = getAllSubordinateIds(new HashSet<>(Collections.singleton(operatorId)));
                predicates.add(cb.in(root.get("userId")).value(allowedUserIds));
            } else {
                throw new IllegalStateException("You do not have permission to search users.");
            }

            Join<BackendUser, TbUser> tbuserJoin = root.join("tbUser", JoinType.LEFT);
            tbuserJoin.join("inviteMoney", JoinType.LEFT);
            tbuserJoin.join("userMoney", JoinType.LEFT);
            tbuserJoin.join("userIntegral", JoinType.LEFT);
            Join<BackendUser, RolePermissionRelationship> rolePermissionRelationshipJoin = root.join("rolePermissionRelationships", JoinType.LEFT);
            Join<BackendUser, TiktokRelationship> tiktokJoin = root.join("tiktokRelationships", JoinType.LEFT);
            tiktokJoin.join("tiktokUserDetails", JoinType.LEFT);
            Join<BackendUser, InviterRelationship> inviterJoin = root.join("inviterRelationships", JoinType.LEFT);
            Join<BackendUser, ManagerRelationship> managerJoin = root.join("managerRelationships", JoinType.LEFT);
            Join<BackendUser, TeacherRelationship> teacherJoin = root.join("teacherRelationships", JoinType.LEFT);
            
            Join<BackendUser, ApplicationProcessRecord> applicationProcessRecordJoin = root.join("applicationProcessRecordAsUser", JoinType.LEFT);
            applicationProcessRecordJoin.join("applicationPaymentRecords", JoinType.LEFT);

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
                predicates.add(cb.equal(rolePermissionRelationshipJoin.get("roleId"), request.getRoleId()));
            }
            if (request.getCreaterId() != null) {
                predicates.add(cb.equal(inviterJoin.get("creater").get("userId"), request.getCreaterId()));
            }
            if (StringUtils.hasText(request.getCreaterName())) {
                predicates.add(cb.like(inviterJoin.get("creater").get("username"), "%" + request.getCreaterName().trim() + "%"));
            }
            if (StringUtils.hasText(request.getCreaterFullname())) {
                predicates.add(cb.like(inviterJoin.get("creater").get("fullname"), "%" + request.getCreaterFullname().trim() + "%"));
            }
            if (request.getInviterId() != null || StringUtils.hasText(request.getInviterName()) || StringUtils.hasText(request.getInviterFullname())) {
                if (request.getInviterId() != null) {
                    predicates.add(cb.equal(inviterJoin.get("inviter").get("userId"), request.getInviterId()));
                }
                if (StringUtils.hasText(request.getInviterName())) {
                    predicates.add(cb.like(inviterJoin.get("inviter").get("username"), "%" + request.getInviterName().trim() + "%"));
                }
                if (StringUtils.hasText(request.getInviterFullname())) {
                    predicates.add(cb.like(inviterJoin.get("inviter").get("fullname"), "%" + request.getInviterFullname().trim() + "%"));
                }
                predicates.add(cb.isNull(inviterJoin.get("endDate")));
            }
            if (request.getInviterNotExists() != null) {
                if (request.getInviterNotExists()) {
                    predicates.add(cb.isNull(cb.isNull(inviterJoin.get("endDate"))));
                } else {
                    predicates.add(cb.isNotNull(cb.isNull(inviterJoin.get("endDate"))));
                }
            }
            if (request.getManagerId() != null || StringUtils.hasText(request.getManagerName()) || StringUtils.hasText(request.getManagerFullname())) {
                if (request.getManagerId() != null) {
                    predicates.add(cb.equal(managerJoin.get("manager").get("userId"), request.getManagerId()));
                }
                if (StringUtils.hasText(request.getManagerName())) {
                    predicates.add(cb.like(managerJoin.get("manager").get("username"), "%" + request.getManagerName().trim() + "%"));
                }
                if (StringUtils.hasText(request.getManagerFullname())) {
                    predicates.add(cb.like(managerJoin.get("manager").get("fullname"), "%" + request.getManagerFullname().trim() + "%"));
                }
                predicates.add(cb.isNull(managerJoin.get("endDate")));
            }
            if (request.getTeacherId() != null || StringUtils.hasText(request.getTeacherName()) || StringUtils.hasText(request.getTeacherFullname())) {
                if (request.getTeacherId() != null) {
                    predicates.add(cb.equal(teacherJoin.get("teacher").get("userId"), request.getTeacherId()));
                }
                if (StringUtils.hasText(request.getTeacherName())) {
                    predicates.add(cb.like(teacherJoin.get("teacher").get("username"), "%" + request.getTeacherName().trim() + "%"));
                }
                if (StringUtils.hasText(request.getTeacherFullname())) {
                    predicates.add(cb.like(teacherJoin.get("teacher").get("fullname"), "%" + request.getTeacherFullname().trim() + "%"));
                }
                predicates.add(cb.isNull(teacherJoin.get("endDate")));
            }
            if (request.getPlatformId() != null) {
                predicates.add(cb.equal(tbuserJoin.get("userId"), request.getPlatformId()));
            }
            if (StringUtils.hasText(request.getTiktokAccount())) {
                predicates.add(cb.like(tiktokJoin.get("tiktokAccount"), "%" + request.getTiktokAccount().trim() + "%"));
            }
            if (StringUtils.hasText(request.getInviterCode())) {
                predicates.add(cb.like(tbuserJoin.get("inviterCode"), "%" + request.getInviterCode().trim() + "%"));
            }
            if (StringUtils.hasText(request.getInvitationCode())) {
                predicates.add(cb.like(tbuserJoin.get("invitationCode"), "%" + request.getInvitationCode().trim() + "%"));
            }
            if (request.getInvitationType() != null) {
                predicates.add(cb.equal(tbuserJoin.get("invitationType"), request.getInvitationType()));
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Helper method to check if inviter has necessary permissions
    private boolean hasNecessaryPermissions(
        List<RolePermissionRelationship> rolePermissionRelationships,
        int level
    ) {
        boolean hasPermissionLevel = rolePermissionRelationships.stream()
            .anyMatch(rel -> rel.getPermissionId() == level && rel.getStatus());

        if (!hasPermissionLevel) {
            return false;
        }

        return true;
    }

    // Helper method to find rate
    private Map<Integer, Double> findRates(List<RolePermissionRelationship> rolePermissionRelationships, LocalDate paymentDate, int level) {
        return rolePermissionRelationships.stream()
            .filter(rel -> rel.getPermissionId() == level && rel.getStatus()
                && (rel.getStartDate().isBefore(paymentDate) || rel.getStartDate().isEqual(paymentDate))
                && (rel.getEndDate() == null || rel.getEndDate().isAfter(paymentDate) || rel.getEndDate().isEqual(paymentDate)))
            .findFirst()
            .map(rel -> Map.of(rel.getRoleId(), rel.getRate1() * rel.getRate2()))
            .orElse(null);
    }

    /**
     * Helper方法：获取所有下级用户IDs（Managers）
     */
    public Set<Long> getAllSubordinateIds(Set<Long> managerIds) {

        Set<Long> visited = managerIds;
        Set<Long> subordinateIds = managerIds;
    
        while (!managerIds.isEmpty()) {
            Set<Long> directSubordinateIds = getSubordinateIds(managerIds, visited);
            
            visited.addAll(directSubordinateIds);
            subordinateIds.addAll(directSubordinateIds);
    
            managerIds = directSubordinateIds;
        }
    
        return subordinateIds;
    }

    /**
     * Helper方法：获取直接下属用户IDs（Managers）
     */
    public Set<Long> getSubordinateIds(Set<Long> managerIds, Set<Long> visited) {

        Set<Long> directSubordinateIds = new HashSet<>(backendUserRepository.findUserIdsByManagerIds(managerIds));
        
        directSubordinateIds.removeAll(visited);
        
        return directSubordinateIds;
    }

    /**
     * 判断用户是否是manager
     * 
     * @param Long userId
     * @param BackendUser manager
     * @return Boolean
     */
    private Boolean isManager(Long userId, BackendUser manager) {
        while (manager != null) {
            if (userId.equals(manager.getUserId())) {
                return true;
            }
            manager = manager.getManagerRelationshipAsManagers().stream()
                .filter(r -> r.getStatus())
                .map(ManagerRelationship::getManager)
                .findFirst()
                .orElse(null);
        }
        return false;
    }

    /**
     * 处理Tiktok账号变更
     */
    private void handleTiktokChange(BackendUser user, String tiktokAccountStr) {

        List<TiktokRelationship> existingRelationships = user.getTiktokRelationships();
        logger.info("existingRelationships: {}", existingRelationships.size());

        // 检查Tiktok账号是否已存在
        if (tikTokRelationshipRepository.isTiktokAccountExists(tiktokAccountStr)) {
            throw new IllegalArgumentException("Tiktok account already exists.");
        }

        LocalDate startDate = LocalDate.now();

        // 移除同一天的旧关系
        existingRelationships.removeIf(rel -> rel.getStartDate().equals(startDate) && rel.getEndDate() == null);

        // 关闭未结束的旧关系
        for (TiktokRelationship rel : existingRelationships) {
            if (rel.getEndDate() == null) {
                if (rel.getStartDate().isBefore(startDate)) {
                    rel.setEndDate(startDate.minusDays(1));
                    rel.setStatus(false);
                } else {
                    throw new RuntimeException("更新Tiktoker关系时发生时间段重叠");
                }
            }
        }

        // 添加新关系
        TiktokRelationship newRelationship = TiktokRelationship.builder()
            .user(user)
            .tiktokAccount(tiktokAccountStr)
            .startDate(startDate)
            .status(true)
            .createrId(authService.getOperatorId())
            .build();
        user.getTiktokRelationships().add(newRelationship);

        backendUserRepository.save(user);
    }

    /**
     * 处理邀请人变更
     */
    private void handleInviterChange(BackendUser user, Long inviterId, LocalDate startDate) {

        List<InviterRelationship> existingRelationships = user.getInviterRelationships();

        BackendUser inviter = backendUserRepository.findById(inviterId)
                .orElseThrow(() -> new NoSuchElementException("Inviter not found."));

        // 移除同一天的旧关系
        existingRelationships.removeIf(rel -> rel.getStartDate().equals(startDate) && rel.getEndDate() == null);

        // 关闭未结束的旧关系
        for (InviterRelationship rel : existingRelationships) {
            if (rel.getEndDate() == null) {
                if (rel.getStartDate().isBefore(startDate)) {
                    rel.setEndDate(startDate.minusDays(1));
                    rel.setStatus(false);
                } else {
                    throw new RuntimeException("更新邀请人关系时发生时间段重叠");
                }
            }
        }

        // 添加新关系
        InviterRelationship newRelationship = InviterRelationship.builder()
            .user(user)
            .inviter(inviter)
            .startDate(startDate)
            .status(true)
            .createrId(authService.getOperatorId())
            .build();

        user.getInviterRelationships().add(newRelationship);

        backendUserRepository.save(user);
    }

    /**
     * 处理Manager变更
     */
    private void handleManagerChange(BackendUser user, Long managerId, LocalDate startDate) {

        List<ManagerRelationship> existingRelationships = user.getManagerRelationships();

        BackendUser manager = backendUserRepository.findById(managerId)
                .orElseThrow(() -> new NoSuchElementException("Manager not found."));

        // 移除同一天的旧关系
        existingRelationships.removeIf(rel -> rel.getStartDate().equals(startDate) && rel.getEndDate() == null);

        // 关闭未结束的旧关系
        for (ManagerRelationship rel : existingRelationships) {
            if (rel.getEndDate() == null) {
                if (rel.getStartDate().isBefore(startDate)) {
                    rel.setEndDate(startDate.minusDays(1));
                    rel.setStatus(false);
                } else {
                    throw new RuntimeException("更新Manager关系时发生时间段重叠");
                }
            }
        }

        // 添加新关系
        ManagerRelationship newRelationship = ManagerRelationship.builder()
            .user(user)
            .manager(manager)
            .startDate(startDate)
            .status(true)
            .createrId(authService.getOperatorId())
            .build();

        user.getManagerRelationships().add(newRelationship);

        backendUserRepository.save(user);
    }

    /**
     * 处理Teacher变更
     */
    private void handleTeacherChange(BackendUser user, Long teacherId, LocalDate startDate) {

        List<TeacherRelationship> existingRelationships = user.getTeacherRelationships();

        BackendUser teacher = backendUserRepository.findById(teacherId)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found."));

        // 移除同一天的旧关系
        existingRelationships.removeIf(rel -> rel.getStartDate().equals(startDate) && rel.getEndDate() == null);

        // 关闭未结束的旧关系
        for (TeacherRelationship rel : existingRelationships) {
            if (rel.getEndDate() == null) {
                if (rel.getStartDate().isBefore(startDate)) {
                    rel.setEndDate(startDate.minusDays(1));
                    rel.setStatus(false);
                } else {
                    throw new RuntimeException("更新Teacher关系时发生时间段重叠");
                }
            }
        }

        // 添加新关系
        TeacherRelationship newRelationship = TeacherRelationship.builder()
            .user(user)
            .teacher(teacher)
            .startDate(startDate)
            .status(true)
            .createrId(authService.getOperatorId())
            .build();

        user.getTeacherRelationships().add(newRelationship);

        backendUserRepository.save(user);
    }

    /**
     * 新增角色 & 权限
     */
    private void createRoleAndPermissions(BackendUser user, Integer roleId, LocalDate startDate) {

        List<RolePermission> rolePermissions = rolePermissionRepository.findByIdRoleId(roleId);

        if (rolePermissions.size() < 3) {
            throw new IllegalStateException("Not all RolePermissions found.");
        }

        for (RolePermission rp : rolePermissions) {
            Integer permissionId = rp.getId().getPermissionId();
            double rate1 = rp.getRate1();
            double rate2 = rp.getRate2();
            Boolean status = rp.getIsEnabled();


            RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                .user(user)
                .roleId(roleId)
                .roleName(rp.getRoleName())
                .permissionId(permissionId)
                .permissionName(rp.getPermissionName())
                .rate1(rate1)
                .rate2(rate2)
                .startDate(startDate)
                .status(status)
                .createrId(authService.getOperatorId())
                .build();

            user.getRolePermissionRelationships().add(rolePermissionRelationship);

            backendUserRepository.save(user);
        }
    }

    private void calculateProfits(BackendUser inviter, List<BackendUser> invitees, List<ProfitDTO> profits, int level) {

        invitees.stream().forEach(invitee -> {
            invitee.getApplicationProcessRecordAsUser().getApplicationPaymentRecords().stream()
                .forEach(payment -> {
                    List<RolePermissionRelationship> rolePermissionRelationships = inviter.getRolePermissionRelationships();
                    LocalDate paymentDate = payment.getPaymentDate();
                    Map<Integer, Double> inviterRoleRate = findRates(rolePermissionRelationships, paymentDate, level);
                    Integer inviterRoleId = inviterRoleRate == null ? 0 : inviterRoleRate.keySet().stream().findFirst().orElse(0);
                    Double rate = inviterRoleRate == null ? 0.0 : inviterRoleRate.values().stream().findFirst().orElse(0.0);
                    profits.add(ProfitDTO.builder()
                        .userFullname(invitee.getFullname())
                        .userRoleId(invitee.getRolePermissionRelationships().stream()
                            .filter(rel -> rel.getEndDate() == null)
                            .findFirst()
                            .map(rel -> rel.getRoleId())
                            .orElse(null))
                        .inviterFullname(inviter.getFullname())
                        .inviterRoleId(inviterRoleId)
                        .regionName(payment.getRegionName())
                        .currencyName(payment.getCurrencyName())
                        // .currencyRate(payment.getCurrencyRate())
                        .projectName(payment.getProjectName())
                        .projectAmount(payment.getProjectAmount())
                        .paymentMethod(payment.getPaymentMethod())
                        .paymentDate(paymentDate)
                        .paymentAmount(payment.getPaymentAmount())
                        .fee(payment.getFee())
                        .actual(payment.getActual())
                        .rate(rate)
                        .profit(Math.round(payment.getActual() * rate * 100.0) / 100.0)
                        .build());
                });
        });
    }

    private OldSearchUsersResponseDTO mapToDTO(BackendUser record) {
        return OldSearchUsersResponseDTO.builder()
                .userId(record.getUserId())
                .username(record.getUsername())
                .fullname(record.getFullname())
                .regionName(record.getRegionName())
                .currencyName(record.getCurrencyName())
                .inviteCount(inviterRelationshipRepository.countByInviterId(record.getUserId()))
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .status(record.getStatus())
                .createrDTO(
                    record.getCreaterRelationships().stream()
                        .filter(creater -> creater.getEndDate() == null)
                        .findFirst()
                        .map(creater -> OldSearchUsersResponseDTO.CreaterDTO.builder()
                            .userId(creater.getCreater().getUserId())
                            .username(creater.getCreater().getUsername())
                            .fullname(creater.getCreater().getFullname())
                            .build())
                        .orElse(null)
                )
                .inviterDTO(
                    record.getInviterRelationships().stream()
                        .filter(inviter -> inviter.getEndDate() == null)
                        .findFirst()
                        .map(inviter -> OldSearchUsersResponseDTO.InviterDTO.builder()
                            .userId(inviter.getInviter().getUserId())
                            .username(inviter.getInviter().getUsername())
                            .fullname(inviter.getInviter().getFullname())
                            .build())
                        .orElse(null)
                )
                .managerDTO(
                    record.getManagerRelationships().stream()
                        .filter(manager -> manager.getEndDate() == null)
                        .findFirst()
                        .map(manager -> OldSearchUsersResponseDTO.ManagerDTO.builder()
                            .userId(manager.getManager().getUserId())
                            .username(manager.getManager().getUsername())
                            .fullname(manager.getManager().getFullname())
                            .build())
                        .orElse(null)
                )
                .teacherDTO(
                    record.getTeacherRelationships().stream()
                        .filter(teacher -> teacher.getEndDate() == null)
                        .findFirst()
                        .map(teacher -> OldSearchUsersResponseDTO.TeacherDTO.builder()
                            .userId(teacher.getTeacher().getUserId())
                            .username(teacher.getTeacher().getUsername())
                            .fullname(teacher.getTeacher().getFullname())
                            .build())
                        .orElse(null)
                )
                .tbUserDTO(
                    record.getTbUser() != null ? OldSearchUsersResponseDTO.TbUserDTO.builder()
                        .userId(record.getTbUser().getUserId())
                        .inviterCode(record.getTbUser().getInviterCode())
                        .invitationCode(record.getTbUser().getInvitationCode())
                        .invitationType(record.getTbUser().getInvitationType())
                        .moneySum(record.getTbUser().getInviteMoney() != null ? record.getTbUser().getInviteMoney().getMoneySum() : 0)
                        .money(record.getTbUser().getInviteMoney() != null ? record.getTbUser().getInviteMoney().getMoney() : 0)
                        .cashOut(record.getTbUser().getInviteMoney() != null ? record.getTbUser().getInviteMoney().getCashOut() : 0)
                        .userMoney(record.getTbUser().getUserMoney() != null ? record.getTbUser().getUserMoney().getMoney() : 0)
                        .userIntegral(record.getTbUser().getUserIntegral() != null ? record.getTbUser().getUserIntegral().getIntegralNum() : 0)
                        .inviteCount(localTbUserRepository.countByInviterCode(record.getTbUser().getInvitationCode()))
                        .inviteDailyMoneySumDTOs(localInviteRepository.findDailyMoneySumByUserId(record.getTbUser().getUserId()))
                        .build() : null
                )
                .tiktokAccountDTO(
                    record.getTiktokRelationships().stream()
                        .filter(tiktok -> tiktok.getStatus())
                        .map(tiktok -> TiktokAccountDTO.builder()
                            .tiktokAccount(tiktok.getTiktokAccount())
                            .tiktokId(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getTiktokId() : null)
                            .diggCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getDiggCount() : 0)
                            .followerCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getFollowerCount() : 0)
                            .followingCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getFollowingCount() : 0)
                            .friendCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getFriendCount() : 0)
                            .heartCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getHeartCount() : 0)
                            .videoCount(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getVideoCount() : 0)
                            .updatedAt(tiktok.getTiktokUserDetails() != null ? tiktok.getTiktokUserDetails().getUpdatedAt() : null)
                            .build())
                        .findFirst()
                        .orElse(null)
                )
                .rolePermissionRelationshipDTOs(
                    record.getRolePermissionRelationships().stream()
                        .map(rolePermission -> RolePermissionRelationshipDTO.builder()
                            .roleId(rolePermission.getRoleId())
                            .roleName(rolePermission.getRoleName())
                            .permissionId(rolePermission.getPermissionId())
                            .permissionName(rolePermission.getPermissionName())
                            .rate1(rolePermission.getRate1())
                            .rate2(rolePermission.getRate2())
                            .startDate(rolePermission.getStartDate())
                            .endDate(rolePermission.getEndDate())
                            .status(rolePermission.getStatus())
                            .build())
                        .collect(Collectors.toList())
                )
                .applicationProcessRecordDTO(
                    Optional.ofNullable(record.getApplicationProcessRecordAsUser())
                        .map(user -> OldSearchUsersResponseDTO.ApplicationProcessRecordDTO.builder()
                            .processId(user.getProcessId())
                            .username(user.getUsername())
                            .fullname(user.getFullname())
                            .roleId(user.getRoleId())
                            .inviterName(user.getInviterName())
                            .rateA(user.getRateA())
                            .rateB(user.getRateB())
                            .startDate(user.getStartDate())
                            .regionName(user.getRegionName())
                            .currencyName(user.getCurrencyName())
                            .projectName(user.getProjectName())
                            .projectAmount(user.getProjectAmount())
                            .paymentMethod(user.getPaymentMethod())
                            .processStatus(user.getProcessStatus())
                            .build())
                        .orElse(null)
                )
                .applicationPaymentRecordDTOs(
                    Optional.ofNullable(record.getApplicationProcessRecordAsUser())
                        .map(user -> user.getApplicationPaymentRecords().stream()
                            .map(payment -> {
                                Optional<Double> rateOptional = usdRateRepository.findRateByDateAndCurrencyCode(
                                    payment.getPaymentDate(), payment.getCurrencyCode());

                                Double rate = rateOptional.orElseGet(() -> usdRateRepository.findRateByDateAndCurrencyCode(
                                    LocalDate.of(1970, 1, 1), payment.getCurrencyCode())
                                    .orElse(0.0));

                                return OldSearchUsersResponseDTO.ApplicationPaymentRecordDTO.builder()
                                    .regionName(payment.getRegionName())
                                    .currencyName(payment.getCurrencyName())
                                    .currencyCode(payment.getCurrencyCode())
                                    .projectName(payment.getProjectName())
                                    .projectAmount(payment.getProjectAmount())
                                    .paymentMethod(payment.getPaymentMethod())
                                    .paymentAmount(payment.getPaymentAmount())
                                    .fee(payment.getFee())
                                    .actual(payment.getActual())
                                    .paymentDate(payment.getPaymentDate())
                                    .rate(rate)
                                    .build();
                            })
                            .collect(Collectors.toList()))
                        .orElse(Collections.emptyList())
                )
                .build();
    }

}
