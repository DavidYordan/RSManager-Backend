package com.rsmanager.controller;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.finance.CashOutDTO;
import com.rsmanager.dto.finance.CashOutRejectDTO;
import com.rsmanager.dto.finance.FinanceSearchDTO;
import com.rsmanager.dto.finance.PaymentAccountDTO;
import com.rsmanager.service.DataSyncService;
import com.rsmanager.service.FinanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final DataSyncService dataSyncService;

    // 同步提款单据
    @GetMapping("/sync")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> sync() {

        Boolean result = dataSyncService.syncCashOut();

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Cash out data synced successfully." : "Failed to sync cash out data.")
                .build());
    }

    // 查询提款记录
    @PostMapping("/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<Page<CashOutDTO>>> searchCashOut(
            @Valid @RequestBody FinanceSearchDTO request) {

        return ResponseEntity.ok(ApiResponseDTO.<Page<CashOutDTO>>builder()
                .success(true)
                .message("All global parameters retrieved successfully.")
                .data(financeService.searchCashOut(request))
                .build());
    }

    // 查询待办记录
    @PostMapping("/searchtodo")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<Page<CashOutDTO>>> searchCashOutTodo(
            @Valid @RequestBody FinanceSearchDTO request) {

        request.setState(0);

        return ResponseEntity.ok(ApiResponseDTO.<Page<CashOutDTO>>builder()
                .success(true)
                .message("All global parameters retrieved successfully.")
                .data(financeService.searchCashOut(request))
                .build());
    }

    // 完成提款
    @PostMapping("/complete")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> completeCashOut(@RequestBody Map<String, Long> request) {

        String result = financeService.completeCashOut(request.get("id"));

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success("success".equals(result) ? true : false)
                .message("success".equals(result) ? "Cash out completed successfully." : result)
                .build());
    }

    // 拒绝提款
    @PostMapping("/reject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> rejectCashOut(@RequestBody CashOutRejectDTO request) {

        String result = financeService.rejectCashOut(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success("success".equals(result) ? true : false)
                .message("success".equals(result) ? "Cash out rejected successfully." : result)
                .build());
    }

    // 导出xlsx文件
    @PostMapping("/export")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<?> exportCashOut(@Valid @RequestBody FinanceSearchDTO request) {
        try {
            ByteArrayInputStream in = financeService.exportCashOutToExcel(request);

            // 设置响应头
            String fileName = "CashOut.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDTO.builder()
                    .success(false)
                    .message("导出 Excel 文件失败: " + e.getMessage())
                    .build());
        }
    }

    // 更新收款账户
    @PostMapping("/paymentaccount/update")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> updatePaymentAccount(
        @RequestBody PaymentAccountDTO request) {
        
        Boolean result = financeService.updatePaymentAccount(request);
        
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Payment account updated successfully." : "Failed to update payment account.")
                .build());
    }

    // 查询收款账户
    @PostMapping("/paymentaccount/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<PaymentAccountDTO>>> searchPaymentAccount(
        @RequestBody PaymentAccountDTO request) {
        
        Page<PaymentAccountDTO> result = financeService.getPaymentAccount(request);
        
        return ResponseEntity.ok(ApiResponseDTO.<Page<PaymentAccountDTO>>builder()
                .success(true)
                .message("Payment account retrieved successfully.")
                .data(result)
                .build());
    }

    // 新增收款账户
    @PostMapping("/paymentaccount/add")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> addPaymentAccount(
        @RequestBody PaymentAccountDTO request) {

        Boolean result = financeService.addPaymentAccount(request);

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Payment account added successfully." : "Failed to add payment account.")
                .build());
    }

    // 删除收款账户
    @PostMapping("/paymentaccount/delete")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 8)")
    public ResponseEntity<ApiResponseDTO<?>> deletePaymentAccount(
        @RequestBody Map<String, Object> request) {
        
        Boolean result = financeService.deletePaymentAccount(Long.parseLong(request.get("accountId").toString()));
        
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Payment account deleted successfully." : "Failed to delete payment account.")
                .build());
    }
}
