package com.rsmanager.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rsmanager.dto.application.*;
import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.security.UserContext;
import com.rsmanager.service.*;
import com.rsmanager.utils.InstantAdapter;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserContext userContext;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationPaymentRecordRepository applicationPaymentRecordRepository;
    private final ApplicationProcessRecordRepository applicationProcessRecordRepository;
    private final BackendUserRepository backendUserRepository;
    private final LocalTbUserRepository localTbUserRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final RegionCurrencyRepository regionCurrencyRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Value("${file.upload.base-path}")
    private String baseUploadPath;

    /**
     * 创建新的申请记录
     */
    @Override
    @Transactional
    public Long createApplication(ApplicationCreateRequestDTO request, MultipartFile[] files) {

        BackendUser operator = userContext.getOperator();

        String regionName = request.getRegionName();
        String currencyName = request.getCurrencyName();
        String comments = request.getComments();

        String managerName = request.getManagerName().trim();
        BackendUser manager = backendUserRepository.findByUsername(managerName)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        String inviterName = request.getInviterName().trim();
        Optional<BackendUser> inviter = backendUserRepository.findByUsername(inviterName);

        LocalDate paymentDate = request.getPaymentDate();

        String currencyCode = regionCurrencyRepository.findCurrencyCodeByCurrencyName(currencyName)
                .orElseThrow(() -> new IllegalStateException("Currency not found."));

        ApplicationProcessRecord applicationProcessRecord = ApplicationProcessRecord.builder()
                .fullname(request.getFullname().trim())
                .roleId(request.getRoleId())
                .projectName(request.getProjectName())
                .projectAmount(request.getProjectAmount())
                .inviter(inviter.orElse(null))
                .inviterName(inviterName)
                .manager(manager)
                .creater(operator)
                .rateA(request.getRateA())
                .rateB(request.getRateB())
                .startDate(paymentDate)
                .paymentMethod(request.getPaymentMethod())
                .regionName(regionName)
                .currencyName(currencyName)
                .currencyCode(currencyCode)
                .comments(comments)
                .processStatus(1)
                .build();

        // For applicationPaymentRecords
        List<ApplicationPaymentRecord> paymentRecords = new ArrayList<>();
        paymentRecords.add(createPaymentRecord(
            regionName, currencyName, currencyCode, request.getProjectName(), request.getProjectAmount(),
            request.getPaymentMethod(), request.getPaymentAmount(), request.getFee(), paymentDate, operator,
            comments, request.getPaymentAccountId(), applicationProcessRecord
        ));

        // For applicationFlowRecords
        List<ApplicationFlowRecord> flowRecords = new ArrayList<>();
        flowRecords.add(createFlowRecord(
            "创建申请单", operator, comments, applicationProcessRecord
        ));
        flowRecords.add(createFlowRecord(
            "创建支付记录", operator, comments, applicationProcessRecord
        )); 

        applicationProcessRecord.setApplicationPaymentRecords(paymentRecords);
        applicationProcessRecord.setApplicationFlowRecords(flowRecords);

        ApplicationProcessRecord savedRecord = applicationProcessRecordRepository.save(applicationProcessRecord);

        try {
            fileStorageService.uploadFiles("applications/" + savedRecord.getProcessId() + "/payments/" + savedRecord.getApplicationPaymentRecords().get(0).getPaymentId(), files);
        } catch (Exception e) {
            applicationProcessRecordRepository.delete(savedRecord);
            throw new IllegalStateException("Failed to upload files for application.");
        }

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return savedRecord.getProcessId();
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

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        applicationProcessRecord.setProcessStatus(0);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "取消申请", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

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

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        applicationProcessRecord.setProcessStatus(1);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "激活申请", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 删除流程单
     */
    @Override
    @Transactional
    public Boolean deleteApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 0)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot delete this application.");
        }

        applicationProcessRecord.getApplicationPaymentRecords().stream()
            .filter(p -> p.getStatus().equals(1))
            .findAny()
            .ifPresent(p -> {
                throw new IllegalStateException("Cannot delete application with payment records.");
            });

        applicationProcessRecordRepository.delete(applicationProcessRecord);

        return true;
    }

    /**
     * 更新申请
     */
    @Override
    @Transactional
    public Boolean updateApplication(ApplicationProcessUpdateDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 1)      
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        String managerName = request.getManagerName().trim();
        BackendUser manager = backendUserRepository.findByUsername(managerName)
            .orElseThrow(() -> new IllegalStateException("Manager not found."));

        String inviterName = request.getInviterName().trim();
        Optional<BackendUser> inviter = backendUserRepository.findByUsername(inviterName);

        String currencyName = request.getCurrencyName();
        String currencyCode = regionCurrencyRepository.findCurrencyCodeByCurrencyName(currencyName)
                .orElseThrow(() -> new IllegalStateException("Currency not found."));

        applicationProcessRecord.setFullname(request.getFullname());
        applicationProcessRecord.setInviterName(inviterName);
        applicationProcessRecord.setInviter(inviter.orElse(null));
        applicationProcessRecord.setManager(manager);
        applicationProcessRecord.setRoleId(request.getRoleId());
        applicationProcessRecord.setRegionName(request.getRegionName());
        applicationProcessRecord.setCurrencyName(currencyName);
        applicationProcessRecord.setCurrencyCode(currencyCode);
        applicationProcessRecord.setProjectName(request.getProjectName());
        applicationProcessRecord.setProjectAmount(request.getProjectAmount());
        applicationProcessRecord.setRateA(request.getRateA());
        applicationProcessRecord.setRateB(request.getRateB());
        applicationProcessRecord.setStartDate(request.getStartDate());
        applicationProcessRecord.setPaymentMethod(request.getPaymentMethod());
        applicationProcessRecord.setComments(request.getComments());

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "更新申请单", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

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

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot submit this application.");
        }

        applicationProcessRecord.setProcessStatus(2);

        String comments = request.getComments();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
            .filter(p -> p.getStatus().equals(0) || p.getStatus().equals(3))
            .forEach(p -> {
                p.setStatus(2);
                p.setComments(comments);
            });

        
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交财务审核", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 撤回审核
     */
    @Override
    @Transactional
    public Boolean withdrawApplication(ApplicationActionDTO request) {

        List<Integer> statusList = Arrays.asList(2, 4, 88, 98);

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> (statusList.contains(r.getProcessStatus())))
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer status = applicationProcessRecord.getProcessStatus();

        BackendUser operator = userContext.getOperator();
        Integer roleId = userContext.getRoleId();

        if (roleId == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager())) {
        } else if (roleId.equals(8) && (status.equals(2) || status.equals(88) || status.equals(98))) {
        } else {
            throw new IllegalStateException("You cannot withdraw this application: " + status);
        }

        applicationProcessRecord.setProcessStatus(status - 1);
        applicationProcessRecord.setUsername(null);
        applicationProcessRecord.setTiktokAccount(null);

        String comments = request.getComments();

        applicationProcessRecord.setComments(comments);

        applicationProcessRecord.getApplicationPaymentRecords().stream()
            .filter(p -> p.getStatus().equals(2))
            .forEach(p -> {
                p.setStatus(3);
                p.setComments(comments);
            });
        
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "撤回审核", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
    * 财务审批申请
    */
   @Override
   @Transactional
   public Boolean approveFinanceApplication(ApplicationActionDTO request) {

       ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
               .filter(r -> (r.getProcessStatus() == 2) && r.getApplicationPaymentRecords().stream().allMatch(p -> p.getStatus().equals(1)))
               .orElseThrow(() -> new IllegalStateException("Application not found."));

       Integer roleId = userContext.getRoleId();

       if (!(roleId == 1 || roleId == 8)) {
           throw new IllegalStateException("You cannot approve this application.");
       }

       applicationProcessRecord.setProcessStatus(3);

       applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
           "财务审核通过", userContext.getOperator(), request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

       return true;
   }

    /**
    * 申请链接
    */
    @Override
    @Transactional
    public Boolean submitForLink(ApplicationSubmitForLinkDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 3)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot submit this application for link.");
        }

        String username = request.getUsername().trim();

        TbUser tbUser = localTbUserRepository.findByPhone(username)
                .orElseThrow(() -> new IllegalStateException("Platform user not found."));

        BackendUser existUser = backendUserRepository.findByUsername(username)
            .orElse(null);

        if (existUser != null) {
            throw new IllegalStateException("User already exists.");
        }

        applicationProcessRecord.setTbUser(tbUser);
        applicationProcessRecord.setUsername(username);
        applicationProcessRecord.setTiktokAccount(request.getTiktokAccount());
        applicationProcessRecord.setProcessStatus(4);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交申请链接", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
    * 链接审批申请
    */
    @Override
    @Transactional
    public Boolean approvelink(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 4)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Long operatorId = operator.getUserId();

        if (!(userContext.getRoleId() == 1 || isManager(operatorId, applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        String username = applicationProcessRecord.getUsername();
        String fullname = applicationProcessRecord.getFullname();

        BackendUser existUser = backendUserRepository.findByUsername(username)
            .orElse(null);

        if (existUser != null) {
            throw new IllegalStateException("User already exists.");
        }

        BackendUser newUser = BackendUser.builder()
            .username(username)
            .password(passwordEncoder.encode("123456"))
            .fullname(fullname)
            .regionName(applicationProcessRecord.getRegionName())
            .currencyName(applicationProcessRecord.getCurrencyName())
            .tbUser(applicationProcessRecord.getTbUser())
            .applicationProcessRecordAsUser(applicationProcessRecord)
            .status(true)
            .build();

        createRoleAndPermissions(newUser, applicationProcessRecord);

        String tiktokAccountString = applicationProcessRecord.getTiktokAccount();
        if (tiktokAccountString == null || tiktokAccountString.trim().isEmpty()) {
            throw new IllegalStateException("TikTok account cannot be null or empty.");
        }

        LocalDate startDate = applicationProcessRecord.getStartDate();

        TiktokRelationship tiktokRelationship = TiktokRelationship.builder()
            .user(newUser)
            .tiktokAccount(tiktokAccountString)
            .startDate(startDate)
            .status(true)
            .createrId(operatorId)
            .build();

        newUser.getTiktokRelationships().add(tiktokRelationship);

        CreaterRelationship createrRelationship = CreaterRelationship.builder()
            .user(newUser)
            .creater(operator)
            .startDate(startDate)
            .build();

        newUser.getCreaterRelationships().add(createrRelationship);

        BackendUser inviter = applicationProcessRecord.getInviter();

        if (inviter != null) {
            InviterRelationship inviterRelationship = InviterRelationship.builder()
                .user(newUser)
                .inviter(inviter)
                .startDate(startDate)
                .status(true)
                .createrId(operatorId)
                .build();

            newUser.getInviterRelationships().add(inviterRelationship);
        } else {
            inviter = backendUserRepository.findByUsername(applicationProcessRecord.getInviterName())
                .orElse(null);
            if (inviter != null) {
                InviterRelationship inviterRelationship = InviterRelationship.builder()
                    .user(newUser)
                    .inviter(inviter)
                    .startDate(startDate)
                    .status(true)
                    .createrId(operatorId)
                    .build();

                newUser.getInviterRelationships().add(inviterRelationship);
                applicationProcessRecord.setInviter(inviter);
            }
        }

        ManagerRelationship managerRelationship = ManagerRelationship.builder()
            .user(newUser)
            .manager(applicationProcessRecord.getManager())
            .startDate(startDate)
            .status(true)
            .createrId(operatorId)
            .build();

        newUser.getManagerRelationships().add(managerRelationship);

        BackendUser savedUser = backendUserRepository.save(newUser);

        applicationProcessRecord.setUser(savedUser);

        if (applicationProcessRecord.getPaymentMethod().equals("全额支付")) {
            applicationProcessRecord.setProcessStatus(6);
        } else {
            applicationProcessRecord.setProcessStatus(5);
        }

        trackingInviter(savedUser);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "审批通过", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 完成申请
     */
    @Override
    @Transactional
    public Boolean finishedApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 5)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Integer roleId = userContext.getRoleId();

        if (!(roleId == 1 || (roleId == 2 && isManager(operator.getUserId(), applicationProcessRecord.getManager())))) {
            throw new IllegalStateException("You cannot submit this application for link.");
        }

        applicationProcessRecord.setProcessStatus(6);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "完成申请", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 归档申请
     */
    @Override
    @Transactional
    public Boolean archiveApplication(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 6)      
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        if (!(userContext.getRoleId() == 1)) {
            throw new IllegalStateException("You cannot archive this application.");
        }

        applicationProcessRecord.setProcessStatus(7);
        
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "归档申请", userContext.getOperator(), request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 退款申请
     */
    @Override
    @Transactional
    public Boolean refundApplication(ApplicationActionDTO request) {

        List<Integer> statusList = Arrays.asList(5, 6, 7);

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> statusList.contains(r.getProcessStatus()))
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || userContext.getRoleId() == 8)) {
            throw new IllegalStateException("You cannot refund this application.");
        }

        BackendUser user = applicationProcessRecord.getUser();
        user.setStatus(false);
        user.getRolePermissionRelationships().forEach(r -> {
            r.setStatus(false);
        });
        user.getTiktokRelationships().forEach(r -> {
            r.setStatus(false);
        });

        backendUserRepository.save(user);

        applicationProcessRecord.getApplicationPaymentRecords().forEach(p -> {
            p.setStatus(-1);
        });

        applicationProcessRecord.setProcessStatus(-1);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "退款申请", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 切换为补充角色编辑态 upgraderoleediting
     */
    @Override
    @Transactional
    public Boolean addRoleEditing(ActionStrDTO request) {

        Long processId = request.getProcessId();

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        Integer roleId = applicationProcessRecord.getRoleId();

        if (roleId == 6) {
            throw new IllegalStateException("You cannot add role for this application.");
        }

        ActionStrDTO actionStrDTO = ActionStrDTO.builder()
            .processId(processId)
            .oldStatus(applicationProcessRecord.getProcessStatus())
            .roleId(roleId + 1)
            .fullname(applicationProcessRecord.getFullname())
            .projectName("")
            .projectAmount(0.0)
            .regionName(applicationProcessRecord.getRegionName())
            .currencyName(applicationProcessRecord.getCurrencyName())
            .rateA("")
            .rateB("")
            .paymentMethod(applicationProcessRecord.getPaymentMethod())
            .startDate(LocalDate.now())
            .comments("补充角色")
            .build();
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();
        String jsonString = gson.toJson(actionStrDTO);

        applicationProcessRecord.setActionStr(jsonString);
        applicationProcessRecord.setProcessStatus(87);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "切换为补充角色编辑态", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 切换为升级角色编辑态 upgraderoleediting
     */
    @Override
    @Transactional
    public Boolean upgradeRoleEditing(ActionStrDTO request) {

        Long processId = request.getProcessId();

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot update this application.");
        }

        Integer roleId = applicationProcessRecord.getRoleId();

        if (roleId == 4) {
            throw new IllegalStateException("You cannot upgrade role for this application.");
        }

        ActionStrDTO actionStrDTO = ActionStrDTO.builder()
            .processId(processId)
            .oldStatus(applicationProcessRecord.getProcessStatus())
            .roleId(roleId - 1)
            .fullname(applicationProcessRecord.getFullname())
            .projectName("")
            .projectAmount(0.0)
            .regionName(applicationProcessRecord.getRegionName())
            .currencyName(applicationProcessRecord.getCurrencyName())
            .rateA("")
            .rateB("")
            .paymentMethod(applicationProcessRecord.getPaymentMethod())
            .startDate(LocalDate.now())
            .comments("升级角色")
            .build();

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();
        String jsonString = gson.toJson(actionStrDTO);

        applicationProcessRecord.setActionStr(jsonString);
        applicationProcessRecord.setProcessStatus(97);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "切换为升级角色编辑态", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 取消角色编辑态
     */
    @Override
    @Transactional
    public Boolean cancelRoleEditing(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 97 || r.getProcessStatus() == 87)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot cancel this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ApplicationUpdateRoleDTO updateRoleDTO = gson.fromJson(actionStr, ApplicationUpdateRoleDTO.class);
        
        applicationProcessRecord.setProcessStatus(updateRoleDTO.getOldStatus());
        applicationProcessRecord.setActionStr(null);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "取消角色编辑态", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 保存编辑中的补充角色信息
     */
    @Override
    @Transactional
    public Boolean saveAddRoleEditing(ActionStrDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 87)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        verifyAddRole(applicationProcessRecord.getUser(), request);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();

        ActionStrDTO oldRequest = gson.fromJson(applicationProcessRecord.getActionStr(), ActionStrDTO.class);

        oldRequest.setFullname(request.getFullname());
        oldRequest.setRoleId(request.getRoleId());
        oldRequest.setProjectName(request.getProjectName());
        oldRequest.setProjectAmount(request.getProjectAmount());
        oldRequest.setRegionName(request.getRegionName());
        oldRequest.setCurrencyName(request.getCurrencyName());
        oldRequest.setRateA(request.getRateA());
        oldRequest.setRateB(request.getRateB());
        oldRequest.setPaymentMethod(request.getPaymentMethod());
        oldRequest.setStartDate(request.getStartDate());
        oldRequest.setComments(request.getComments());

        applicationProcessRecord.setActionStr(gson.toJson(request));

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "保存补充角色信息", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 保存编辑中的升级角色信息
     */
    @Override
    @Transactional
    public Boolean saveUpgradeRoleEditing(ActionStrDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 97)
            .orElseThrow(() -> new IllegalStateException("Application not found."));    

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        verifyUpgradeRole(applicationProcessRecord.getUser(), request);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();

        ActionStrDTO oldRequest = gson.fromJson(applicationProcessRecord.getActionStr(), ActionStrDTO.class);

        oldRequest.setFullname(request.getFullname());
        oldRequest.setRoleId(request.getRoleId());
        oldRequest.setProjectName(request.getProjectName());
        oldRequest.setProjectAmount(request.getProjectAmount());
        oldRequest.setRegionName(request.getRegionName());
        oldRequest.setCurrencyName(request.getCurrencyName());
        oldRequest.setRateA(request.getRateA());
        oldRequest.setRateB(request.getRateB());
        oldRequest.setPaymentMethod(request.getPaymentMethod());
        oldRequest.setStartDate(request.getStartDate());
        oldRequest.setComments(request.getComments());

        applicationProcessRecord.setActionStr(gson.toJson(request));

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "保存升级角色信息", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 提交补充角色审核
     */
    @Override
    @Transactional
    public Boolean submitAddRole(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 87)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        applicationProcessRecord.setProcessStatus(88);

        applicationProcessRecord.getApplicationPaymentRecords().stream()
            .filter(p -> p.getStatus().equals(0) || p.getStatus().equals(3))
            .forEach(p -> {
                p.setStatus(2);
            });

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交补充角色审核", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 提交升级角色审核
     */
    @Override
    @Transactional
    public Boolean submitUpgradeRole(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 97)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot save this application.");
        }

        applicationProcessRecord.setProcessStatus(98);

        applicationProcessRecord.getApplicationPaymentRecords().stream()
            .filter(p -> p.getStatus().equals(0) || p.getStatus().equals(3))
            .forEach(p -> {
                p.setStatus(2);
            });

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交升级角色审核", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 财务通过角色补充审核
     */
    @Override
    @Transactional
    public Boolean approveRoleAddByFinance(ApplicationActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
            .filter(r -> r.getProcessStatus() == 88)
            .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Integer operatorRoleId = userContext.getRoleId();

        if (!(operatorRoleId == 1 || operatorRoleId == 8)) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ActionStrDTO updateRoleDTO = gson.fromJson(actionStr, ActionStrDTO.class);

        verifyAddRole(applicationProcessRecord.getUser(), updateRoleDTO);

        applicationProcessRecord.setProcessStatus(updateRoleDTO.getOldStatus());

        addRoleAndPermissions(applicationProcessRecord.getUser(), updateRoleDTO);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务通过补充角色审核", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

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

        BackendUser operator = userContext.getOperator();
        Integer operatorRoleId = userContext.getRoleId();

        if (!(operatorRoleId == 1 || operatorRoleId == 8)) {
            throw new IllegalStateException("You cannot approve this application.");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new InstantAdapter())
            .create();
        String actionStr = applicationProcessRecord.getActionStr();
        ActionStrDTO updateRoleDTO = gson.fromJson(actionStr, ActionStrDTO.class);

        verifyUpgradeRole(applicationProcessRecord.getUser(), updateRoleDTO);

        String paymentMethod = updateRoleDTO.getPaymentMethod();

        applicationProcessRecord.setRoleId(updateRoleDTO.getRoleId());
        applicationProcessRecord.setStartDate(updateRoleDTO.getStartDate());
        applicationProcessRecord.setPaymentMethod(paymentMethod);
        applicationProcessRecord.setRateA(updateRoleDTO.getRateA());
        applicationProcessRecord.setRateB(updateRoleDTO.getRateB());
        applicationProcessRecord.setComments(request.getComments());
        applicationProcessRecord.setProjectName(updateRoleDTO.getProjectName());
        applicationProcessRecord.setProjectAmount(updateRoleDTO.getProjectAmount());
        applicationProcessRecord.setProcessStatus(paymentMethod.equals("全额支付") ? 6 : 5);

        upgradeRoleAndPermissions(applicationProcessRecord.getUser(), updateRoleDTO);

        // 记录提交操作到流程记录表
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务通过升级角色审核", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 添加支付记录
     */
    @Override
    @Transactional
    public Boolean addPaymentRecord(PaymentAddDTO request, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 87 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot add payment record to this application.");
        }

        String comments = request.getComments();

        String currencyName = request.getCurrencyName();

        String currencyCode = regionCurrencyRepository.findCurrencyCodeByCurrencyName(currencyName)
                .orElseThrow(() -> new IllegalStateException("Currency not found."));

        ApplicationPaymentRecord paymentRecord = createPaymentRecord(
            request.getRegionName(), currencyName, currencyCode, request.getProjectName(),
            request.getProjectAmount(), request.getPaymentMethod(), request.getPaymentAmount(), request.getFee(),
            request.getPaymentDate(), operator, comments, request.getPaymentAccountId(), applicationProcessRecord
        );

        applicationProcessRecord.getApplicationPaymentRecords().add(paymentRecord);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "增加支付记录", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        try {
            fileStorageService.uploadFiles("applications/" + applicationProcessRecord.getProcessId() + "/payments/" + paymentRecord.getPaymentId(), files);
        } catch (Exception e) {
            applicationProcessRecord.getApplicationPaymentRecords().remove(paymentRecord);
            throw new IllegalStateException("Failed to upload files for payment record.");
        }

        return true;
    }

    /**
     * 提交支付记录
     */
    @Override
    @Transactional
    public Boolean submitPaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 5)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        
        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot submit payment record for this application.");
        }

        Long paymentId = request.getPaymentId();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && (r.getStatus().equals(0) || r.getStatus().equals(3)))
                .findFirst()
                .map(r -> {
                    r.setStatus(2);
                    r.setComments(request.getComments());
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));
    
        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "提交支付记录", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 审核支付记录
     */
    @Override
    @Transactional
    public Boolean approvePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 2 || r.getProcessStatus() == 5 || r.getProcessStatus() == 88 || r.getProcessStatus() == 98)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Integer roleId = userContext.getRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve payment record for this application.");
        }

        Long paymentId = request.getPaymentId();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && r.getStatus().equals(2))
                .findFirst()
                .map(r -> {
                    r.setFinanceId(operator.getUserId());
                    r.setFinanceName(operator.getUsername());
                    r.setFinanceFullname(operator.getFullname());
                    r.setFinanceApprovalTime(Instant.now());
                    r.setStatus(1);
                    r.setComments(request.getComments());
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务审核通过", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 拒绝支付记录
     */
    @Override
    @Transactional
    public Boolean rejectPaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 5)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Integer roleId = userContext.getRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot reject payment record for this application.");
        }

        Long paymentId = request.getPaymentId();
        String comments = request.getComments();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && r.getStatus().equals(2))
                .findFirst()
                .map(r -> {
                    r.setFinanceId(operator.getUserId());
                    r.setFinanceName(operator.getUsername());
                    r.setFinanceFullname(operator.getFullname());
                    r.setFinanceApprovalTime(Instant.now());
                    r.setStatus(3);
                    r.setComments(comments);
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务审核拒绝", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 撤销审核支付记录
     */
    @Override
    @Transactional
    public Boolean disApprovePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 2 || r.getProcessStatus() == 5 || r.getProcessStatus() == 88 || r.getProcessStatus() == 98)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();
        Integer roleId = userContext.getRoleId();

        if (!(roleId == 1 || roleId == 8)) {
            throw new IllegalStateException("You cannot approve payment record for this application.");
        }

        Long paymentId = request.getPaymentId();
        String comments = request.getComments();

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && r.getStatus().equals(1))
                .findFirst()
                .map(r -> {
                    r.setFinanceId(operator.getUserId());
                    r.setFinanceName(operator.getUsername());
                    r.setFinanceFullname(operator.getFullname());
                    r.setFinanceApprovalTime(Instant.now());
                    r.setStatus(2);
                    r.setComments(comments);
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "财务撤销审核", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 更新支付记录
     */
    @Override
    @Transactional
    public Boolean updatePaymentRecord(PaymentUpdateDTO request, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 87 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot update this payment record.");
        }

        Long paymentId = request.getPaymentId();

        String comments = request.getComments();

        String currencyName = request.getCurrencyName();

        Long paymentAccountId = request.getPaymentAccountId();

        String currencyCode = regionCurrencyRepository.findCurrencyCodeByCurrencyName(currencyName)
                .orElseThrow(() -> new IllegalStateException("Currency not found."));

        PaymentAccount paymentAccount = paymentAccountRepository.findByAccountId(paymentAccountId)
                .orElseThrow(() -> new IllegalStateException("Payment account not found."));

        StringBuilder sb = new StringBuilder();
        sb.append(paymentAccountId).append("|")
            .append(paymentAccount.getAccountName()).append("|")
            .append(paymentAccount.getAccountNumber()).append("|")
            .append(paymentAccount.getAccountType()).append("|")
            .append(paymentAccount.getAccountBank()).append("|")
            .append(paymentAccount.getAccountHolder()).append("|")
            .append(paymentAccount.getAccountCurrency()).append("|")
            .append(paymentAccount.getAccountCurrencyCode()).append("|")
            .append(paymentAccount.getAccountRegion()).append("|")
            .append(paymentAccount.getAccountStatus()).append("|")
            .append(paymentAccount.getAccountComments());

        applicationProcessRecord.getApplicationPaymentRecords().stream()
                .filter(r -> r.getPaymentId().equals(paymentId) && (r.getStatus().equals(0) || r.getStatus().equals(3)))
                .findFirst()
                .map(r -> {
                    r.setRegionName(request.getRegionName());
                    r.setCurrencyName(request.getCurrencyName());
                    r.setCurrencyCode(currencyCode);
                    r.setProjectName(request.getProjectName());
                    r.setProjectAmount(request.getProjectAmount());
                    r.setPaymentMethod(request.getPaymentMethod());
                    r.setFee(request.getFee());
                    r.setPaymentAmount(request.getPaymentAmount());
                    r.setActual(request.getPaymentAmount() - request.getFee());
                    r.setPaymentDate(request.getPaymentDate());
                    r.setPaymentAccountId(request.getPaymentAccountId());
                    r.setPaymentAccountStr(sb.toString());
                    r.setComments(comments);
                    return r;
                })
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        List<String> deleteFiles = request.getDeleteFiles();
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            try {
                fileStorageService.deleteFiles(deleteFiles);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to delete files for payment record.");
            }
        }

        if (files != null && files.length > 0) {
            try {
                fileStorageService.uploadFiles("applications/" + applicationProcessRecord.getProcessId() + "/payments/" + paymentId, files);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to upload files for payment record.");
            }
        }

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "更新支付记录", operator, comments, applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 删除支付记录
     */
    @Override
    @Transactional
    public Boolean deletePaymentRecord(PaymentActionDTO request) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .filter(r -> r.getProcessStatus() == 1 || r.getProcessStatus() == 5 || r.getProcessStatus() == 87 || r.getProcessStatus() == 97)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot delete this payment record.");
        }

        ApplicationPaymentRecord paymentRecord = applicationPaymentRecordRepository.findById(request.getPaymentId())
                .filter(r -> r.getStatus().equals(1))
                .orElseThrow(() -> new IllegalStateException("Payment record not found."));

        applicationProcessRecord.getApplicationPaymentRecords().remove(paymentRecord);

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "删除支付记录", operator, request.getComments(), applicationProcessRecord
        ));

        applicationProcessRecordRepository.save(applicationProcessRecord);

        return true;
    }

    /**
     * 上传合同文件
     */
    @Override
    @Transactional
    public Boolean uploadContractFiles(Long processId, MultipartFile[] files) {

        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        BackendUser operator = userContext.getOperator();

        if (!(userContext.getRoleId() == 1 || isManager(
            operator.getUserId(), applicationProcessRecord.getManager()))) {
            throw new IllegalStateException("You cannot upload contract files for this application.");
        }

        try {
            fileStorageService.uploadFiles("applications/" + processId + "/contracts", files);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload files for application.");
        }

        applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
            "上传合同", operator,  "", applicationProcessRecord
        ));

        return true;
    }

    /**
     * 更新管理人
     */
    @Override
    @Transactional
    public ApplicationResponseDTO changeManager(ApplicationUpdateDTO request) {
            
            ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(request.getProcessId())
                .orElseThrow(() -> new IllegalStateException("Application not found."));

            BackendUser operator = userContext.getOperator();

            if (!(userContext.getRoleId() == 1 || isManager(operator.getUserId(), applicationProcessRecord.getManager()))) {
                throw new IllegalStateException("You cannot update this application.");
            }

            BackendUser manager = backendUserRepository.findById(request.getManagerId())
                .orElseThrow(() -> new IllegalStateException("Manager not found."));

            applicationProcessRecord.setManager(manager);

            BackendUser user = applicationProcessRecord.getUser();

            LocalDate startDate = request.getStartDate();

            if (user != null) {
                List<ManagerRelationship> existingRelationships = user.getManagerRelationships();

                existingRelationships.removeIf(rel -> rel.getStartDate().equals(startDate) && rel.getEndDate() == null);

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

                ManagerRelationship newRelationship = ManagerRelationship.builder()
                    .user(user)
                    .manager(manager)
                    .startDate(startDate)
                    .status(true)
                    .createrId(userContext.getOperatorId())
                    .build();

                user.getManagerRelationships().add(newRelationship);

                backendUserRepository.save(user);
            }

            applicationProcessRecord.getApplicationFlowRecords().add(createFlowRecord(
                "更新管理人", operator, "", applicationProcessRecord
            ));

            applicationProcessRecordRepository.save(applicationProcessRecord);

            return viewApplication(request.getProcessId());
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

        Optional<ApplicationProcessRecord> applicationProcessRecord = applicationProcessRecordRepository.findByFullname(fullname);
        
        if (applicationProcessRecord.isEmpty()) {
            return "success";
        } else {
            return fullname + " 已被 " + applicationProcessRecord.get().getManager().getFullname() + " 创建<br>" +
                "流程单ID: " + applicationProcessRecord.get().getProcessId() + "<br>" +
                "创建时间: " + applicationProcessRecord.get().getCreatedAt();
        }
    }

    /**
     * 检查平台账号是否存在及是否允许创建
     */
    @Override
    @Transactional(readOnly = true)
    public ValidatePlatformAccountDTO validatePlatformAccount(String platformAccount) {

        ValidatePlatformAccountDTO dto = ValidatePlatformAccountDTO.builder()
            .platformAccount(platformAccount)
            .build();

        if (platformAccount == null || platformAccount.trim().isEmpty()) {
            dto.setMessage("Platform account is required.");
            return dto;
        }

        Optional<ApplicationProcessRecord> applicationProcessRecord = applicationProcessRecordRepository.findByUsername(platformAccount);
        
        if (applicationProcessRecord.isEmpty()) {
            Optional<TbUser> tbUser = localTbUserRepository.findByPhone(platformAccount);
            if (tbUser.isEmpty()) {
                dto.setMessage("Platform account not found.");
            } else {
                dto.setPlatformId(tbUser.get().getUserId());
            }
        } else {
            dto.setMessage(platformAccount + " 已被 " + applicationProcessRecord.get().getManager().getFullname() + " 创建<br>" +
                "流程单ID: " + applicationProcessRecord.get().getProcessId() + "<br>" +
                "创建时间: " + applicationProcessRecord.get().getCreatedAt());
        }

        return dto;
    }

    /**
     * 查看申请
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDTO viewApplication(Long processId) {
        ApplicationProcessRecord applicationProcessRecord = applicationProcessRecordRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Application not found."));

        Integer roleId = userContext.getRoleId();

        if (!(roleId == 1 || roleId == 8 || isManager(userContext.getOperatorId(), applicationProcessRecord.getManager())) ) {
            throw new IllegalStateException("You cannot view this application.");
        }

        ApplicationSearchDTO request = ApplicationSearchDTO.builder()
            .processId(processId)
            .build();
        
        Pageable pageable = PageRequest.of(0, 1);

        Page<ApplicationResponseDTO> resultPage = applicationProcessRecordRepository.searchApplications(request, pageable);

        return resultPage.getContent().get(0);
    }

    /**
     * 搜索方法，使用Specification，本人待办申请
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponseDTO> searchTodoApplications(ApplicationSearchDTO request) {

        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("processId").descending());

        // 根据角色获取可见的创建者ID
        List<Integer> allowedProcessStatuses = new ArrayList<>();
        switch (userContext.getRoleId()) {
            case 1:
                allowedProcessStatuses.addAll(Arrays.asList(4, 6));
                break;
            case 2:
                allowedProcessStatuses.addAll(Arrays.asList(4));
                break;
            case 3:
                allowedProcessStatuses.addAll(Arrays.asList(1, 3, 5, 87, 97));
                break;
            case 8:
                allowedProcessStatuses.addAll(Arrays.asList(2, 5, 88, 98));
                request.setIsFinanceTodo(true);
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
        request.setProcessStatuses(processStatuses);

        Page<ApplicationResponseDTO> resultPage = applicationProcessRecordRepository.searchApplications(request, pageable);

        return resultPage;
    }

    /**
     * 搜索方法，使用Specification
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponseDTO> searchApplications(ApplicationSearchDTO request) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("processId").ascending());

        Page<ApplicationResponseDTO> resultPage = applicationProcessRecordRepository.searchApplications(request, pageable);

        return resultPage;
    
    }

    /**
     * 创建支付记录
     */
    private ApplicationPaymentRecord createPaymentRecord(
            String regionName, String currencyName, String currencyCode, String projectName, Double projectAmout,
            String paymentMethod, Double paymentAmount, Double fee, LocalDate paymentDate, BackendUser creater,
            String comments, Long paymentAccountId, ApplicationProcessRecord applicationProcessRecord) {

        PaymentAccount paymentAccount = paymentAccountRepository.findByAccountId(paymentAccountId)
            .orElseThrow(() -> new IllegalStateException("Payment account not found."));

        StringBuilder sb = new StringBuilder();
        sb.append(paymentAccountId).append("|")
            .append(paymentAccount.getAccountName()).append("|")
            .append(paymentAccount.getAccountNumber()).append("|")
            .append(paymentAccount.getAccountType()).append("|")
            .append(paymentAccount.getAccountBank()).append("|")
            .append(paymentAccount.getAccountHolder()).append("|")
            .append(paymentAccount.getAccountCurrency()).append("|")
            .append(paymentAccount.getAccountCurrencyCode()).append("|")
            .append(paymentAccount.getAccountRegion()).append("|")
            .append(paymentAccount.getAccountStatus()).append("|")
            .append(paymentAccount.getAccountComments());

        return ApplicationPaymentRecord.builder()
            .regionName(regionName)
            .currencyName(currencyName)
            .currencyCode(currencyCode)
            .projectName(projectName)
            .projectAmount(projectAmout)
            .projectCurrencyName(applicationProcessRecord.getCurrencyName())
            .projectCurrencyCode(applicationProcessRecord.getCurrencyCode())
            .paymentMethod(paymentMethod)
            .paymentAmount(paymentAmount)
            .paymentAccountId(paymentAccountId)
            .paymentAccountStr(sb.toString())
            .fee(fee)
            .actual(paymentAmount - fee)
            .paymentDate(paymentDate)
            .createrId(creater.getUserId())
            .createrName(creater.getUsername())
            .createrFullname(creater.getFullname())
            .comments(comments)
            .applicationProcessRecord(applicationProcessRecord)
            .build();
    }

    /**
     * 创建流程记录
     */
    private ApplicationFlowRecord createFlowRecord(
            String action, BackendUser creater, String comments, ApplicationProcessRecord applicationProcessRecord) {
        
        return ApplicationFlowRecord.builder()
            .action(action)
            .createrId(creater.getUserId())
            .createrName(creater.getUsername())
            .createrFullname(creater.getFullname())
            .comments(comments)
            .applicationProcessRecord(applicationProcessRecord)
            .build();
    }

    /**
     * 验证角色是否允许补充
     */
    private void verifyAddRole(BackendUser user, ActionStrDTO request) {

        List<RolePermissionRelationship> rolePermissionRelationships = user.getRolePermissionRelationships();

        if (rolePermissionRelationships.isEmpty()) {
            throw new IllegalStateException("User has no role.");
        }

        Integer newRoleId = request.getRoleId();
        LocalDate newStartDate = request.getStartDate();

        for (RolePermissionRelationship rolePermissionRelationship : rolePermissionRelationships) {
            Integer roleId = rolePermissionRelationship.getRoleId();
            LocalDate startDate = rolePermissionRelationship.getStartDate();
            LocalDate endDate = rolePermissionRelationship.getEndDate();

            if (newRoleId == roleId) {
                throw new IllegalStateException("已存在相同角色");
            } else if (endDate == null && newRoleId < roleId) {
                throw new IllegalStateException("补充角色必须低于当前角色");
            } else if (newRoleId < roleId && !(newStartDate.isAfter(startDate))) {
                throw new IllegalStateException("补充角色生效日期必须晚于" + startDate.toString());
            } else if (newRoleId > roleId && !(newStartDate.isBefore(startDate))) {
                throw new IllegalStateException("补充角色生效日期必须早于" + startDate.toString());
            }
        }
    }

    /**
     * 验证角色是否允许升级
     */
    private void verifyUpgradeRole(BackendUser user, ActionStrDTO request) {

        List<RolePermissionRelationship> rolePermissionRelationships = user.getRolePermissionRelationships();

        if (rolePermissionRelationships.isEmpty()) {
            throw new IllegalStateException("User has no role.");
        }

        RolePermissionRelationship currentRole = rolePermissionRelationships.stream()
            .filter(rr -> rr.getEndDate() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Current role not found."));

        if (!(request.getRoleId() < currentRole.getRoleId())) {
            throw new IllegalStateException("新角色必须高于当前角色");
        }

        if (!(request.getStartDate().isAfter(currentRole.getStartDate()))) {
            throw new IllegalStateException("新角色生效日期必须晚于" + currentRole.getStartDate().toString());
        }
    }

    /**
     * 补充角色 & 权限
     */
    private void addRoleAndPermissions(BackendUser user, ActionStrDTO updateRoleDTO) {

        Integer addRoleId = updateRoleDTO.getRoleId();

        List<RolePermission> rolePermissions = rolePermissionRepository.findByIdRoleId(addRoleId);

        if (rolePermissions.size() < 3) {
            throw new IllegalStateException("Not all RolePermissions found.");
        }

        List<RolePermissionRelationship> rolePermissionRelationships = user.getRolePermissionRelationships();
        rolePermissionRelationships.sort(Comparator.comparing(RolePermissionRelationship::getStartDate));

        LocalDate addStartDate = updateRoleDTO.getStartDate();
        LocalDate addEndDate = null;

        for (RolePermissionRelationship rolePermissionRelationship : rolePermissionRelationships) {
            Integer roleId = rolePermissionRelationship.getRoleId();
            LocalDate startDate = rolePermissionRelationship.getStartDate();
            LocalDate endDate = rolePermissionRelationship.getEndDate();
            if (addRoleId > roleId && addStartDate.isBefore(startDate)) {
                addEndDate = startDate.minusDays(1);
                break;
            } else if (addRoleId < roleId && addStartDate.isAfter(startDate) && addStartDate.isBefore(endDate)) {
                addEndDate = endDate;
                rolePermissionRelationship.setEndDate(startDate.minusDays(1));
            }
        }

        Long operatorId = userContext.getOperatorId();

        for (RolePermission rp : rolePermissions) {
            Integer permissionId = rp.getId().getPermissionId();
            double rate1 = rp.getRate1();
            double rate2 = rp.getRate2();
            Boolean status = rp.getIsEnabled();

            if (permissionId == 1) {
                if (updateRoleDTO.getRateA() != null && !updateRoleDTO.getRateA().trim().isEmpty()) {
                    double[] rates = parseRates(updateRoleDTO.getRateA());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            } else if (permissionId == 2) {
                if (updateRoleDTO.getRateB() != null && !updateRoleDTO.getRateB().trim().isEmpty()) {
                    double[] rates = parseRates(updateRoleDTO.getRateB());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            }

            RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                .user(user)
                .roleId(addRoleId)
                .roleName(rp.getRoleName())
                .permissionId(permissionId)
                .permissionName(rp.getPermissionName())
                .rate1(rate1)
                .rate2(rate2)
                .startDate(addStartDate)
                .endDate(addEndDate)
                .status(status)
                .createrId(operatorId)
                .build();

            user.getRolePermissionRelationships().add(rolePermissionRelationship);
        }
    }

    /**
     * 升级角色 & 权限
     */
    private void upgradeRoleAndPermissions(BackendUser user, ActionStrDTO updateRoleDTO) {

        Integer newRoleId = updateRoleDTO.getRoleId();
        LocalDate newStarDate = updateRoleDTO.getStartDate();

        List<RolePermission> rolePermissions = rolePermissionRepository.findByIdRoleId(newRoleId);

        if (rolePermissions.size() < 3) {
            throw new IllegalStateException("Not all RolePermissions found.");
        }

        user.getRolePermissionRelationships().stream()
            .filter(rr -> rr.getEndDate() == null)
            .forEach(rr -> rr.setEndDate(newStarDate.minusDays(1)));

            LocalDate startDate = updateRoleDTO.getStartDate();

        for (RolePermission rp : rolePermissions) {
            Integer permissionId = rp.getId().getPermissionId();
            double rate1 = rp.getRate1();
            double rate2 = rp.getRate2();
            Boolean status = rp.getIsEnabled();

            if (permissionId == 1) {
                if (updateRoleDTO.getRateA() != null && !updateRoleDTO.getRateA().trim().isEmpty()) {
                    double[] rates = parseRates(updateRoleDTO.getRateA());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            } else if (permissionId == 2) {
                if (updateRoleDTO.getRateB() != null && !updateRoleDTO.getRateB().trim().isEmpty()) {
                    double[] rates = parseRates(updateRoleDTO.getRateB());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            }

            RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                .user(user)
                .roleId(newRoleId)
                .roleName(rp.getRoleName())
                .permissionId(permissionId)
                .permissionName(rp.getPermissionName())
                .rate1(rate1)
                .rate2(rate2)
                .startDate(startDate)
                .status(status)
                .createrId(userContext.getOperatorId())
                .build();

            user.getRolePermissionRelationships().add(rolePermissionRelationship);
        }

        backendUserRepository.save(user);
    }

    /**
     * 新增角色 & 权限
     */
    private void createRoleAndPermissions(BackendUser user, ApplicationProcessRecord applicationProcessRecord) {

        Integer roleId = applicationProcessRecord.getRoleId();

        List<RolePermission> rolePermissions = rolePermissionRepository.findByIdRoleId(roleId);

        if (rolePermissions.size() < 3) {
            throw new IllegalStateException("Not all RolePermissions found.");
        }

        for (RolePermission rp : rolePermissions) {
            Integer permissionId = rp.getId().getPermissionId();
            double rate1 = rp.getRate1();
            double rate2 = rp.getRate2();
            Boolean status = rp.getIsEnabled();

            if (permissionId == 1) {
                if (applicationProcessRecord.getRateA() != null && !applicationProcessRecord.getRateA().trim().isEmpty()) {
                    double[] rates = parseRates(applicationProcessRecord.getRateA());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            } else if (permissionId == 2) {
                if (applicationProcessRecord.getRateB() != null && !applicationProcessRecord.getRateB().trim().isEmpty()) {
                    double[] rates = parseRates(applicationProcessRecord.getRateB());
                    if (rates.length > 0) {
                        rate1 = rates.length > 1 ? rates[1] : 0.0;
                        rate2 = rates.length > 2 ? rates[2] : 0.0;
                        status = rate2 > 0.0;
                    }
                }
            }

            RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                .user(user)
                .roleId(roleId)
                .roleName(rp.getRoleName())
                .permissionId(permissionId)
                .permissionName(rp.getPermissionName())
                .rate1(rate1)
                .rate2(rate2)
                .startDate(applicationProcessRecord.getStartDate())
                .status(status)
                .createrId(userContext.getOperatorId())
                .build();

            user.getRolePermissionRelationships().add(rolePermissionRelationship);

            backendUserRepository.save(user);
        }
    }

    /**
     * 解析 rate 字符串为 double 数组
     */
    private double[] parseRates(String rate) {
        return Arrays.stream(rate.split("\\*"))
                 .mapToDouble(this::parseDoubleSafe)
                 .toArray();
    }

    /**
     * 安全地将字符串解析为 double
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
     * 回溯设置被邀请人的邀请人关系
     */
    private void trackingInviter(BackendUser savedUser) {

        String inviterName = savedUser.getUsername();

        applicationProcessRecordRepository.findAllByInviterIsNull().stream()
            .filter(r -> r.getInviterName().equals(inviterName))
            .forEach(r -> {
                r.setInviter(savedUser);
                applicationProcessRecordRepository.save(r);
                BackendUser user = r.getUser();
                if (user != null) {
                    InviterRelationship inviterRelationship = InviterRelationship.builder()
                        .user(user)
                        .inviter(savedUser)
                        .startDate(r.getStartDate())
                        .status(true)
                        .createrId(userContext.getOperator().getUserId())
                        .build();
                    user.getInviterRelationships().add(inviterRelationship);
                    backendUserRepository.save(user);
                }
            });
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
                .map(ManagerRelationship::getUser)
                .findFirst()
                .orElse(null);
        }
        return false;
    }
}
