package com.rsmanager.controller;

import com.rsmanager.dto.api.*;
import com.rsmanager.dto.application.*;
import com.rsmanager.service.ApplicationService;
import com.rsmanager.service.FileStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;

    /**
     * 创建流程单
     * 前端需要传递 ApplicationCreateRequestDTO 和多个文件
     */
    @PostMapping("/create")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> createApplication(
        @ModelAttribute @Valid ApplicationCreateRequestDTO applicationCreateRequestDTO,
        @RequestPart("files") MultipartFile[] files) {

        Long processId = applicationService.createApplication(applicationCreateRequestDTO, files);

        return ResponseEntity.ok(ApiResponseDTO.<Map<String, Long>>builder()
                .success(true)
                .message("User ID retrieved successfully")
                .data(Collections.singletonMap("processId", processId))
                .build());
    }

    /**
     * 删除流程单
     */
    @PostMapping("/delete")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> deleteApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.deleteApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application deleted." : "Application not deleted.")
            .build());
    }

    /**
     * 激活流程单
     */
    @PostMapping("/activate")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> activateApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.activateApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application activated." : "Application not activated.")
            .build());
    }

    /**
     * 取消流程单（未提交）
     */
    @PostMapping("/cancel")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> cancelApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.cancelApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application canceled." : "Application not canceled.")
            .build());
    }

    /**
     * 提交审核
     */
    @PostMapping("/submit")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.submitApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application submitted." : "Application not submitted.")
            .build());
    }

    /**
     * 更新流程单
     */
    @PostMapping("/update")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> updateApplication(
        @Valid @RequestBody ApplicationProcessUpdateDTO request) {

        Boolean result = applicationService.updateApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application updated." : "Application not updated.")
            .build());
    }

    /**
     * 撤回流程单（审核中）
     */
    @PostMapping("/withdraw")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<?>> withdrawApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.withdrawApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application withdrawn." : "Application not withdrawn.")
            .build());
    }

    /**
     * 财务审批申请
     */
    @PostMapping("/approvefinance")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> approveFinanceApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approveFinanceApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application approved." : "Application not approved.")
            .build());
    }

    /**
     * 申请链接
     */
    @PostMapping("/submitforlink")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitforlink(
        @Valid @RequestBody ApplicationSubmitForLinkDTO request) {

        Boolean result = applicationService.submitForLink(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application submitted." : "Application not submitted.")
            .build());
    }

    /**
     * 链接审批申请
     */
    @PostMapping("/approvelink")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2)")
    public ResponseEntity<ApiResponseDTO<?>> approvelink(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approvelink(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application approved." : "Application not approved.")
            .build());
    }

    /**
     * 完成申请
     */
    @PostMapping("/finished")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2)")
    public ResponseEntity<ApiResponseDTO<?>> finished(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.finishedApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application finished." : "Application not finished.")
            .build());
    }

    /**
     * 归档申请
     */
    @PostMapping("/archive")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2)")
    public ResponseEntity<ApiResponseDTO<?>> archiveApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.archiveApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application archived." : "Application not archived.")
            .build());
    }

    /**
     * 退款
     */
    @PostMapping("/refund")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> refundApplication(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.refundApplication(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Application refunded." : "Application not refunded.")
            .build());
    }

    /**
     * 添加支付记录
     */
    @PostMapping("/payment/add")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> addPaymentRecord(
        @ModelAttribute @Valid PaymentAddDTO request,
        @RequestPart("files") MultipartFile[] files) {

        Boolean result = applicationService.addPaymentRecord(request, files);

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Payment record added." : "Payment record not added.")
                .build());
    }

    /**
     * 提交支付记录
     */
    @PostMapping("/payment/submit")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitPaymentRecord(
        @Valid @RequestBody PaymentActionDTO request) {

        Boolean result = applicationService.submitPaymentRecord(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record submitted." : "Payment record not submitted.")
            .build());
    }

    /**
     * 审批支付记录
     */
    @PostMapping("/payment/approve")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> approvePaymentRecord(
        @Valid @RequestBody PaymentActionDTO request) {

        Boolean result = applicationService.approvePaymentRecord(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record approved." : "Payment record not approved.")
            .build());
    }

    /**
     * 拒绝支付记录
     */
    @PostMapping("/payment/reject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> rejectPaymentRecord(
        @Valid @RequestBody PaymentActionDTO request) {

        Boolean result = applicationService.rejectPaymentRecord(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record rejected." : "Payment record not rejected.")
            .build());
    }

    /**
     * 撤销审核支付记录
     */
    @PostMapping("/payment/disapprove")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> disApprovePaymentRecord(
        @Valid @RequestBody PaymentActionDTO request) {

        Boolean result = applicationService.disApprovePaymentRecord(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record disapproved." : "Payment record not disapproved.")
            .build());
    }

    /**
     * 编辑支付记录
     */
    @PostMapping("/payment/update")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> updatePaymentRecord(
        @ModelAttribute @Valid PaymentUpdateDTO request,
        @RequestPart(value = "files", required = false) MultipartFile[] files) {

        Boolean result = applicationService.updatePaymentRecord(request, files);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record updated." : "Payment record not updated.")
            .build());
    }

    /**
     * 删除支付记录
     */
    @PostMapping("/payment/delete")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> deletePaymentRecord(
        @Valid @RequestBody PaymentActionDTO request) {

        Boolean result = applicationService.deletePaymentRecord(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Payment record deleted." : "Payment record not deleted.")
            .build());
    }

    /**
     * 切换为补充角色编辑态
     */
    @PostMapping("/addroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> addRoleEditing(
        @Valid @RequestBody ActionStrDTO request) {

        Boolean result = applicationService.addRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing updated." : "Role editing not updated.")
            .build());
    }

    /**
     * 切换为升级角色编辑态
     */
    @PostMapping("/upgraderoleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> upgradeRoleEditing(
        @Valid @RequestBody ActionStrDTO request) {

        Boolean result = applicationService.upgradeRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing updated." : "Role editing not updated.")
            .build());
    }

    /**
     * 取消角色编辑态
     */
    @PostMapping("/cancelroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> cancelRoleEditing(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.cancelRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing canceled." : "Role editing not canceled.")
            .build());
    }

    /**
     * 保存编辑中的补充角色信息
     */
    @PostMapping("/saveaddroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> saveAddRoleEditing(
        @Valid @RequestBody ActionStrDTO request) {

        Boolean result = applicationService.saveAddRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing saved." : "Role editing not saved.")
            .build());
    }

    /**
     * 保存编辑中的升级角色信息
     */
    @PostMapping("/saveupgraderoleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> saveUpgradeRoleEditing(
        @Valid @RequestBody ActionStrDTO request) {

        Boolean result = applicationService.saveUpgradeRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing saved." : "Role editing not saved.")
            .build());
    }

    /**
     * 提交补充角色审核
     */
    @PostMapping("/submitaddrole")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitAddRole(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.submitAddRole(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade submitted." : "Role upgrade not submitted.")
            .build());
    }

    /**
     * 提交升级角色审核
     */
    @PostMapping("/submitupgraderole")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitUpgradeRole(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.submitUpgradeRole(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade submitted." : "Role upgrade not submitted.")
            .build());
    }

    /**
     * 财务通过角色升级审核
     */
    @PostMapping("/approveroleaddbyfinance")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> approveRoleAddByFinance(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approveRoleAddByFinance(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade approved." : "Role upgrade not approved.")
            .build());
    }

    /**
     * 财务通过角色升级审核
     */
    @PostMapping("/approveroleupgradebyfinance")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> approveRoleUpgradeByFinance(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approveRoleUpgradeByFinance(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade approved." : "Role upgrade not approved.")
            .build());
    }

    /**
     * 上传合同文件
     */
    @PostMapping("/uploadcontractfiles")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<Void>> uploadContractFiles(
        @RequestParam("processId") Long processId,
        @RequestPart("files") MultipartFile[] files) {

        Boolean result = applicationService.uploadContractFiles(processId, files);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(result)
                .message(result ? "Contract file uploaded." : "Contract file not uploaded.")
                .build());
    }

    /**
     * 查看单个流程单
     */
    @PostMapping("/info")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<ApplicationResponseDTO>> viewApplication(
        @Valid @RequestBody Map<String, Long> request) {

            ApplicationResponseDTO application = applicationService.viewApplication(request.get("processId"));

        return ResponseEntity.ok(ApiResponseDTO.<ApplicationResponseDTO>builder()
                .success(application != null)
                .message(application != null ? "Application found." : "Application not found.")
                .data(application)
                .build());
    }

    /**
     * 通用的待办搜索方法，支持分页，返回的是Page<ApplicationProcessRecordDTO>
     */
    @PostMapping("/searchtodo")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<ApplicationResponseDTO>>> searchTodoApplications(
            @Valid @RequestBody ApplicationSearchDTO request) {

        Page<ApplicationResponseDTO> response = applicationService.searchTodoApplications(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<ApplicationResponseDTO>>builder()
                .success(true)
                .message("Applications found.")
                .data(response)
                .build());
    }

    /**
     * 通用的搜索方法，支持分页，返回的是Page<ApplicationProcessRecordDTO>
     */
    @PostMapping("/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<ApplicationResponseDTO>>> searchApplications(
            @Valid @RequestBody ApplicationSearchDTO request) {

        Page<ApplicationResponseDTO> response = applicationService.searchApplications(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<ApplicationResponseDTO>>builder()
                .success(true)
                .message("Applications found.")
                .data(response)
                .build());
    }

    /**
     * 修改管理人
     */
    @PostMapping("/changemanager")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2)")
    public ResponseEntity<ApiResponseDTO<ApplicationResponseDTO>> changeManager(
        @Valid @RequestBody ApplicationUpdateDTO request) {

        ApplicationResponseDTO response = applicationService.changeManager(request);

        return ResponseEntity.ok(ApiResponseDTO.<ApplicationResponseDTO>builder()
                .success(response != null)
                .message(response != null ? "Manager changed." : "Manager not changed.")
                .data(response)
                .build());
    }

    /**
     * 检查用户姓名是否存在
     */
    @PostMapping("/checkfullname")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> checkFullname(
        @Valid @RequestBody Map<String, String> request) {
        String fullname = request.get("fullname");
        String message = applicationService.checkFullname(fullname);
        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(message == "success")
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 检查平台账号是否存在及是否允许创建
     */
    @PostMapping("/validateplatformaccount")
    public ResponseEntity<ApiResponseDTO<?>> validatePlatformAccount(@Valid @RequestBody Map<String, String> request) {

        ValidatePlatformAccountDTO result = applicationService.validatePlatformAccount(request.get("platformAccount"));

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(true)
                .message(result.getMessage())
                .data(result)
                .build());
    }

    /**
     * 下载文件
     */
    @PostMapping("/file")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<Resource> getFile(
        @Valid @RequestBody GetFileDTO request) throws IOException {
        Resource file = fileStorageService.getFile(request.getTargetDir(), request.getFileName());
        return ResponseEntity.ok(file);
    }
    
    /**
     * 获取文件摘要信息
     */
    @PostMapping("/files/allfilessummary")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<AllFilesSummaryDTO>> getAllFilesSummary(
        @Valid @RequestBody AllFilesSummaryRequstDTO request) throws IOException {
        
        AllFilesSummaryDTO fileSummary = fileStorageService.getAllFilesSummary(request);
        
        return ResponseEntity.ok(ApiResponseDTO.<AllFilesSummaryDTO>builder()
                .success(true)
                .message("File summary retrieved successfully.")
                .data(fileSummary)
                .build());
    }

    /**
     * 获取合同文件摘要信息
     */
    @PostMapping("/files/contractSummary")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<FileSummaryDTO>> getContractSummary(
        @Valid @RequestBody GetFileDTO request) throws IOException {
        
        String targetDir = "applications/" + request.getProcessId().toString() + "/contract";
        FileSummaryDTO fileSummary = fileStorageService.getFilesSummary(targetDir);
        
        return ResponseEntity.ok(ApiResponseDTO.<FileSummaryDTO>builder()
                .success(true)
                .message("File summary retrieved successfully.")
                .data(fileSummary)
                .build());
    }
}
