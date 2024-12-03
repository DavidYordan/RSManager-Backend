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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final BackendUserRepository backendUserRepository;
    private final RolePermissionRepository rolePermissionRepository;
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

        // 判断选定月份是否为当前月份
        YearMonth currentYearMonth = YearMonth.now();
        boolean isCurrentMonth = selectedMonth.equals(currentYearMonth);
        // 如果是当前月，计算到今天，否则计算到月末
        LocalDate actualEndOfMonth = isCurrentMonth ? LocalDate.now() : endOfMonth;

        // 存储历史总收益和付款人数，按货币分组
        Map<String, Double> historyTotalProfitByCurrency = new HashMap<>();
        Map<String, Integer> historyTotalInvitesByCurrency = new HashMap<>();
        Map<String, Set<String>> userFullnamesByCurrency = new HashMap<>();
        Map<String, List<ProfitDTO>> profitsByCurrency = new HashMap<>();

        List<ProfitDTO> allProfits = new ArrayList<>();
        if (responseDTO.getProfits1() != null) {
            allProfits.addAll(responseDTO.getProfits1());
        }
        if (responseDTO.getProfits2() != null) {
            allProfits.addAll(responseDTO.getProfits2());
        }

        // 收集所有涉及的货币
        Set<String> allCurrencies = allProfits.stream()
                .map(ProfitDTO::getCurrencyName)
                .collect(Collectors.toSet());

        for (ProfitDTO profit : allProfits) {
            String currencyName = profit.getCurrencyName();

            if (profit.getPaymentDate().isBefore(startOfMonth)) {
                // 更新历史总收益
                historyTotalProfitByCurrency.put(currencyName,
                    historyTotalProfitByCurrency.getOrDefault(currencyName, 0.0) + 
                    (profit.getProfit() != null ? profit.getProfit() : 0.0));

                // 更新历史付款人数
                userFullnamesByCurrency.computeIfAbsent(currencyName, k -> new HashSet<>());
                Set<String> userFullnames = userFullnamesByCurrency.get(currencyName);
                if (profit.getUserFullname() != null && userFullnames.add(profit.getUserFullname())) {
                    historyTotalInvitesByCurrency.put(currencyName,
                        historyTotalInvitesByCurrency.getOrDefault(currencyName, 0) + 1);
                }
            } else if (!profit.getPaymentDate().isAfter(actualEndOfMonth)) {
                // 当前月份的收益，按货币分组
                profitsByCurrency.computeIfAbsent(currencyName, k -> new ArrayList<>()).add(profit);
            }
        }

        Double totalLearningCost = 0.0;

        if (responseDTO.getApplicationPaymentRecordDTOs() != null) {
            for (ApplicationPaymentRecordDTO payment: responseDTO.getApplicationPaymentRecordDTOs()) {
                totalLearningCost += payment.getPaymentAmount();
            }
        }

        List<InviteDailyMoneySumDTO> inviteDailyMoneySumDTOs = new ArrayList<>();
        inviteDailyMoneySumDTOs.addAll(responseDTO.getInviteDailyMoneySum0DTOs());
        inviteDailyMoneySumDTOs.addAll(responseDTO.getInviteDailyMoneySum1DTOs());

        // 构建按货币分组的收益和增长数据
        List<OwnerSummaryDTO.CurrencyProfitData> currencyProfits = new ArrayList<>();

        // 确保所有货币都被处理，即使某些货币在选定月份没有收益
        for (String currencyName : allCurrencies) {
            List<ProfitDTO> profitList = profitsByCurrency.getOrDefault(currencyName, new ArrayList<>());

            // 获取历史数据
            Double historyTotalProfit = historyTotalProfitByCurrency.getOrDefault(currencyName, 0.0);
            Integer historyTotalInvites = historyTotalInvitesByCurrency.getOrDefault(currencyName, 0);
            Set<String> userFullnames = new HashSet<>(userFullnamesByCurrency.getOrDefault(currencyName, new HashSet<>()));

            // 计算增长数据
            List<GrowthDataDTO> growthData = computeGrowthData(profitList, selectedMonth, actualEndOfMonth, historyTotalProfit, historyTotalInvites, userFullnames);

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
                .totalLearningCost(totalLearningCost)
                .moneySum(responseDTO.getMoneySum())
                .money(responseDTO.getMoney())
                .cashOut(responseDTO.getCashOut())
                .userMoney(responseDTO.getUserMoney())
                .userIntegral(responseDTO.getUserIntegral())
                .inviteCount(responseDTO.getInviteCount())
                .platformInviteCount(responseDTO.getPlatformInviteCount())
                .currencyProfits(currencyProfits)
                .inviteDailyMoneySumDTOs(inviteDailyMoneySumDTOs)
                .build();
    }
    

    /**
     * 计算增长数据，按天汇总利润和邀请人数
     */
    private List<GrowthDataDTO> computeGrowthData(
            List<ProfitDTO> profitDTOs,
            YearMonth selectedMonth,
            LocalDate actualEndOfMonth,
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

        // 计算实际需要处理的天数
        int daysInMonth = actualEndOfMonth.getDayOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = selectedMonth.atDay(day);
            String formattedDate = date.format(formatter);

            // 处理当天的所有利润记录
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

            // 创建当天的增长数据
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

        Optional<TiktokRelationship> tiktokOptional = tikTokRelationshipRepository.findByTiktokAccountAndStatus(tiktokAccountStr);

        
        if (tiktokOptional.isPresent()) {
            BackendUser backendUser = tiktokOptional.get().getUser();
            throw new IllegalArgumentException("tiktok账户已被用户" + backendUser.getFullname() + "绑定");
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
}
