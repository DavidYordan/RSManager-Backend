package com.rsmanager.service.impl;

import com.rsmanager.dto.api.ServiceResponseDTO;
import com.rsmanager.dto.user.*;
import com.rsmanager.dto.user.OwnerSummaryDTO.GrowthDataDTO;
import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.service.UserService;
import com.rsmanager.service.AuthService;
import lombok.RequiredArgsConstructor;

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

    private final BackendUserRepository backendUserRepository;
    private final LocalInviteRepository localInviteRepository;
    private final LocalTbUserRepository localTbUserRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UsdRateRepository usdRateRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public OwnerSummaryDTO getOwnerSummary(YearMonth selectedMonth) {
        Long userId = authService.getOperatorId();

        Page<SearchResponseDTO> searchResult = searchUsers(BackendUserSearchDTO.builder()
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
    private OwnerSummaryDTO calculateOwnerSummaryDTO(SearchResponseDTO responseDTO, YearMonth selectedMonth) {
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
                if (userFullnames.add(profit.getFullname())) {
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
                .inviteCount(responseDTO.getInviteCount())
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
                if (profit.getFullname() != null && !existingUserFullnames.contains(profit.getFullname())) {
                    cumulativeInvites += 1;
                    existingUserFullnames.add(profit.getFullname());
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
            handleTiktokChange(backendUser, tiktokAccountString, startDate);
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
    public Page<SearchResponseDTO> searchUsers(BackendUserSearchDTO searchDTO) {

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), Sort.by("userId").ascending());

        Specification<BackendUser> spec = buildSpecification(searchDTO);

        Page<BackendUser> resultPage = backendUserRepository.findAll(spec, pageable);

        List<SearchResponseDTO> dtoList = mapToDTO(resultPage.getContent());

        return new PageImpl<>(dtoList, pageable, resultPage.getTotalElements());

        // Long total = searchUsersPage.getTotalElements();

        // logger.info("Total users: {}", total);

        // // Process each SearchUsersDTO to calculate profits
        // for (SearchUsersDTO searchUser : searchUsers) {
        //     // Initialize profits lists
        //     List<ProfitDTO> profits1 = new ArrayList<>();
        //     List<ProfitDTO> profits2 = new ArrayList<>();
    
        //     // Get inviter's relationships from SearchUsersDTO
        //     SearchResponseDTO searchResponseDTO = searchUser.getSearchResponseDTO();
        //     List<RoleRelationship> inviterRoleRelationships = searchUser.getRoleRelationships();
        //     List<PermissionRelationship> inviterPermissionRelationships = searchUser.getPermissionRelationships();
        //     List<InviterRelationship> inviterRelationships = searchUser.getInviterRelationships();
    
        //     // Check if inviter has active role and permission relationships
        //     if (!hasNecessaryPermissions(inviterPermissionRelationships, 1)) {
        //         searchResponseDTO.setProfits1(profits1);
        //         searchResponseDTO.setProfits2(profits2);
        //         continue;
        //     }
    
        //     // Get the user's inviter relationships (first-level invitees)
        //     List<InviterRelationship> firstLevelInvites = inviterRelationships;
    
        //     // Collect user IDs of first-level invitees
        //     Set<Long> firstLevelInviteeIds = firstLevelInvites.stream()
        //         .map(ir -> ir.getUser().getUserId())
        //         .collect(Collectors.toSet());
    
        //     // Map to store BackendUser data for invitees to avoid multiple fetches
        //     Map<Long, BackendUser> userCache = new HashMap<>();
    
        //     // Fetch first-level invitees
        //     List<BackendUser> firstLevelInvitees = fetchUsersByIds(firstLevelInviteeIds, userCache);

        //     // Calculate profits from first-level invitees
        //     profits1.addAll(calculateProfits(
        //         searchUser.getSearchResponseDTO(),
        //         firstLevelInvitees,
        //         userCache,
        //         1,
        //         inviterRoleRelationships,
        //         inviterPermissionRelationships,
        //         inviterRelationships
        //     ));

        //     // Check if inviter has active role and permission relationships
        //     if (!hasNecessaryPermissions(inviterPermissionRelationships, 2)) {
        //         searchResponseDTO.setProfits1(profits1);
        //         searchResponseDTO.setProfits2(profits2);
        //         continue;
        //     }

        //     // 去除firstLevelInvitees中roleId为4的用户
        //     Set<Long> firstLevelInviteeIdsWithoutRoleId4 = firstLevelInvitees.stream()
        //         .filter(user -> {
        //             List<RoleRelationship> roleRelationships = user.getRoleRelationships();
        //             return roleRelationships.stream().noneMatch(role -> 
        //                 role.getRole().getRoleId() == 4 && role.getStatus());
        //         })
        //         .map(BackendUser::getUserId)
        //         .collect(Collectors.toSet());

        //     // Fetch second-level invitees in one query
        //     List<InviterRelationship> secondLevelInvites = inviterRelationshipRepository.findAllByInviterUserIdIn(firstLevelInviteeIdsWithoutRoleId4);

        //     // Collect user IDs of second-level invitees
        //     Set<Long> secondLevelInviteeIds = secondLevelInvites.stream()
        //         .map(ir -> ir.getUser().getUserId())
        //         .collect(Collectors.toSet());

        //     // Fetch second-level invitees
        //     List<BackendUser> secondLevelInvitees = fetchUsersByIds(secondLevelInviteeIds, userCache);

        //     // Calculate profits from second-level invitees
        //     profits2.addAll(calculateProfits(
        //         searchUser.getSearchResponseDTO(),
        //         secondLevelInvitees,
        //         userCache,
        //         2,
        //         inviterRoleRelationships,
        //         inviterPermissionRelationships,
        //         secondLevelInvites
        //     ));

        //     // Set the profits in SearchResponseDTO
        //     searchResponseDTO.setProfits1(profits1);
        //     searchResponseDTO.setProfits2(profits2);

        //     // Check if inviterName is null and assign it if necessary
        //     if (searchResponseDTO.getInviterName() == null) {
        //         Long userId = searchUser.getSearchResponseDTO().getUserId();
        //         Optional<String> inviterName = applicationProcessRecordRepository.findInviterNameByUserId(userId);
        //         searchResponseDTO.setInviterName(inviterName.orElse(null));
        //     }
        // }

        // // Extract SearchResponseDTOs
        // List<SearchResponseDTO> searchResponseDTOs = searchUsers.stream()
        //     .map(SearchUsersDTO::getSearchResponseDTO)
        //     .collect(Collectors.toList());

        // return new PageImpl<>(searchResponseDTOs, pageable, total);
    }

    private Specification<BackendUser> buildSpecification(BackendUserSearchDTO request) {

        return (Root<BackendUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

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

            Join<BackendUser, TbUser> tbuserJoin = root.join("user", JoinType.LEFT);
            tbuserJoin.join("inviteMoney", JoinType.LEFT);
            tbuserJoin.join("userMoney", JoinType.LEFT);
            tbuserJoin.join("userIntegral", JoinType.LEFT);
            Join<BackendUser, RolePermissionRelationship> rolePermissionRelationshipJoin = root.join("rolePermissionRelationships", JoinType.LEFT);
            Join<BackendUser, TiktokRelationship> tiktokJoin = root.join("tiktokRelationships", JoinType.LEFT);
            tiktokJoin.join("tiktokUserDetails", JoinType.LEFT);
            Join<BackendUser, InviterRelationship> inviterJoin = root.join("inviterRelationships", JoinType.LEFT);
            Join<BackendUser, ManagerRelationship> managerJoin = root.join("managerRelationships", JoinType.LEFT);
            Join<BackendUser, TeacherRelationship> teacherJoin = root.join("teacherRelationships", JoinType.LEFT);
            
            Join<BackendUser, ApplicationProcessRecord> applicationProcessRecordJoin = root.join("applicationProcessRecords", JoinType.LEFT);
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
                predicates.add(cb.equal(inviterJoin.get("inviter").get("userId"), request.getCreaterId()));
            }
            if (StringUtils.hasText(request.getCreaterName())) {
                predicates.add(cb.like(inviterJoin.get("inviter").get("username"), "%" + request.getCreaterName().trim() + "%"));
            }
            if (StringUtils.hasText(request.getCreaterFullname())) {
                predicates.add(cb.like(inviterJoin.get("inviter").get("fullname"), "%" + request.getCreaterFullname().trim() + "%"));
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
            if (request.getInviterExists() != null) {
                if (request.getInviterExists()) {
                    predicates.add(cb.isNotNull(cb.isNull(inviterJoin.get("endDate"))));
                } else {
                    predicates.add(cb.isNull(cb.isNull(inviterJoin.get("endDate"))));
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
    // private boolean hasNecessaryPermissions(
    //     List<PermissionRelationship> permissionRelationships,
    //     int level
    // ) {
    //     boolean hasPermissionLevel = permissionRelationships.stream()
    //         .anyMatch(rel -> rel.getPermissionId() == level && rel.getStatus());

    //     if (!hasPermissionLevel) {
    //         return false;
    //     }

    //     return true;
    // }

    // // Helper method to fetch users and cache them
    // private List<BackendUser> fetchUsersByIds(Set<Long> userIds, Map<Long, BackendUser> userCache) {
    //     List<Long> idsToFetch = userIds.stream()
    //         .filter(id -> !userCache.containsKey(id))
    //         .collect(Collectors.toList());

    //     if (!idsToFetch.isEmpty()) {
    //         List<BackendUser> users = backendUserRepository.findAllById(idsToFetch);
    //         for (BackendUser user : users) {
    //             userCache.put(user.getUserId(), user);
    //         }
    //     }

    //     return userIds.stream()
    //         .map(userCache::get)
    //         .filter(Objects::nonNull)
    //         .collect(Collectors.toList());
    // }

    // // Method to calculate profits for a list of invitees
    // private List<ProfitDTO> calculateProfits(
    //     SearchResponseDTO inviterDTO,
    //     List<BackendUser> invitees,
    //     Map<Long, BackendUser> userCache,
    //     int level,
    //     List<RoleRelationship> inviterRoleRelationships,
    //     List<PermissionRelationship> inviterPermissionRelationships,
    //     List<InviterRelationship> inviterInviterRelationships
    // ) {
    //     List<ProfitDTO> profits = new ArrayList<>();

    //     for (BackendUser invitee : invitees) {
    //         // Fetch ApplicationProcessRecords for invitee
    //         List<ApplicationProcessRecord> processRecords = applicationProcessRecordRepository.findByUserId(invitee.getUserId());

    //         // Initialize total amounts for this invitee
    //         double totalProjectAmount = 0.0;
    //         double totalPaymentAmount = 0.0;

    //         for (ApplicationProcessRecord processRecord : processRecords) {
    //             // Fetch ApplicationPaymentRecords where status = true, ordered by paymentDate ascending
    //             List<ApplicationPaymentRecord> paymentRecords = processRecord.getApplicationPaymentRecords().stream()
    //                 .filter(payment -> payment.getStatus())
    //                 .sorted(Comparator.comparing(ApplicationPaymentRecord::getPaymentDate))
    //                 .collect(Collectors.toList());

    //             // Sum up projectAmount process_status in [5, 6, 7, 99, 100]
    //             if (processRecord.getProjectAmount() != null) {
    //                 if (processRecord.getProcessStatus() == 5 || processRecord.getProcessStatus() == 6
    //                         || processRecord.getProcessStatus() == 7 || processRecord.getProcessStatus() == 99
    //                         || processRecord.getProcessStatus() == 100) {
    //                     totalProjectAmount += processRecord.getProjectAmount();
    //                 }
    //             }

    //             for (ApplicationPaymentRecord payment : paymentRecords) {
    //                 if (payment.getPaymentAmount() != null) {
    //                     totalPaymentAmount += payment.getPaymentAmount();
    //                 }

    //                 LocalDate paymentDate = payment.getPaymentDate();

    //                 // Check if inviter relationship is active during payment date
    //                 boolean isInviterActive = isRelationshipActive(invitee.getInviterRelationships(), inviterDTO.getUserId(), paymentDate);

    //                 if (!isInviterActive) {
    //                     continue;
    //                 }

    //                 // Get inviter's active RoleRelationship and PermissionRelationship during payment date
    //                 RoleRelationship roleRel = getActiveRoleRelationship(inviterRoleRelationships, paymentDate);
    //                 PermissionRelationship permissionRel = getActivePermissionRelationship(inviterPermissionRelationships, paymentDate, level);

    //                 if (roleRel == null || permissionRel == null) {
    //                     continue;
    //                 }

    //                 // Calculate profit
    //                 double rate = permissionRel.getRate1() * permissionRel.getRate2();
    //                 double profitAmount = Math.round(payment.getActual() * rate * 100.0) / 100.0;

    //                 // Build ProfitDTO
    //                 ProfitDTO profitDTO = ProfitDTO.builder()
    //                     .fullname(invitee.getFullname())
    //                     .inviterFullname(inviterDTO.getFullname())
    //                     .roleId(roleRel.getRole().getRoleId())
    //                     .regionName(payment.getRegionName())
    //                     .currencyName(payment.getCurrencyName())
    //                     .projectName(payment.getProjectName())
    //                     .projectAmount(payment.getProjectAmount())
    //                     .paymentMethod(payment.getPaymentMethod())
    //                     .paymentDate(paymentDate)
    //                     .paymentAmount(payment.getPaymentAmount())
    //                     .fee(payment.getFee())
    //                     .actual(payment.getActual())
    //                     .rate(rate)
    //                     .profit(profitAmount)
    //                     .totalPaymentAmount(totalPaymentAmount)
    //                     .totalProjectAmount(totalProjectAmount)
    //                     .build();

    //                 profits.add(profitDTO);
    //             }
    //         }
    //     }

    //     return profits;
    // }

    // // Helper method to check if a relationship is active during a given date
    // private boolean isRelationshipActive(List<InviterRelationship> relationships, Long inviterId, LocalDate paymentDate) {
    //     for (InviterRelationship rel : relationships) {
    //         if (rel.getInviter().getUserId().equals(inviterId)
    //             && rel.getStatus()
    //             && (rel.getStartDate().isBefore(paymentDate) || rel.getStartDate().isEqual(paymentDate))
    //             && (rel.getEndDate() == null || rel.getEndDate().isAfter(paymentDate) || rel.getEndDate().isEqual(paymentDate))) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // // Helper method to get active RoleRelationship during a given date
    // private RoleRelationship getActiveRoleRelationship(List<RoleRelationship> roleRelationships, LocalDate paymentDate) {
    //     return roleRelationships.stream()
    //         .filter(rel -> rel.getStatus()
    //             && (rel.getStartDate().isBefore(paymentDate) || rel.getStartDate().isEqual(paymentDate))
    //             && (rel.getEndDate() == null || rel.getEndDate().isAfter(paymentDate) || rel.getEndDate().isEqual(paymentDate)))
    //         .sorted(Comparator.comparing(RoleRelationship::getStartDate).reversed())
    //         .findFirst()
    //         .orElse(null);
    // }
    

    // // Helper method to get active PermissionRelationship during a given date
    // private PermissionRelationship getActivePermissionRelationship(List<PermissionRelationship> permissionRelationships, LocalDate paymentDate, int level) {
    //     return permissionRelationships.stream()
    //         .filter(rel -> rel.getStatus()
    //             && rel.getPermissionId().equals(level)
    //             && (rel.getStartDate().isBefore(paymentDate) || rel.getStartDate().isEqual(paymentDate))
    //             && (rel.getEndDate() == null || rel.getEndDate().isAfter(paymentDate) || rel.getEndDate().isEqual(paymentDate)))
    //         .sorted(Comparator.comparing(PermissionRelationship::getStartDate).reversed())
    //         .findFirst()
    //         .orElse(null);
    // }

    // private Page<SearchUsersDTO> executeCustomQuery(BackendUserSearchDTO searchDTO, Set<Long> allowedUserIds, Pageable pageable) {
    //     CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    //     CriteriaQuery<SearchResponseDTO> query = criteriaBuilder.createQuery(SearchResponseDTO.class);
    //     Root<BackendUser> root = query.from(BackendUser.class);

    //     // Subqueries for role
    //     Join<BackendUser, RoleRelationship> roleJoin = root.join("roleRelationships", JoinType.LEFT);
    //     roleJoin.on(criteriaBuilder.equal(roleJoin.get("status"), true));
    //     Join<RoleRelationship, BackendRole> roleJoin2 = roleJoin.join("role", JoinType.LEFT);

    //     // Subqueries for creater
    //     Join<BackendUser, CreaterRelationship> createrJoin = root.join("createrRelationships", JoinType.LEFT);
    //     Join<CreaterRelationship, BackendUser> createrJoin2 = createrJoin.join("creater", JoinType.LEFT);

    //     // Subqueries for inviter
    //     Join<BackendUser, InviterRelationship> inviterJoin = root.join("inviterRelationships", JoinType.LEFT);
    //     inviterJoin.on(criteriaBuilder.equal(inviterJoin.get("status"), true));
    //     Join<InviterRelationship, BackendUser> inviterJoin2 = inviterJoin.join("inviter", JoinType.LEFT);

    //     // Subqueries to fetch fields from TbUser based on BackendUser.platformId
    //     Subquery<String> inviterCodeSubquery = query.subquery(String.class);
    //     Root<TbUser> tbUserRoot1 = inviterCodeSubquery.from(TbUser.class);
    //     inviterCodeSubquery.select(tbUserRoot1.get("inviterCode"))
    //         .where(criteriaBuilder.equal(tbUserRoot1.get("userId"), root.get("platformId")));

    //     // Subqueries for manager
    //     Join<BackendUser, ManagerRelationship> managerJoin = root.join("managerRelationships", JoinType.LEFT);
    //     managerJoin.on(criteriaBuilder.equal(managerJoin.get("status"), true));
    //     Join<ManagerRelationship, BackendUser> managerJoin2 = managerJoin.join("manager", JoinType.LEFT);

    //     // Subqueries for teacher
    //     Join<BackendUser, TeacherRelationship> teacherJoin = root.join("teacherRelationships", JoinType.LEFT);
    //     teacherJoin.on(criteriaBuilder.equal(teacherJoin.get("status"), true));
    //     Join<TeacherRelationship, BackendUser> teacherJoin2 = teacherJoin.join("teacher", JoinType.LEFT);

    //     // Subqueries for tiktok
    //     Join<BackendUser, TiktokRelationship> tiktokerJoin = root.join("tiktokRelationships", JoinType.LEFT);
    //     tiktokerJoin.on(criteriaBuilder.equal(tiktokerJoin.get("status"), true));
    //     Join<TiktokRelationship, TiktokUserDetails> tiktokerJoin2 = tiktokerJoin.join("tiktoker", JoinType.LEFT);

    //     // Subquery for inviteCount
    //     Subquery<Long> inviteCountSubquery = query.subquery(Long.class);
    //     Root<InviterRelationship> inviterRelRoot = inviteCountSubquery.from(InviterRelationship.class);
    //     inviteCountSubquery.select(criteriaBuilder.count(inviterRelRoot))
    //         .where(
    //             criteriaBuilder.equal(inviterRelRoot.get("inviter").get("userId"), root.get("userId")),
    //             criteriaBuilder.equal(inviterRelRoot.get("status"), true)
    //         );
    
    //     Subquery<String> invitationCodeSubquery = query.subquery(String.class);
    //     Root<TbUser> tbUserRoot2 = invitationCodeSubquery.from(TbUser.class);
    //     invitationCodeSubquery.select(tbUserRoot2.get("invitationCode"))
    //         .where(criteriaBuilder.equal(tbUserRoot2.get("userId"), root.get("platformId")));
    
    //     Subquery<Integer> invitationTypeSubquery = query.subquery(Integer.class);
    //     Root<TbUser> tbUserRoot3 = invitationTypeSubquery.from(TbUser.class);
    //     invitationTypeSubquery.select(tbUserRoot3.get("invitationType"))
    //         .where(criteriaBuilder.equal(tbUserRoot3.get("userId"), root.get("platformId")));
    
    //     // Subquery for platformInviteCount
    //     Subquery<Long> platformInviteCountSubquery = query.subquery(Long.class);
    //     Root<TbUser> tbUserRoot4 = platformInviteCountSubquery.from(TbUser.class);
    //     platformInviteCountSubquery.select(criteriaBuilder.count(tbUserRoot4))
    //         .where(criteriaBuilder.equal(tbUserRoot4.get("inviterCode"), invitationCodeSubquery));

    //     // Subqueries to fetch fields from InviteMoney based on BackendUser.platformId
    //     Subquery<Double> moneySumSubquery = query.subquery(Double.class);
    //     Root<InviteMoney> inviteMoneyRoot1 = moneySumSubquery.from(InviteMoney.class);
    //     moneySumSubquery.select(inviteMoneyRoot1.get("moneySum"))
    //         .where(criteriaBuilder.equal(inviteMoneyRoot1.get("userId"), root.get("platformId")));

    //     Subquery<Double> moneySubquery = query.subquery(Double.class);
    //     Root<InviteMoney> inviteMoneyRoot2 = moneySubquery.from(InviteMoney.class);
    //     moneySubquery.select(inviteMoneyRoot2.get("money"))
    //         .where(criteriaBuilder.equal(inviteMoneyRoot2.get("userId"), root.get("platformId")));

    //     Subquery<Double> cashOutSumSubquery = query.subquery(Double.class);
    //     Root<InviteMoney> inviteMoneyRoot3 = cashOutSumSubquery.from(InviteMoney.class);
    //     cashOutSumSubquery.select(inviteMoneyRoot3.get("cashOut"))
    //         .where(criteriaBuilder.equal(inviteMoneyRoot3.get("userId"), root.get("platformId")));

    //     // Subqueries to fetch fields from UserMoney based on BackendUser.platformId
    //     Subquery<Double> platformMoneySubquery = query.subquery(Double.class);
    //     Root<UserMoney> userMoneyRoot = platformMoneySubquery.from(UserMoney.class);
    //     platformMoneySubquery.select(userMoneyRoot.get("money"))
    //         .where(criteriaBuilder.equal(userMoneyRoot.get("userId"), root.get("platformId")));

    //     // Subqueries to fetch fields from UserIntegral based on BackendUser.platformId
    //     Subquery<Integer> integralNumSubquery = query.subquery(Integer.class);
    //     Root<UserIntegral> userIntegralRoot = integralNumSubquery.from(UserIntegral.class);
    //     integralNumSubquery.select(userIntegralRoot.get("integralNum"))
    //         .where(criteriaBuilder.equal(userIntegralRoot.get("userId"), root.get("platformId")));

    //     // 查applicationProcessRecord表的projectAmount求和，status in [5, 6, 7, 99, 100]
    //     Subquery<Double> projectAmountSumSubquery = query.subquery(Double.class);
    //     Root<ApplicationProcessRecord> processRecordRoot1 = projectAmountSumSubquery.from(ApplicationProcessRecord.class);
    //     projectAmountSumSubquery.select(criteriaBuilder.sum(processRecordRoot1.get("projectAmount")))
    //         .where(
    //             criteriaBuilder.equal(processRecordRoot1.get("userId"), root.get("userId")),
    //             processRecordRoot1.get("processStatus").in(5, 6, 7, 99, 100)
    //         );

    //     // 查applicationProcessRecord表的paidStr
    //     Subquery<String> paidStrSubquery = query.subquery(String.class);
    //     Root<ApplicationProcessRecord> processRecordRoot2 = paidStrSubquery.from(ApplicationProcessRecord.class);
    //     paidStrSubquery.select(processRecordRoot2.get("paidStr"))
    //         .where(
    //             criteriaBuilder.equal(processRecordRoot2.get("userId"), root.get("userId"))
    //         );

    //     query.select(criteriaBuilder.construct(
    //         SearchResponseDTO.class, 
    //         root.get("userId").alias("userId"),
    //         root.get("username").alias("username"),
    //         root.get("fullname").alias("fullname"),
    //         root.get("regionName").alias("regionName"),
    //         root.get("currencyName").alias("currencyName"),
    //         root.get("createdAt").alias("createdAt"),
    //         root.get("updatedAt").alias("updatedAt"),
    //         roleJoin2.get("roleId").alias("roleId"),
    //         createrJoin2.get("userId").alias("createrId"),
    //         createrJoin2.get("username").alias("createrName"),
    //         createrJoin2.get("fullname").alias("createrFullname"),
    //         inviterJoin2.get("userId").alias("inviterId"),
    //         inviterJoin2.get("username").alias("inviterName"),
    //         inviterJoin2.get("fullname").alias("inviterFullname"),
    //         managerJoin2.get("userId").alias("managerId"),
    //         managerJoin2.get("username").alias("managerName"),
    //         managerJoin2.get("fullname").alias("managerFullname"),
    //         teacherJoin2.get("userId").alias("teacherId"),
    //         teacherJoin2.get("username").alias("teacherName"),
    //         teacherJoin2.get("fullname").alias("teacherFullname"),
    //         root.get("status").alias("status"),
    //         root.get("platformId").alias("platformId"),
    //         inviterCodeSubquery.alias("inviterCode"),
    //         invitationCodeSubquery.alias("invitationCode"),
    //         invitationTypeSubquery.alias("invitationType"),
    //         moneySumSubquery.alias("platformTotalRevenue"),
    //         moneySubquery.alias("platformRevenueBalance"),
    //         cashOutSumSubquery.alias("platformTotalWithdrawal"),
    //         platformMoneySubquery.alias("platformMoney"),
    //         integralNumSubquery.alias("integralNum"),
    //         inviteCountSubquery.alias("inviteCount"),
    //         platformInviteCountSubquery.alias("platformInviteCount"),
    //         projectAmountSumSubquery.alias("projectAmountSum"),
    //         paidStrSubquery.alias("paidStr"),
    //         criteriaBuilder.construct(
    //             TiktokAccountDTO.class,
    //             tiktokerJoin2.get("tiktokAccount").alias("tiktokAccount"),
    //             tiktokerJoin2.get("tiktokId").alias("tiktokId"),
    //             tiktokerJoin2.get("diggCount").alias("diggCount"),
    //             tiktokerJoin2.get("followerCount").alias("followerCount"),
    //             tiktokerJoin2.get("followingCount").alias("followingCount"),
    //             tiktokerJoin2.get("friendCount").alias("friendCount"),
    //             tiktokerJoin2.get("heartCount").alias("heartCount"),
    //             tiktokerJoin2.get("videoCount").alias("videoCount"),
    //             tiktokerJoin2.get("updatedAt").alias("accountUpdatedAt")
    //         ).alias("tiktokAccountDTO")
    //     ));

    //     query.where(buildPredicates(searchDTO, criteriaBuilder, root, allowedUserIds));
    
    //     // Execute main query
    //     TypedQuery<SearchResponseDTO> typedQuery = entityManager.createQuery(query);
    //     typedQuery.setFirstResult((int) pageable.getOffset());
    //     typedQuery.setMaxResults(pageable.getPageSize());
    //     List<SearchResponseDTO> searchResponseDTOs = typedQuery.getResultList();

    //     // Total count (adjust as needed)
    //     CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    //     Root<BackendUser> countRoot = countQuery.from(BackendUser.class);
    //     countQuery.select(criteriaBuilder.count(countRoot));
    //     countQuery.where(buildPredicates(searchDTO, criteriaBuilder, countRoot, allowedUserIds));
    //     Long total = entityManager.createQuery(countQuery).getSingleResult();
    
    //     // Collect user IDs
    //     Set<Long> userIds = searchResponseDTOs.stream()
    //         .map(SearchResponseDTO::getUserId)
    //         .collect(Collectors.toSet());

    //     // Fetch RoleRelationships
    //     TypedQuery<RoleRelationship> roleQuery = entityManager.createQuery(
    //         "SELECT rr FROM RoleRelationship rr WHERE rr.user.userId IN :userIds", RoleRelationship.class);
    //     roleQuery.setParameter("userIds", userIds);
    //     List<RoleRelationship> allRoleRelationships = roleQuery.getResultList();
    
    //     // Fetch PermissionRelationships
    //     TypedQuery<PermissionRelationship> permissionQuery = entityManager.createQuery(
    //         "SELECT pr FROM PermissionRelationship pr WHERE pr.user.userId IN :userIds", PermissionRelationship.class);
    //     permissionQuery.setParameter("userIds", userIds);
    //     List<PermissionRelationship> allPermissionRelationships = permissionQuery.getResultList();
    
    //     // Fetch Invited User IDs
    //     TypedQuery<InviterRelationship> inviterQuery = entityManager.createQuery(
    //         "SELECT ir FROM InviterRelationship ir WHERE ir.inviter.userId IN :userIds", InviterRelationship.class);
    //     inviterQuery.setParameter("userIds", userIds);
    //     List<InviterRelationship> allInviterRelationships = inviterQuery.getResultList();

    //     // Map relationships to users
    //     Map<Long, List<RoleRelationship>> userIdToRoles = allRoleRelationships.stream()
    //         .collect(Collectors.groupingBy(rr -> rr.getUser().getUserId()));
    
    //     Map<Long, List<PermissionRelationship>> userIdToPermissions = allPermissionRelationships.stream()
    //         .collect(Collectors.groupingBy(pr -> pr.getUser().getUserId()));
    
    //     Map<Long, List<InviterRelationship>> userIdToInviters = allInviterRelationships.stream()
    //         .collect(Collectors.groupingBy(ir -> ir.getInviter().getUserId()));

    //     // Construct SearchUsersDTO list
    //     List<SearchUsersDTO> searchUsersDTOs = new ArrayList<>();
    //     for (SearchResponseDTO dto : searchResponseDTOs) {
    //         Long userId = dto.getUserId();
    
    //         List<RoleRelationship> roleRelationships = userIdToRoles.getOrDefault(userId, Collections.emptyList());
    //         List<PermissionRelationship> permissionRelationships = userIdToPermissions.getOrDefault(userId, Collections.emptyList());
    //         List<InviterRelationship> inviterRelationships = userIdToInviters.getOrDefault(userId, Collections.emptyList());
    
    //         SearchUsersDTO usersDTO = SearchUsersDTO.builder()
    //             .searchResponseDTO(dto)
    //             .roleRelationships(roleRelationships)
    //             .permissionRelationships(permissionRelationships)
    //             .inviterRelationships(inviterRelationships)
    //             .build();
    
    //         searchUsersDTOs.add(usersDTO);
    //     }

    //     return new PageImpl<>(searchUsersDTOs, pageable, total);
    // }

    // private Predicate buildPredicates(BackendUserSearchDTO searchDTO, CriteriaBuilder criteriaBuilder,
    //         Root<BackendUser> root, Set<Long> allowedUserIds) {

    //     List<Predicate> predicates = new ArrayList<>();

    //     if (!allowedUserIds.isEmpty()) {
    //         predicates.add(root.get("userId").in(allowedUserIds));
    //     }

    //     // 构建查询条件，使用传入的 Join
    //     if (searchDTO.getUserId() != null) {
    //         predicates.add(criteriaBuilder.equal(root.get("userId"), searchDTO.getUserId()));
    //     }
    //     if (searchDTO.getUsername() != null && !searchDTO.getUsername().isEmpty()) {
    //         predicates.add(criteriaBuilder.equal(root.get("username"), searchDTO.getUsername().trim()));
    //     }
    //     if (searchDTO.getFullname() != null && !searchDTO.getFullname().isEmpty()) {
    //         predicates.add(criteriaBuilder.like(root.get("fullname"), "%" + searchDTO.getFullname() + "%"));
    //     }
    //     if (searchDTO.getRegionName() != null && !searchDTO.getRegionName().isEmpty()) {
    //         predicates.add(criteriaBuilder.equal(root.get("regionName"), searchDTO.getRegionName()));
    //     }
    //     if (searchDTO.getCurrencyName() != null && !searchDTO.getCurrencyName().isEmpty()) {
    //         predicates.add(criteriaBuilder.equal(root.get("currencyName"), searchDTO.getCurrencyName()));
    //     }
    //     if (searchDTO.getStatus() != null) {
    //         predicates.add(criteriaBuilder.equal(root.get("status"), searchDTO.getStatus()));
    //     }
    //     if (searchDTO.getCreatedAfter() != null) {
    //         predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt").as(LocalDate.class), searchDTO.getCreatedAfter()));
    //     }
    //     if (searchDTO.getCreatedBefore() != null) {
    //         predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt").as(LocalDate.class), searchDTO.getCreatedBefore()));
    //     }
    //     if (searchDTO.getPlatformId() != null) {
    //         predicates.add(criteriaBuilder.equal(root.get("platformId"), searchDTO.getPlatformId()));
    //     }
    //     // For roleId
    //     if (searchDTO.getRoleId() != null) {
    //         Predicate roleIdPredicate = criteriaBuilder.equal(root.get("roleRelationships").get("role").get("roleId"), searchDTO.getRoleId());
    //         predicates.add(roleIdPredicate);
    //     }

    //     // For createrId
    //     if (searchDTO.getCreaterId() != null) {
    //         Predicate createrIdPredicate = criteriaBuilder.equal(root.get("createrRelationships").get("creater").get("userId"), searchDTO.getCreaterId());
    //         predicates.add(createrIdPredicate);
    //     }

    //     // For createrName
    //     if (searchDTO.getCreaterName() != null && !searchDTO.getCreaterName().isEmpty()) {
    //         Predicate createrNamePredicate = criteriaBuilder.equal(root.get("createrRelationships").get("creater").get("username"), searchDTO.getCreaterName().trim());
    //         predicates.add(createrNamePredicate);
    //     }

    //     // For createrFullname
    //     if (searchDTO.getCreaterFullname() != null && !searchDTO.getCreaterFullname().isEmpty()) {
    //         Predicate createrFullnamePredicate = criteriaBuilder.like(root.get("createrRelationships").get("creater").get("fullname"), "%" + searchDTO.getCreaterFullname() + "%");
    //         predicates.add(createrFullnamePredicate);
    //     }

    //     // For inviterId
    //     if (searchDTO.getInviterId() != null) {
    //         Predicate inviterIdPredicate = criteriaBuilder.equal(root.get("inviterRelationships").get("inviter").get("userId"), searchDTO.getInviterId());
    //         predicates.add(inviterIdPredicate);
    //     }

    //     // For inviterName
    //     if (searchDTO.getInviterName() != null && !searchDTO.getInviterName().isEmpty()) {
    //         Predicate inviterNamePredicate = criteriaBuilder.equal(root.get("inviterRelationships").get("inviter").get("username"), searchDTO.getInviterName().trim());
    //         predicates.add(inviterNamePredicate);
    //     }

    //     // For inviterFullname
    //     if (searchDTO.getInviterFullname() != null && !searchDTO.getInviterFullname().isEmpty()) {
    //         Predicate inviterFullnamePredicate = criteriaBuilder.like(root.get("inviterRelationships").get("inviter").get("fullname"), "%" + searchDTO.getInviterFullname() + "%");
    //         predicates.add(inviterFullnamePredicate);
    //     }

    //     // For managerId
    //     if (searchDTO.getManagerId() != null) {
    //         Predicate managerIdPredicate = criteriaBuilder.equal(root.get("managerRelationships").get("manager").get("userId"), searchDTO.getManagerId());
    //         predicates.add(managerIdPredicate);
    //     }

    //     // For managerName
    //     if (searchDTO.getManagerName() != null && !searchDTO.getManagerName().isEmpty()) {
    //         Predicate managerNamePredicate = criteriaBuilder.equal(root.get("managerRelationships").get("manager").get("username"), searchDTO.getManagerName().trim());
    //         predicates.add(managerNamePredicate);
    //     }

    //     // For managerFullname
    //     if (searchDTO.getManagerFullname() != null && !searchDTO.getManagerFullname().isEmpty()) {
    //         Predicate managerFullnamePredicate = criteriaBuilder.like(root.get("managerRelationships").get("manager").get("fullname"), "%" + searchDTO.getManagerFullname() + "%");
    //         predicates.add(managerFullnamePredicate);
    //     }

    //     return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    // }

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
    private void handleTiktokChange(BackendUser user, String tiktokAccountStr, LocalDate startDate) {

        List<TiktokRelationship> existingRelationships = user.getTiktokRelationships();

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

    private List<SearchResponseDTO> mapToDTO(List<BackendUser> records) {
        return records.stream()
            .map(record -> SearchResponseDTO.builder()
                .userId(record.getUserId())
                .username(record.getUsername())
                .fullname(record.getFullname())
                .regionName(record.getRegionName())
                .currencyName(record.getCurrencyName())
                .inviteCount(backendUserRepository.countByInviterId(record.getUserId()))
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .status(record.getStatus())
                .createrDTO(
                    record.getCreaterRelationships().stream()
                        .filter(creater -> creater.getEndDate() == null)
                        .findFirst()
                        .map(creater -> SearchResponseDTO.CreaterDTO.builder()
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
                        .map(inviter -> SearchResponseDTO.InviterDTO.builder()
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
                        .map(manager -> SearchResponseDTO.ManagerDTO.builder()
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
                        .map(teacher -> SearchResponseDTO.TeacherDTO.builder()
                            .userId(teacher.getTeacher().getUserId())
                            .username(teacher.getTeacher().getUsername())
                            .fullname(teacher.getTeacher().getFullname())
                            .build())
                        .orElse(null)
                )
                .tbUserDTO(
                    record.getTbUser() != null ? SearchResponseDTO.TbUserDTO.builder()
                        .userId(record.getTbUser().getUserId())
                        .inviterCode(record.getTbUser().getInviterCode())
                        .invitationCode(record.getTbUser().getInvitationCode())
                        .invitationType(record.getTbUser().getInvitationType())
                        .moneySum(record.getTbUser().getInviteMoney().getMoneySum())
                        .money(record.getTbUser().getInviteMoney().getMoney())
                        .cashOut(record.getTbUser().getInviteMoney().getCashOut())
                        .userMoney(record.getTbUser().getUserMoney().getMoney())
                        .userIntegral(record.getTbUser().getUserIntegral().getIntegralNum())
                        .inviteCount(localTbUserRepository.countByInviterCode(record.getTbUser().getInvitationCode()))
                        .inviteDailyMoneySumDTOs(localInviteRepository.findDailyMoneySumByUserId(record.getTbUser().getUserId()))
                        .build() : null
                )
                .tiktokAccountDTO(
                    record.getTiktokRelationships().stream()
                        .filter(tiktok -> tiktok.getStatus())
                        .map(tiktok -> TiktokAccountDTO.builder()
                            .tiktokAccount(tiktok.getTiktokAccount())
                            .tiktokId(tiktok.getTiktokUserDetails().getTiktokId())
                            .diggCount(tiktok.getTiktokUserDetails().getDiggCount())
                            .followerCount(tiktok.getTiktokUserDetails().getFollowerCount())
                            .followingCount(tiktok.getTiktokUserDetails().getFollowingCount())
                            .friendCount(tiktok.getTiktokUserDetails().getFriendCount())
                            .heartCount(tiktok.getTiktokUserDetails().getHeartCount())
                            .videoCount(tiktok.getTiktokUserDetails().getVideoCount())
                            .updatedAt(tiktok.getTiktokUserDetails().getUpdatedAt())
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
                .applicationPaymentRecordDTOs(
                    record.getApplicationProcessRecordAsUser().getApplicationPaymentRecords().stream()
                        .map(payment -> SearchResponseDTO.ApplicationPaymentRecordDTO.builder()
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
                            .rate(usdRateRepository.findByDateAndCurrencyCode(payment.getPaymentDate(), payment.getCurrencyCode()))
                            .build())
                        .collect(Collectors.toList())
                )
                .build())
            .collect(Collectors.toList());
    }

}
