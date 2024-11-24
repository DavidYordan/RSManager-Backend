package com.rsmanager.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rsmanager.dto.application.*;
import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.service.*;
import com.rsmanager.utils.LocalDateAdapter;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationPaymentRecordRepository applicationPaymentRecordRepository;
    private final ApplicationProcessRecordRepository applicationProcessRecordRepository;
    private final AuthService authService;
    private final BackendRoleRepository backendRoleRepository;
    private final BackendUserRepository backendUserRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final LocalTbUserRepository localTbUserRepository;
    private final TiktokUserDetailsRepository tiktokAccountRepository;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Value("${file.upload.base-path}")
    private String baseUploadPath;

    /**
     * 创建新的申请记录
     */
    @Override
    @Transactional
    public Long createApplication(ApplicationCreateRequestDTO request, MultipartFile[] files) {
        // 获取用户信息
        Long createrId = authService.getCurrentUserId();
        String createrUsername = authService.getCurrentUsername();
        String createrFullname = authService.getCurrentFullname();

        String regionName = request.getRegionName();
        String currencyName = request.getCurrencyName();
        String comments = request.getComments();

        String managerName = request.getManagerName().trim();
        BackendUser manager = backendUserRepository.findByUsername(managerName)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        String inviterName = request.getInviterName().trim();
        Optional<BackendUser> inviter = backendUserRepository.findByUsername(inviterName);
        Long inviterId = null;
        String inviterFullname = null;
        if (inviter.isPresent()) {
            inviterId = inviter.get().getUserId();
            inviterFullname = inviter.get().getFullname();
        }

        LocalDate paymentDate = request.getPaymentTime();

        ApplicationProcessRecord applicationProcessRecord = ApplicationProcessRecord.builder()
                .fullname(request.getFullname())
                .roleId(request.getRoleId())
                .projectName(request.getProjectName())
                .projectAmount(request.getProjectAmount())
                .inviterId(inviterId)
                .inviterName(request.getInviterName())
                .inviterFullname(inviterFullname)
                .managerId(manager.getUserId())
                .managerName(manager.getUsername())
                .managerFullname(manager.getFullname())
                .createrId(createrId)
                .createrName(createrUsername)
                .createrFullname(createrFullname)
                .rateA(request.getRateA())
                .rateB(request.getRateB())
                .startDate(paymentDate)
                .paymentMethod(request.getPaymentMethod())
                .regionName(regionName)
                .currencyName(currencyName)
                .comments(comments)
                .processStatus(1)
                .build();

        // For applicationPaymentRecords
        List<ApplicationPaymentRecord> paymentRecords = new ArrayList<>();
        paymentRecords.add(createPaymentRecord(
            regionName, currencyName, request.getProjectName(), request.getProjectAmount(), request.getPaymentMethod(),
            request.getPaymentAmount(), request.getFee(), paymentDate, createrId, createrUsername, createrFullname,
            comments, applicationProcessRecord
        ));

        // For applicationFlowRecords
        List<ApplicationFlowRecord> flowRecords = new ArrayList<>();
        flowRecords.add(createFlowRecord(
            "创建申请单", createrId, createrUsername, createrFullname, comments, applicationProcessRecord
        ));
        flowRecords.add(createFlowRecord(
            "创建支付记录", createrId, createrUsername, createrFullname, comments, applicationProcessRecord
        )); 

        applicationProcessRecord.setApplicationPaymentRecords(paymentRecords);
        applicationProcessRecord.setApplicationFlowRecords(flowRecords);

        // 保存流程单记录及关联
        ApplicationProcessRecord savedRecord = applicationProcessRecordRepository.save(applicationProcessRecord);

        // 使用 FileStorageService 上传文件
        uploadFiles("applications/" + savedRecord.getProcessId() + "/payments/" + savedRecord.getApplicationPaymentRecords().get(0).getPaymentId().toString(), files);

        return savedRecord.getProcessId();
    }

    /**
     * 上传合同文件
     *
     * @param processId 流程单ID
     * @param files     合同文件
     * @return ServiceResponseDTO
     */
    @Override
    public Boolean uploadContractFiles(Long processId, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Application not found."));


        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            authService.getCurrentUserId(),
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found.")))) ) {
            throw new IllegalStateException("You cannot upload contract files for this application.");
        }

        // 使用 FileStorageService 上传文件
        uploadFiles("applications/" + processId.toString() + "/contracts", files);

        return true;
    }

    /**
     * 更新申请
     *
     * @param request 更新请求DTO
     * @return ServiceResponseDTO
     */
    @Override
    @Transactional
    public Boolean updateApplication(ApplicationProcessUpdateDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 1)      
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        String managerName = request.getManagerName().trim();
        BackendUser manager = backendUserRepository.findByUsername(managerName)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(userId, manager))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        String inviterName = request.getInviterName().trim();
        Optional<BackendUser> inviter = backendUserRepository.findByUsername(inviterName);
        Long inviterId = null;
        String inviterFullname = null;
        if (inviter.isPresent()) {
            inviterId = inviter.get().getUserId();
            inviterFullname = inviter.get().getFullname();
        }

        applicationProcessRecord.setFullname(request.getFullname());
        applicationProcessRecord.setInviterId(inviterId);
        applicationProcessRecord.setInviterName(inviterName);
        applicationProcessRecord.setInviterFullname(inviterFullname);
        applicationProcessRecord.setManagerId(manager.getUserId());
        applicationProcessRecord.setManagerName(manager.getUsername());
        applicationProcessRecord.setManagerFullname(manager.getFullname());
        applicationProcessRecord.setRoleId(request.getRoleId());
        applicationProcessRecord.setRegionName(request.getRegionName());
        applicationProcessRecord.setCurrency(request.getCurrency());
        applicationProcessRecord.setProjectName(request.getProjectName());
        applicationProcessRecord.setProjectAmount(request.getProjectAmount());
        applicationProcessRecord.setRateA(request.getRateA());
        applicationProcessRecord.setRateB(request.getRateB());
        applicationProcessRecord.setStartDate(request.getStartDate());
        applicationProcessRecord.setPaymentMethod(request.getPaymentMethod());
        applicationProcessRecord.setComments(request.getComments());

        // 记录flow流水
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "更新申请单", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 提交审核
     */
    @Override
    @Transactional
    public Boolean submitApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1)
                .orElseThrow(() -> new IllegalStateException("Application status is not valid for submission."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot submit this application.");
        }

        applicationProcessRecord.setProcessStatus(2);
        
        // 记录flow流水
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交财务审核", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 归档申请
     *
     * @param processId 流程单ID
     * @param authentication 认证信息
     * @return ServiceResponseDTO
     */
    @Override
    @Transactional
    public Boolean archiveApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 6)      
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1)) {
            throw new IllegalStateException("You cannot archive this application.");
        }

        applicationProcessRecord.setProcessStatus(7);
        
        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "归档申请", authService.getCurrentUserId(), authService.getCurrentUsername(),
            authService.getCurrentFullname(), request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 撤回审核
     */
    @Override
    @Transactional
    public Boolean withdrawApplication(ApplicationActionDTO request) {

        List<Integer> statusList = Arrays.asList(2, 4, 98, 99);

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> (statusList.contains(r.getProcessStatus())))
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer status = applicationProcessRecord.getProcessStatus();
        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();
        Long managerId = applicationProcessRecord.getManagerId();
        BackendUser manager = backendUserRepository.findById(managerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        Boolean canWithdraw = false;

        if (isManager(userId, manager) || roleId == 1) {
            canWithdraw = true;
        } else if ((status == 2 || status == 98) && roleId == 8) {
            canWithdraw = true;
        } else {
            canWithdraw = false;
        }

        if (!canWithdraw) {
            throw new IllegalStateException("You cannot withdraw this application.");
        }

        applicationProcessRecord.setProcessStatus(status - 1);
        
        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "撤回审核", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 切换为升级角色编辑态
     */
    @Override
    @Transactional
    public Boolean updateRoleEditing(ApplicationUpdateRoleDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();
        Long managerId = applicationProcessRecord.getManagerId();
        Long userId = authService.getCurrentUserId();

        if (!(roleId == 1 || isManager(
            userId,
            backendUserRepository.findById(managerId)
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        request.setOldStatus(applicationProcessRecord.getProcessStatus());
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        String jsonString = gson.toJson(request);

        applicationProcessRecord.setActionStr(jsonString);
        applicationProcessRecord.setProcessStatus(97);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "切换为升级角色编辑态", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 取消角色编辑态
     */
    @Override
    @Transactional
    public Boolean cancelUpdateRoleEditing(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 97 || r.getProcessStatus() == 100)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();
        Long userId = authService.getCurrentUserId();
        Long managerId = applicationProcessRecord.getManagerId();
        BackendUser manager = backendUserRepository.findById(managerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(roleId == 1 || isManager(userId, manager))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ApplicationUpdateRoleDTO oldUpdateRoleDTO = gson.fromJson(actionStr, ApplicationUpdateRoleDTO.class);
        
        applicationProcessRecord.setProcessStatus(oldUpdateRoleDTO.getOldStatus());

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "取消升级角色编辑态", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 保存编辑中的升级角色信息
     */
    @Override
    @Transactional
    public Boolean saveRoleEditing(ApplicationUpdateRoleDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();
        Long userId = authService.getCurrentUserId();
        Long managerId = applicationProcessRecord.getManagerId();
        BackendUser manager = backendUserRepository.findById(managerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(roleId == 1 || isManager(userId, manager))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

        applicationProcessRecord.setActionStr(gson.toJson(request));

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "保存升级角色信息", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 提交升级角色审核
     */
    @Override
    @Transactional
    public Boolean submitRoleUpgrade(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 97)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();
        Long userId = authService.getCurrentUserId();
        Long managerId = applicationProcessRecord.getManagerId();
        BackendUser manager = backendUserRepository.findById(managerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(roleId == 1 || isManager(userId, manager))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        applicationProcessRecord.setProcessStatus(98);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交升级角色审核", authService.getCurrentUserId(), authService.getCurrentUsername(),
            authService.getCurrentFullname(), request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 财务通过角色升级审核
     */
    @Override
    @Transactional
    public Boolean approveRoleUpgradeByFinance(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 98)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        applicationProcessRecord.setProcessStatus(99);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "通过角色升级财务审核", authService.getCurrentUserId(), authService.getCurrentUsername(),
            authService.getCurrentFullname(), request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 主管通过角色升级审核
     */
    @Override
    @Transactional
    public Boolean approveRoleUpgradeByManager(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 99)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long currentUserId = authService.getCurrentUserId();
        Integer currentRoleId = authService.getCurrentUserRoleId();
        Long currentManagerId = applicationProcessRecord.getManagerId();
        BackendUser currentManager = backendUserRepository.findById(currentManagerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(currentRoleId == 1 || isManager(currentUserId, currentManager))) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ApplicationUpdateRoleDTO oldUpdateRoleDTO = gson.fromJson(actionStr, ApplicationUpdateRoleDTO.class);

        Long userId = applicationProcessRecord.getUserId();
        BackendUser user = backendUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

        Integer roleId = oldUpdateRoleDTO.getRoleId();
        createRole(user, roleId, oldUpdateRoleDTO.getStartDate());
        createPermissions(user, roleId, oldUpdateRoleDTO.getRateA(), oldUpdateRoleDTO.getRateB());

        String paymentMethod = oldUpdateRoleDTO.getPaymentMethod();
        Integer newStatus = paymentMethod.equals("全额支付") ? 6 : 5;

        applicationProcessRecord.setProcessStatus(newStatus);
        applicationProcessRecord.setRoleId(roleId);
        applicationProcessRecord.setStartDate(oldUpdateRoleDTO.getStartDate());
        applicationProcessRecord.setPaymentMethod(paymentMethod);
        applicationProcessRecord.setRateA(oldUpdateRoleDTO.getRateA());
        applicationProcessRecord.setRateB(oldUpdateRoleDTO.getRateB());
        applicationProcessRecord.setComments(request.getComments());
        applicationProcessRecord.setProjectName(oldUpdateRoleDTO.getProjectName());
        applicationProcessRecord.setProjectAmount(oldUpdateRoleDTO.getProjectAmount());


        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "通过角色升级审核", currentUserId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 补充历史角色信息
     */
    @Override
    @Transactional
    public Boolean updateRoleHistory(ApplicationUpdateRoleDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();
        Long userId = authService.getCurrentUserId();
        Long managerId = applicationProcessRecord.getManagerId();
        BackendUser manager = backendUserRepository.findById(managerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(roleId == 1 || isManager(userId, manager))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        String jsonString = gson.toJson(request);

        applicationProcessRecord.setActionStr(jsonString);
        applicationProcessRecord.setProcessStatus(100);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "补充历史角色信息", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecord.setActionStr(gson.toJson(request));

        return true;
    }

    /**
     * 审核历史角色信息
     */
    @Override
    @Transactional
    public Boolean approveRoleHistory(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 100)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer currentRoleId = authService.getCurrentUserRoleId();
        Long currentUserId = authService.getCurrentUserId();
        Long currentManagerId = applicationProcessRecord.getManagerId();
        BackendUser currentManager = backendUserRepository.findById(currentManagerId)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        if (!(currentRoleId == 1 || (currentRoleId == 2 && isManager(currentUserId, currentManager)))) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ApplicationUpdateRoleDTO oldUpdateRoleDTO = gson.fromJson(actionStr, ApplicationUpdateRoleDTO.class);

        Long userId = applicationProcessRecord.getUserId();
        BackendUser user = backendUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

        Integer roleId = oldUpdateRoleDTO.getRoleId();
        createRole(user, roleId, oldUpdateRoleDTO.getStartDate());
        createPermissions(user, roleId, oldUpdateRoleDTO.getRateA(), oldUpdateRoleDTO.getRateB());

        applicationProcessRecord.setProcessStatus(6);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "通过历史角色信息审核", currentUserId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 添加支付记录
     *
     * @param PaymentAddDTO
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean addPaymentRecord(PaymentAddDTO request, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot add payment record to this application.");
        }

        String username = authService.getCurrentUsername();
        String fullname = authService.getCurrentFullname();
        String comments = request.getComments();

        ApplicationPaymentRecord paymentRecord = createPaymentRecord(
            request.getRegionName(), request.getCurrency(), request.getProjectName(), request.getProjectAmount(),
            request.getPaymentMethod(), request.getPaymentAmount(), request.getFee(), request.getPaymentTime(),
            userId, username, fullname, comments, applicationProcessRecord
        );

        applicationProcessRecord.getApplicationPaymentRecords().add(paymentRecord);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "增加支付记录", userId, username, fullname, comments, applicationProcessRecord
        ));

        ApplicationProcessRecord savedRecord = applicationProcessRecordRepository.save(applicationProcessRecord);

        // 使用 FileStorageService 上传文件
        uploadFiles("applications/" + savedRecord.getProcessId().toString() + "/payments/" + paymentRecord.getPaymentId().toString(), files);

        return true;
    }

    /**
     * 审核支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean approvePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 2 || r.getProcessStatus() == 5 || r.getProcessStatus() == 98)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve payment record for this application.");
        }

        String username = authService.getCurrentUsername();
        String fullname = authService.getCurrentFullname();
        Long paymentId = request.getPaymentId();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && !r.getStatus())
                .findFirst()
                .map(r -> {
                    r.setFinanceId(userId);
                    r.setFinanceName(username);
                    r.setFinanceFullname(fullname);
                    r.setFinanceApprovalTime(Instant.now());
                    r.setStatus(true);
                    r.setComments(request.getComments());
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        getTotalPaymentAmountByCurrency(request.getProcessId());

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务审核通过", userId, username, fullname, request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 撤销审核支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean disApprovePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 2 || r.getProcessStatus() == 5 || r.getProcessStatus() == 98)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve payment record for this application.");
        }

        String username = authService.getCurrentUsername();
        String fullname = authService.getCurrentFullname();
        Long paymentId = request.getPaymentId();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && r.getStatus())
                .findFirst()
                .map(r -> {
                    r.setFinanceId(userId);
                    r.setFinanceName(username);
                    r.setFinanceFullname(fullname);
                    r.setFinanceApprovalTime(Instant.now());
                    r.setStatus(false);
                    r.setComments(request.getComments());
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        getTotalPaymentAmountByCurrency(request.getProcessId());

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务撤销审核", userId, username, fullname, request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 编辑支付记录
     *
     * @param PaymentUpdateDTO
     * @param files 附件文件
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean updatePaymentRecord(PaymentUpdateDTO request, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot update this payment record.");
        }

        Long paymentId = request.getPaymentId();
        String comments = request.getComments();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && !r.getStatus())
                .findFirst()
                .map(r -> {
                    r.setRegionName(request.getRegionName());
                    r.setCurrency(request.getCurrency());
                    r.setProjectName(request.getProjectName());
                    r.setProjectAmount(request.getProjectAmount());
                    r.setPaymentMethod(request.getPaymentMethod());
                    r.setFee(request.getFee());
                    r.setPaymentAmount(request.getPaymentAmount());
                    r.setActual(request.getPaymentAmount() - request.getFee());
                    r.setPaymentTime(request.getPaymentTime());
                    r.setComments(comments);
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        List<String> deleteFiles = request.getDeleteFiles();
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            fileStorageService.deleteFiles(deleteFiles);
        }

        if (files != null && files.length > 0) {
            uploadFiles("applications/" + applicationProcessRecord.getProcessId().toString() + "/payments/" + paymentId.toString(), files);
        }

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "更新支付记录", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            comments, applicationProcessRecord
        ));

        return true;
    }

    /**
     * 删除支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean deletePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot delete this payment record.");
        }

        Integer processStatus = applicationProcessRecord.getProcessStatus();
        Long paymentId = request.getPaymentId();

        if (processStatus == 1 ) {
            applicationProcessRecord.getApplicationPaymentRecords().stream()
                    .filter(r -> r.getPaymentId().equals(paymentId))
                    .findFirst()
                    .map(r -> {
                        applicationPaymentRecordRepository.delete(r);
                        applicationProcessRecord.getApplicationPaymentRecords().remove(r);
                        return r;
                    })
                    .orElseThrow(() -> new IllegalStateException("Payment record not found."));
        } else {
            applicationProcessRecord.getApplicationPaymentRecords().stream()
                    .filter(r -> r.getPaymentId().equals(paymentId) && !r.getStatus())
                    .findFirst()
                    .map(r -> {
                        applicationPaymentRecordRepository.delete(r);
                        applicationProcessRecord.getApplicationPaymentRecords().remove(r);
                        return r;
                    })
                    .orElseThrow(() -> new IllegalStateException("Payment record not found."));
        }

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "删除支付记录", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
            ));

        return true;
    }

     /**
     * 财务审批申请
     *
     * @param processId 流程单ID
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean approveFinanceApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> (r.getProcessStatus() == 2 || r.getProcessStatus() == 98) && r.getApplicationPaymentRecords().stream().allMatch(p -> p.getStatus()))
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        applicationProcessRecord.setProcessStatus(3);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务审核通过", authService.getCurrentUserId(), authService.getCurrentUsername(),
            authService.getCurrentFullname(), request.getComments(), applicationProcessRecord
            ));

        return true;
    }

    /**
     * 链接审批申请
     *
     * @param processId 流程单ID
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean approvelink(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 4)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long currentUserId = authService.getCurrentUserId();
        Integer currentRoleId = authService.getCurrentUserRoleId();

        if (!(currentRoleId == 1 || isManager(
            currentUserId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        String username = applicationProcessRecord.getUsername();
        String fullname = applicationProcessRecord.getFullname();

        if (userService.userExists(username)) {
            throw new IllegalStateException("Platform account already exists.");
        }

        BackendUser newUser = BackendUser.builder()
            .username(username)
            .password(passwordEncoder.encode("123456"))
            .fullname(fullname)
            .platformId(applicationProcessRecord.getPlatformId())
            .regionName(applicationProcessRecord.getRegionName())
            .currencyName(applicationProcessRecord.getCurrencyName())
            .status(true)
            .build();


        LocalDate startDate = applicationProcessRecord.getStartDate();

        Integer roleId = applicationProcessRecord.getRoleId();
        createRole(newUser, roleId, startDate);
        createPermissions(newUser, roleId, applicationProcessRecord.getRateA(), applicationProcessRecord.getRateB());

        String tiktokAccountString = applicationProcessRecord.getTiktokAccount();
        if (tiktokAccountString == null || tiktokAccountString.trim().isEmpty()) {
            throw new IllegalStateException("TikTok account cannot be null or empty.");
        }

        TiktokRelationship tiktokRelationship = TiktokRelationship.builder()
            .user(newUser)
            .startDate(startDate)
            .status(true)
            .createrId(currentUserId)
            .build();

        TiktokUserDetails tiktokAccount = TiktokUserDetails.builder()
            .tiktokAccount(tiktokAccountString)
            .build();

        tiktokAccountRepository.save(tiktokAccount);

        tiktokRelationship.setTiktoker(tiktokAccount);
        newUser.getTiktokRelationships().add(tiktokRelationship);


        BackendUser creater = backendUserRepository.findById(applicationProcessRecord.getCreaterId())
            .orElseThrow(() -> new IllegalStateException("Creater not found."));

        CreaterRelationship createrRelationship = CreaterRelationship.builder()
            .user(newUser)
            .creater(creater)
            .startDate(startDate)
            .build();

        newUser.getCreaterRelationships().add(createrRelationship);


        String inviterName = applicationProcessRecord.getInviterName().trim();
        Optional<BackendUser> inviterOpt = backendUserRepository.findByUsername(inviterName);

        if (inviterOpt.isPresent()) {
            BackendUser inviter = inviterOpt.get();
            applicationProcessRecord.setInviterId(inviter.getUserId());
            applicationProcessRecord.setInviterFullname(inviter.getFullname());

            InviterRelationship inviterRelationship = InviterRelationship.builder()
                .user(newUser)
                .inviter(inviter)
                .startDate(startDate)
                .status(true)
                .createrId(currentUserId)
                .build();

            newUser.getInviterRelationships().add(inviterRelationship);
        }


        BackendUser manager = backendUserRepository.findById(applicationProcessRecord.getManagerId())
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        ManagerRelationship managerRelationship = ManagerRelationship.builder()
            .user(newUser)
            .manager(manager)
            .startDate(startDate)
            .status(true)
            .createrId(currentUserId)
            .build();

        newUser.getManagerRelationships().add(managerRelationship);

        BackendUser savedUser = backendUserRepository.save(newUser);
        Long savedUserId = savedUser.getUserId();

        applicationProcessRecord.setUserId(savedUser.getUserId());

        if (applicationProcessRecord.getPaymentMethod().equals("全额支付")) {
            applicationProcessRecord.setProcessStatus(6);
        } else {
            applicationProcessRecord.setProcessStatus(5);
        }


        // 回溯设置被邀请人的邀请人关系
        List<ApplicationProcessRecord> pendingInvites = applicationProcessRecordRepository.findAllByInviterName(applicationProcessRecord.getUsername());

        for (ApplicationProcessRecord inviteRecord : pendingInvites) {
            // 仅处理尚未设置邀请人的记录
            if (inviteRecord.getInviterId() == null) {
                inviteRecord.setInviterId(savedUserId);
                inviteRecord.setInviterFullname(fullname);
                applicationProcessRecordRepository.save(inviteRecord);

                // 更新对应的 BackendUser 的邀请人关系，前提是用户已经存在且尚未有邀请人
                Long inviteeId = inviteRecord.getUserId();
                if (inviteeId != null) {
                    backendUserRepository.findById(inviteRecord.getUserId())
                        .ifPresent(u -> {
                            boolean hasInviter = u.getInviterRelationships().stream()
                                .anyMatch(ir -> ir.getStatus() && ir.getInviter() != null);
                            if (!hasInviter) {
                                InviterRelationship newInviterRelationship = InviterRelationship.builder()
                                    .user(u)
                                    .inviter(savedUser)
                                    .startDate(startDate)
                                    .status(true)
                                    .createrId(currentUserId)
                                    .build();
                                u.getInviterRelationships().add(newInviterRelationship);
                                backendUserRepository.save(u);
                            }
                        });
                }
            }
        }

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "审批通过", currentUserId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 新增角色
     * 
     * @param user 后台用户
     * @param roleId 角色ID
     * @param startDate 生效日期
     */
    private void createRole(BackendUser user, Integer roleId, LocalDate startDate) {

        BackendRole role = backendRoleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalStateException("Role not found."));

        // 获取用户的所有角色关系，按开始日期排序
        List<RoleRelationship> existingRoles = user.getRoleRelationships()
            .stream()
            .sorted(Comparator.comparing(RoleRelationship::getStartDate))
            .collect(Collectors.toList());

        RoleRelationship previousRole = null;
        RoleRelationship nextRole = null;
        LocalDate previousEndDate = null;

        for (RoleRelationship existingRole : existingRoles) {
            if (existingRole.getStartDate().isBefore(startDate)) {
                previousRole = existingRole;
            } else if (existingRole.getStartDate().isAfter(startDate)) {
                nextRole = existingRole;
                break;
            } else {
                throw new IllegalStateException("已有相同开始日期的角色存在。");
            }
        }

        // 验证 roleId 顺序
        if (previousRole != null && roleId >= previousRole.getRole().getRoleId()) {
            throw new IllegalArgumentException("新的 角色 必须大于之前的 角色。");
        }
        if (nextRole != null && roleId <= nextRole.getRole().getRoleId()) {
            throw new IllegalArgumentException("新的 角色 必须小于之后的 角色。");
        }

        // 调整之前角色的结束日期
        if (previousRole != null) {
            previousEndDate = previousRole.getEndDate();
            previousRole.setEndDate(startDate.minusDays(1));
        }

        // 确定新角色的结束日期
        LocalDate newEndDate = null;
        if (previousEndDate != null) {
            newEndDate = previousEndDate;
        }
        if (nextRole != null) {
            LocalDate nextStartDateMinus1 = nextRole.getStartDate().minusDays(1);
            if (newEndDate == null || nextStartDateMinus1.isBefore(newEndDate)) {
                newEndDate = nextStartDateMinus1;
            }
        }

        // 创建新的角色关系
        RoleRelationship roleRelationship = RoleRelationship.builder()
            .user(user)
            .role(role)
            .startDate(startDate)
            .endDate(newEndDate)
            .status(true)
            .createrId(authService.getCurrentUserId())
            .build();

        user.getRoleRelationships().add(roleRelationship);
    }

    /**
     * 新增权限
     *
     * @param backendUser 后台用户
     * @param roleId 角色ID
     * @param rateA
     * @param rateB
     */
    private void createPermissions(BackendUser backendUser, Integer roleId, String rateA, String rateB) {

        // 获取用户的所有角色关系
        List<RoleRelationship> roleRelationships = backendUser.getRoleRelationships();

        // 遍历每个角色关系
        for (RoleRelationship roleRelationship : roleRelationships) {
            BackendRole role = roleRelationship.getRole();
            Integer currentRoleId = role.getRoleId();

            // 获取角色的默认权限
            List<RolePermission> rolePermissions = role.getRolePermissions();

            if (rolePermissions.size() < 3) {
                throw new IllegalStateException("Not all RolePermissions found.");
            }

            // 遍历每个权限
            for (RolePermission rp : rolePermissions) {
                final Integer permissionId = rp.getId().getPermissionId();
                double rate1 = rp.getRate1();
                double rate2 = rp.getRate2();
                Boolean status = rp.getIsEnabled();

                // 仅在新增的角色上覆盖费率
                if (currentRoleId.equals(roleId)) {
                    if (permissionId == 1) {
                        if (rateA != null && !rateA.trim().isEmpty()) {
                            double[] rates = parseRates(rateA);
                            if (rates.length > 0) {
                                rate1 = rates.length > 1 ? rates[1] : 0.0;
                                rate2 = rates.length > 2 ? rates[2] : 0.0;
                                status = rate2 > 0.0;
                            }
                        }
                    } else if (permissionId == 2) {
                        if (rateB != null && !rateB.trim().isEmpty()) {
                            double[] rates = parseRates(rateB);
                            if (rates.length > 0) {
                                rate1 = rates.length > 1 ? rates[1] : 0.0;
                                rate2 = rates.length > 2 ? rates[2] : 0.0;
                                status = rate2 > 0.0;
                            }
                        }
                    }
                    // 权限 ID 为 3 的权限，使用系统默认，不需要修改
                }

                // 创建新的权限关系
                PermissionRelationship permissionRelationship = PermissionRelationship.builder()
                    .user(backendUser)
                    .permissionId(permissionId)
                    .roleId(currentRoleId)
                    .rate1(rate1)
                    .rate2(rate2)
                    .startDate(roleRelationship.getStartDate())
                    .endDate(roleRelationship.getEndDate())
                    .status(status)
                    .createrId(authService.getCurrentUserId())
                    .build();

                backendUser.getPermissionRelationships().add(permissionRelationship);
            }
        }
    }

    /**
     * 解析 rate 字符串为 double 数组
     *
     * @param rate rate 字符串，例如 "1.0*2.0"
     * @return double 数组
     */
    private double[] parseRates(String rate) {
        return Arrays.stream(rate.split("\\*"))
                 .mapToDouble(this::parseDoubleSafe)
                 .toArray();
    }

    /**
     * 安全地将字符串解析为 double
     *
     * @param value 字符串值
     * @return double 值，如果解析失败返回0.0
     */
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("无法解析的数字: {}", value);
            return 0.0;
        }
    }

    /**
     * 申请链接
     *
     * @param request 提交申请请求DTO
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean submitForLink(ApplicationSubmitForLinkDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 3)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot submit this application for link.");
        }

        Long platformId = request.getPlatformId();
        TbUser tbUser = localTbUserRepository.findByUserId(platformId)
                .orElseThrow(() -> new IllegalStateException("Platform user not found."));

        String username = request.getUsername().trim();
        if (!tbUser.getPhone().equals(username)) {
            throw new IllegalStateException("Platform account not matched.");
        }

        if (userService.userExists(username)) {
            throw new IllegalStateException("Username already exists.");
        }

        applicationProcessRecord.setPlatformId(platformId);
        applicationProcessRecord.setUsername(username);
        applicationProcessRecord.setTiktokAccount(request.getTiktokAccount());
        applicationProcessRecord.setProcessStatus(4);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交申请链接", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
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
            manager = manager.getManager();
        }
        return false;
    }
        

    /**
     * 完成申请
     *
     * @param processId 流程单ID
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean finishedApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 5)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();

        if (!(roleId == 1 || (roleId == 2 && isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found.")))))) {
            throw new IllegalStateException("You cannot submit this application for link.");
        }

        applicationProcessRecord.setProcessStatus(6);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "完成申请", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 取消流程单（未提交）
     */
    @Override
    @Transactional
    public Boolean cancelApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        applicationProcessRecord.setProcessStatus(0);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "取消申请", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 激活流程单
     */
    @Override
    @Transactional
    public Boolean activateApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 0)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();

        if (!(authService.getCurrentUserRoleId() == 1 || isManager(
            userId,
            backendUserRepository.findById(applicationProcessRecord.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."))))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        applicationProcessRecord.setProcessStatus(1);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "激活申请", userId, authService.getCurrentUsername(), authService.getCurrentFullname(),
            request.getComments(), applicationProcessRecord
        ));

        return true;
    }

    /**
     * 检查用户姓名是否存在
     *
     * @param fullname 用户姓名
     * @return 
     */
    @Override
    @Transactional(readOnly = true)
    public String checkFullname(String fullname) {
        if (fullname == null || fullname.trim().isEmpty()) {
            return("Fullname is required.");
        }

        List<ApplicationProcessRecord> applicationProcessRecord = applicationProcessRecordRepository.findByFullname(fullname);
        
        if (applicationProcessRecord.isEmpty()) {
            return "success";
        } else {
            return fullname + " 已被 " + applicationProcessRecord.get(0).getManagerFullname() + " 创建<br>" +
                "流程单ID: " + applicationProcessRecord.get(0).getProcessId().toString() + "<br>" +
                "创建时间: " + applicationProcessRecord.get(0).getCreatedAt().toString();
        }
    }

    /**
     * 查看申请
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationInfoResponseDTO> viewApplication(Long processId) {
        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();
        Long managerId = applicationProcessRecord.getManagerId();

        if (!(roleId == 1 || roleId == 8 || isManager(
            userId,
            backendUserRepository.findById(managerId)
                .orElseThrow(() -> new IllegalStateException("Manager not found.")))) ) {
            throw new IllegalStateException("You cannot view this application.");
        }

        Long platformId = applicationProcessRecord.getPlatformId();
        Optional<TbUser> tbUser = localTbUserRepository.findByUserId(platformId);
        String invitaionCode = tbUser.map(TbUser::getInvitationCode).orElse(null);

        String inviteName = applicationProcessRecord.getInviterName();
        Optional<TbUser> inviter = localTbUserRepository.findByPhone(inviteName);
        String inviterCode = inviter.map(TbUser::getInvitationCode).orElse(null);

        ApplicationInfoResponseDTO response = ApplicationInfoResponseDTO.builder()
                .processId(applicationProcessRecord.getProcessId())
                .fullname(applicationProcessRecord.getFullname())
                .roleId(applicationProcessRecord.getRoleId())
                .invitationCode(invitaionCode)
                .projectName(applicationProcessRecord.getProjectName())
                .projectAmount(applicationProcessRecord.getProjectAmount())
                .inviterId(applicationProcessRecord.getInviterId())
                .inviterName(inviteName)
                .inviterFullname(applicationProcessRecord.getInviterFullname())
                .inviterCode(inviterCode)
                .managerId(applicationProcessRecord.getManagerId())
                .managerName(applicationProcessRecord.getManagerName())
                .managerFullname(applicationProcessRecord.getManagerFullname())
                .createrId(applicationProcessRecord.getCreaterId())
                .createrName(applicationProcessRecord.getCreaterName())
                .createrFullname(applicationProcessRecord.getCreaterFullname())
                .rateA(applicationProcessRecord.getRateA())
                .rateB(applicationProcessRecord.getRateB())
                .startDate(applicationProcessRecord.getStartDate())
                .paymentMethod(applicationProcessRecord.getPaymentMethod())
                .paidStr(applicationProcessRecord.getPaidStr())
                .regionName(applicationProcessRecord.getRegionName())
                .currencyName(applicationProcessRecord.getCurrencyName())
                .comments(applicationProcessRecord.getComments())
                .processStatus(applicationProcessRecord.getProcessStatus())
                .platformId(platformId)
                .username(applicationProcessRecord.getUsername())
                .tiktokAccount(applicationProcessRecord.getTiktokAccount())
                .createdAt(applicationProcessRecord.getCreatedAt())
                .actionStr(applicationProcessRecord.getActionStr())
                .applicationFlowRecordDtos(applicationProcessRecord.getApplicationFlowRecords().stream()
                        .map(flowRecord -> ApplicationFlowRecordDTO.builder()
                                .flowId(flowRecord.getFlowId())
                                .action(flowRecord.getAction())
                                .createrId(flowRecord.getCreaterId())
                                .createrName(flowRecord.getCreaterName())
                                .createrFullname(flowRecord.getCreaterFullname())
                                .comments(flowRecord.getComments())
                                .createdAt(flowRecord.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .applicationPaymentRecordDtos(applicationProcessRecord.getApplicationPaymentRecords().stream()
                        .map(paymentRecord -> ApplicationPaymentRecordDTO.builder()
                                .paymentId(paymentRecord.getPaymentId())
                                .projectName(paymentRecord.getProjectName())
                                .projectAmount(paymentRecord.getProjectAmount())
                                .paymentMethod(paymentRecord.getPaymentMethod())
                                .paymentAmount(paymentRecord.getPaymentAmount())
                                .fee(paymentRecord.getFee())
                                .actual(paymentRecord.getActual())
                                .paymentDate(paymentRecord.getPaymentTime())
                                .status(paymentRecord.getStatus())
                                .createrId(paymentRecord.getCreaterId())
                                .createrName(paymentRecord.getCreaterName())
                                .createrFullname(paymentRecord.getCreaterFullname())
                                .financeId(paymentRecord.getFinanceId())
                                .financeName(paymentRecord.getFinanceName())
                                .financeFullname(paymentRecord.getFinanceFullname())
                                .financeApprovalTime(paymentRecord.getFinanceApprovalTime())
                                .regionName(paymentRecord.getRegionName())
                                .currencyName(paymentRecord.getCurrencyName())
                                .comments(paymentRecord.getComments())
                                .createdAt(paymentRecord.getCreatedAt())
                                .status(paymentRecord.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return Optional.of(response);
    }

    /**
     * 搜索方法，使用Specification，本人待办申请
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ViewApplicationResponseDTO> searchTodoApplications(ApplicationSearchDTO request) {
        // 获取当前用户信息
        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();

        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("processId").ascending());

        // 根据角色获取可见的创建者ID
        List<Integer> allowedProcessStatuses = new ArrayList<>();
        Set<Long> subordinateIds = new HashSet<>();
        switch (roleId) {
            case 1:
                allowedProcessStatuses.addAll(Arrays.asList(4, 6, 99));
                break;
            case 2:
                allowedProcessStatuses.addAll(Arrays.asList(4, 99));
                subordinateIds.add(userId);
                subordinateIds.addAll(getAllSubordinateIds(List.of(userId), new HashSet<>()));
                break;
            case 3:
                allowedProcessStatuses.addAll(Arrays.asList(1, 3, 5));
                subordinateIds.add(userId);
                break;
            case 8:
                allowedProcessStatuses.addAll(Arrays.asList(2, 5));
                break;
            default:
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Integer> processStatuses = request.getProcessStatuses();
        if (processStatuses.isEmpty()) {
            processStatuses.addAll(allowedProcessStatuses);
        } else {
            processStatuses.retainAll(allowedProcessStatuses);
        }

        // 构建查询规范
        Specification<ApplicationProcessRecord> spec = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // 根据角色过滤创建者ID
            if (roleId == 2 || roleId == 3) {
                predicates.add(root.get("managerId").in(subordinateIds));
            }

            // 根据processId过滤
            if (request.getProcessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("processId"), request.getProcessId()));
            }

            // 根据userId过滤
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), request.getUserId()));
            }

            // 根据username过滤
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + request.getUsername().trim().toLowerCase() + "%"));
            }

            // 根据fullname过滤
            if (request.getFullname() != null && !request.getFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), "%" + request.getFullname().toLowerCase() + "%"));
            }

            // 根据roleId过滤
            if (request.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roleId"), request.getRoleId()));
            }

            // 根据platformId过滤
            if (request.getPlatformId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("platformId"), request.getPlatformId()));
            }

            // 根据inviterId过滤
            if (request.getInviterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("inviterId"), request.getInviterId()));
            }

            // 根据inviterName过滤
            if (request.getInviterName() != null && !request.getInviterName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("inviterName")), "%" + request.getInviterName().trim().toLowerCase() + "%"));
            }

            // 根据inviterFullname过滤
            if (request.getInviterFullname() != null && !request.getInviterFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("inviterFullname")), "%" + request.getInviterFullname().toLowerCase() + "%"));
            }

            // 根据managerId过滤
            if (request.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("managerId"), request.getManagerId()));
            }

            // 根据managerName过滤
            if (request.getManagerName() != null && !request.getManagerName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("managerName")), "%" + request.getManagerName().trim().toLowerCase() + "%"));
            }

            // 根据managerFullname过滤
            if (request.getManagerFullname() != null && !request.getManagerFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("managerFullname")), "%" + request.getManagerFullname().toLowerCase() + "%"));
            }

            // 根据createrId过滤
            if (request.getCreaterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createrId"), request.getCreaterId()));
            }

            // 根据createrName过滤
            if (request.getCreaterName() != null && !request.getCreaterName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("createrName")), "%" + request.getCreaterName().trim().toLowerCase() + "%"));
            }

            // 根据createrFullname过滤
            if (request.getCreaterFullname() != null && !request.getCreaterFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("createrFullname")), "%" + request.getCreaterFullname().toLowerCase() + "%"));
            }

            // 根据tiktokAccount过滤
            if (request.getTiktokAccount() != null && !request.getTiktokAccount().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("tiktokAccount")), "%" + request.getTiktokAccount().trim().toLowerCase() + "%"));
            }

            // 根据regionName过滤
            if (request.getRegionName() != null && !request.getRegionName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("regionName")), "%" + request.getRegionName().toLowerCase() + "%"));
            }

            // 根据currency过滤
            if (request.getCurrencyName() != null && !request.getCurrencyName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("currencyName")), request.getCurrency().toLowerCase()));
            }

            // 根据projectName过滤
            if (request.getProjectName() != null && !request.getProjectName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("projectName")), "%" + request.getProjectName().toLowerCase() + "%"));
            }

            // 根据paymentMethod过滤
            if (request.getPaymentMethod() != null && !request.getPaymentMethod().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), "%" + request.getPaymentMethod().toLowerCase() + "%"));
            }

            // 根据inviterCode过滤
            if (request.getInviterCode() != null) {
                List<Long> platformIds = localTbUserRepository.findUserIdByInviterCode(request.getInviterCode());
                if (platformIds.isEmpty()) {
                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("platformId"), 0));
                }
                predicates.add(root.get("platformId").in(platformIds));
            }

            // 根据invitationCode过滤
            if (request.getInvitationCode() != null) {
                Long platformId = localTbUserRepository.findUserIdByInvitationCode(request.getInvitationCode());
                if (platformId == null) {
                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("platformId"), 0));
                }
                predicates.add(criteriaBuilder.equal(root.get("platformId"), platformId));
            }

            // 根据processStatuses过滤
            if (!processStatuses.isEmpty()) {
                predicates.add(root.get("processStatus").in(processStatuses));
            } else {
                return criteriaBuilder.and(criteriaBuilder.equal(root.get("processId"), 0));
            }

            // 特殊条件：roleId=8 且 processStatus=5 时，存在 ApplicationPaymentRecord.status=false
            if (roleId == 8) {
                // 创建一个子查询来检查 ApplicationPaymentRecord 是否存在 status=false
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<ApplicationPaymentRecord> paymentRoot = subquery.from(ApplicationPaymentRecord.class);
                subquery.select(paymentRoot.get("paymentId"))
                        .where(
                            criteriaBuilder.equal(paymentRoot.get("applicationProcessRecord").get("processId"), root.get("processId")),
                            criteriaBuilder.isFalse(paymentRoot.get("status"))
                        );

                // 当 processStatus=5 时，确保子查询有结果
                Predicate status5Predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("processStatus"), 5),
                    criteriaBuilder.exists(subquery)
                );

                // 当 processStatus=2 时，无需额外条件
                Predicate status2Predicate = criteriaBuilder.equal(root.get("processStatus"), 2);

                // 合并两个条件
                predicates.add(
                    criteriaBuilder.or(status2Predicate, status5Predicate)
                );
            }

            // 根据effectiveAfter过滤
            if (request.getStartAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveDate"), request.getStartAfter()));
            }

            // 根据effectiveBefore过滤
            if (request.getStartBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("effectiveDate"), request.getStartBefore()));
            }

            // 根据createdAfter过滤
            if (request.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter().atStartOfDay()));
            }

            // 根据createdBefore过滤
            if (request.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedBefore().atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ApplicationProcessRecord> resultPage = applicationProcessRecordRepository.findAll(spec, pageable);

        // 将查询结果映射为 ViewApplicationResponseDTO
        List<ViewApplicationResponseDTO> dtoList = resultPage.getContent().stream()
            .map(record -> ViewApplicationResponseDTO.builder()
                .processId(record.getProcessId())
                .userId(record.getUserId())
                .username(record.getUsername())
                .fullname(record.getFullname())
                .platformId(record.getPlatformId())
                .roleId(record.getRoleId())
                .inviterId(record.getInviterId())
                .inviterName(record.getInviterName())
                .inviterFullname(record.getInviterFullname())
                .managerId(record.getManagerId())
                .managerName(record.getManagerName())
                .managerFullname(record.getManagerFullname())
                .rateA(record.getRateA())
                .rateB(record.getRateB())
                .startDate(record.getStartDate())
                .tiktokAccount(record.getTiktokAccount())
                .regionName(record.getRegionName())
                .currencyName(record.getCurrencyName())
                .projectName(record.getProjectName())
                .projectAmount(record.getProjectAmount())
                .paymentMethod(record.getPaymentMethod())
                .paidStr(record.getPaidStr())
                .processStatus(record.getProcessStatus())
                .createdAt(record.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        // 返回分页结果
        return new PageImpl<>(dtoList, pageable, resultPage.getTotalElements());
    }

    /**
     * 搜索方法，使用Specification
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ViewApplicationResponseDTO> searchApplications(ApplicationSearchDTO request) {
        // 获取当前用户信息
        Long userId = authService.getCurrentUserId();
        Integer roleId = authService.getCurrentUserRoleId();

        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("processId").ascending());

        // 根据角色获取可见的创建者ID,包括自己和下属
        Set<Long> subordinateIds = new HashSet<>();
        if (roleId == 2 || roleId == 3) {
            subordinateIds.add(userId);
            subordinateIds.addAll(getAllSubordinateIds(List.of(userId), new HashSet<>()));
        }

        // 构建查询规范
        Specification<ApplicationProcessRecord> spec = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // 根据角色过滤创建者ID
            if (roleId == 2 || roleId == 3) {
                predicates.add(root.get("managerId").in(subordinateIds));
            }

            // 根据processId过滤
            if (request.getProcessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("processId"), request.getProcessId()));
            }

            // 根据userId过滤
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), request.getUserId()));
            }

            // 根据username过滤
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + request.getUsername().trim().toLowerCase() + "%"));
            }

            // 根据fullname过滤
            if (request.getFullname() != null && !request.getFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), "%" + request.getFullname().toLowerCase() + "%"));
            }

            // 根据platformId过滤
            if (request.getPlatformId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("platformId"), request.getPlatformId()));
            }

            // 根据roleId过滤
            if (request.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roleId"), request.getRoleId()));
            }

            // 根据inviterId过滤
            if (request.getInviterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("inviterId"), request.getInviterId()));
            }

            // 根据inviterName过滤
            if (request.getInviterName() != null && !request.getInviterName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("inviterName")), "%" + request.getInviterName().trim().toLowerCase() + "%"));
            }

            // 根据inviterFullname过滤
            if (request.getInviterFullname() != null && !request.getInviterFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("inviterFullname")), "%" + request.getInviterFullname().toLowerCase() + "%"));
            }

            // 根据managerId过滤
            if (request.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("managerId"), request.getManagerId()));
            }

            // 根据managerName过滤
            if (request.getManagerName() != null && !request.getManagerName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("managerName")), "%" + request.getManagerName().trim().toLowerCase() + "%"));
            }

            // 根据managerFullname过滤
            if (request.getManagerFullname() != null && !request.getManagerFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("managerFullname")), "%" + request.getManagerFullname().toLowerCase() + "%"));
            }

            // 根据createrId过滤
            if (request.getCreaterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createrId"), request.getCreaterId()));
            }

            // 根据createrName过滤
            if (request.getCreaterName() != null && !request.getCreaterName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("createrName")), "%" + request.getCreaterName().trim().toLowerCase() + "%"));
            }

            // 根据createrFullname过滤
            if (request.getCreaterFullname() != null && !request.getCreaterFullname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("createrFullname")), "%" + request.getCreaterFullname().toLowerCase() + "%"));
            }

            // 根据tiktokAccount过滤
            if (request.getTiktokAccount() != null && !request.getTiktokAccount().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("tiktokAccount")), "%" + request.getTiktokAccount().toLowerCase() + "%"));
            }

            // 根据regionName过滤
            if (request.getRegionName() != null && !request.getRegionName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("regionName")), "%" + request.getRegionName().toLowerCase() + "%"));
            }

            // 根据currency过滤
            if (request.getCurrencyName() != null && !request.getCurrencyName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("currencyName")), request.getCurrency().toLowerCase()));
            }

            // 根据projectName过滤
            if (request.getProjectName() != null && !request.getProjectName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("projectName")), "%" + request.getProjectName().toLowerCase() + "%"));
            }

            // 根据paymentMethod过滤
            if (request.getPaymentMethod() != null && !request.getPaymentMethod().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), "%" + request.getPaymentMethod().toLowerCase() + "%"));
            }

            // 根据inviterCode过滤
            if (request.getInviterCode() != null) {
                List<Long> platformIds = localTbUserRepository.findUserIdByInviterCode(request.getInviterCode());
                if (platformIds.isEmpty()) {
                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("platformId"), 0));
                }
                predicates.add(root.get("platformId").in(platformIds));
            }

            // 根据invitationCode过滤
            if (request.getInvitationCode() != null) {
                Long platformId = localTbUserRepository.findUserIdByInvitationCode(request.getInvitationCode());
                if (platformId == null) {
                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("platformId"), 0));
                }
                predicates.add(criteriaBuilder.equal(root.get("platformId"), platformId));
            }

            // 根据processStatuses过滤
            if (!request.getProcessStatuses().isEmpty()) {
                predicates.add(root.get("processStatus").in(request.getProcessStatuses()));
            }

            // 根据effectiveAfter过滤
            if (request.getStartAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveDate"), request.getStartAfter()));
            }

            // 根据effectiveBefore过滤
            if (request.getStartBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("effectiveDate"), request.getStartBefore()));
            }

            // 根据createdAfter过滤
            if (request.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter().atStartOfDay()));
            }

            // 根据createdBefore过滤
            if (request.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedBefore().atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ApplicationProcessRecord> resultPage = applicationProcessRecordRepository.findAll(spec, pageable);

        // 将查询结果映射为 ViewApplicationResponseDTO
        List<ViewApplicationResponseDTO> dtoList = resultPage.getContent().stream()
            .map(record -> ViewApplicationResponseDTO.builder()
                .processId(record.getProcessId())
                .userId(record.getUserId())
                .username(record.getUsername())
                .fullname(record.getFullname())
                .platformId(record.getPlatformId())
                .roleId(record.getRoleId())
                .inviterId(record.getInviterId())
                .inviterName(record.getInviterName())
                .inviterFullname(record.getInviterFullname())
                .managerId(record.getManagerId())
                .managerName(record.getManagerName())
                .managerFullname(record.getManagerFullname())
                .rateA(record.getRateA())
                .rateB(record.getRateB())
                .startDate(record.getStartDate())
                .tiktokAccount(record.getTiktokAccount())
                .regionName(record.getRegionName())
                .currencyName(record.getCurrencyName())
                .projectName(record.getProjectName())
                .projectAmount(record.getProjectAmount())
                .paymentMethod(record.getPaymentMethod())
                .paidStr(record.getPaidStr())
                .processStatus(record.getProcessStatus())
                .createdAt(record.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        // 返回分页结果
        return new PageImpl<>(dtoList, pageable, resultPage.getTotalElements());
    }


    // Helper方法：获取所有下级用户ID（递归）
    private Set<Long> getAllSubordinateIds(List<Long> managerIds, Set<Long> visited) {
        Set<Long> subordinateIds = new HashSet<>();
        List<Long> directSubordinateIds = backendUserRepository.findUserIdsByManagerIds(managerIds);
        for (Long id : directSubordinateIds) {
            if (!visited.contains(id)) {
                visited.add(id);
                subordinateIds.add(id);
                subordinateIds.addAll(getAllSubordinateIds(Collections.singletonList(id), visited));
            }
        }
        return subordinateIds;
    }

    private ApplicationPaymentRecord createPaymentRecord(
            String regionName, String currencyName, String projectName, Double projectAmout, String paymentMethod,
            Double paymentAmount, Double fee, LocalDate paymentDate, Long createrId, String createrUsername,
            String createrFullname, String comments, ApplicationProcessRecord applicationProcessRecord) {
        return ApplicationPaymentRecord.builder()
            .regionName(regionName)
            .currencyName(currencyName)
            .projectName(projectName)
            .projectAmount(projectAmout)
            .paymentMethod(paymentMethod)
            .paymentAmount(paymentAmount)
            .fee(fee)
            .actual(paymentAmount - fee)
            .paymentDate(paymentDate)
            .createrId(createrId)
            .createrName(createrUsername)
            .createrFullname(createrFullname)
            .comments(comments)
            .applicationProcessRecord(applicationProcessRecord)
            .build();
    }

    private ApplicationFlowRecord createFlowRecord(
            String action, Long createrId, String createrUsername, String createrFullname, String comments,
            ApplicationProcessRecord applicationProcessRecord) {
        return ApplicationFlowRecord.builder()
            .action(action)
            .createrId(createrId)
            .createrName(createrUsername)
            .createrFullname(createrFullname)
            .comments(comments)
            .applicationProcessRecord(applicationProcessRecord)
            .build();
    }

    private void uploadFiles(String path, MultipartFile[] files) {
        try {
            fileStorageService.uploadFiles(path, files);
        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage());
            throw new IllegalStateException("File upload failed.");
        }
    }

    /**
     * 提取 status=true 的 ApplicationPaymentRecords，按 currencyName 分类统计 paymentAmount 之和，
     * 并拼接成以 "<br>" 分割的字符串，格式为 "{totalAmount} {currencyName}"
     *
     * @return 拼接后的字符串
     */
    private void getTotalPaymentAmountByCurrency(Long processId) {
        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        List<Object[]> results = applicationPaymentRecordRepository.findTotalPaymentAmountByCurrencyAndProcessId(processId);
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##"); // 格式化小数点，保留两位

        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            String currencyName = (String) row[0];
            Double totalAmount = (Double) row[1];
            sb.append(df.format(totalAmount)).append(" ").append(currencyName);
            if (i < results.size() - 1) {
                sb.append("<br>");
            }
        }

        applicationProcessRecord.setPaidStr(sb.toString());
        applicationProcessRecordRepository.save(applicationProcessRecord);
    }

}
