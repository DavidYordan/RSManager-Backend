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
import java.util.Optional;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;
    // private final SystemService globalParamsService;

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

    // 上传合同
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
     * 通用的搜索方法，支持分页，返回的是Page<ApplicationProcessRecordDTO>
     */
    @PostMapping("/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<ViewApplicationResponseDTO>>> searchApplications(
            @Valid @RequestBody ApplicationSearchDTO request) {

        Page<ViewApplicationResponseDTO> response = applicationService.searchApplications(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<ViewApplicationResponseDTO>>builder()
                .success(true)
                .message("Applications found.")
                .data(response)
                .build());
    }

    /**
     * 通用的待办搜索方法，支持分页，返回的是Page<ApplicationProcessRecordDTO>
     */
    @PostMapping("/searchtodo")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<ViewApplicationResponseDTO>>> searchTodoApplications(
            @Valid @RequestBody ApplicationSearchDTO request) {

        Page<ViewApplicationResponseDTO> response = applicationService.searchTodoApplications(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<ViewApplicationResponseDTO>>builder()
                .success(true)
                .message("Applications found.")
                .data(response)
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
     * 切换为升级角色编辑态
     */
    @PostMapping("/updateroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> updateRoleEditing(
        @Valid @RequestBody ApplicationUpdateRoleDTO request) {

        Boolean result = applicationService.updateRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing updated." : "Role editing not updated.")
            .build());
    }

    /**
     * 取消角色编辑态
     */
    @PostMapping("/cancelupdateroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> cancelUpdateRoleEditing(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.cancelUpdateRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing canceled." : "Role editing not canceled.")
            .build());
    }

    /**
     * 保存编辑中的升级角色信息
     */
    @PostMapping("/saveroleediting")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> saveRoleEditing(
        @Valid @RequestBody ApplicationUpdateRoleDTO request) {

        Boolean result = applicationService.saveRoleEditing(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role editing saved." : "Role editing not saved.")
            .build());
    }

    /**
     * 提交升级角色审核
     */
    @PostMapping("/submitroleupgrade")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> submitRoleUpgrade(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.submitRoleUpgrade(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade submitted." : "Role upgrade not submitted.")
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
     * 主管通过角色升级审核
     */
    @PostMapping("/approveroleupgradebymanager")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> approveRoleUpgradeByManager(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approveRoleUpgradeByManager(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role upgrade approved." : "Role upgrade not approved.")
            .build());
    }

    /**
     * 补充历史角色信息
     */
    @PostMapping("/updaterolehistory")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> updateRoleHistory(
        @Valid @RequestBody ApplicationUpdateRoleDTO request) {

        Boolean result = applicationService.updateRoleHistory(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role history updated." : "Role history not updated.")
            .build());
    }

    /**
     * 审核历史角色信息
     */
    @PostMapping("/approverolehistory")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<?>> approveRoleHistory(
        @Valid @RequestBody ApplicationActionDTO request) {

        Boolean result = applicationService.approveRoleHistory(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
            .success(result)
            .message(result ? "Role history approved." : "Role history not approved.")
            .build());
    }

    /**
     * 审核同意流程单
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
     * 查看单个流程单
     */
    @PostMapping("/info")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<ApplicationInfoResponseDTO>> viewApplication(
        @Valid @RequestBody Map<String, Long> request) {

        Optional<ApplicationInfoResponseDTO> application = applicationService.viewApplication(request.get("processId"));

        return ResponseEntity.ok(ApiResponseDTO.<ApplicationInfoResponseDTO>builder()
                .success(application.isPresent())
                .message(application.isPresent() ? "Application found." : "Application not found.")
                .data(application.orElse(null))
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
     * 需要权限: ROLE_SUPER_ADMIN or APPLICATIONS_PAYMENT_DELETE
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

    // checkfullname
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
}
