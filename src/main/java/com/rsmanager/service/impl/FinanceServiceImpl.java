package com.rsmanager.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.rsmanager.dto.finance.*;
import com.rsmanager.model.PaymentAccount;
import com.rsmanager.repository.local.LocalCashOutRepository;
import com.rsmanager.repository.local.PaymentAccountRepository;
import com.rsmanager.service.DataSyncService;
import com.rsmanager.service.FinanceService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceServiceImpl.class);

    private final DataSyncService dataSyncService;
    private final LocalCashOutRepository localCashOutRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final RestTemplate restTemplate;

    @Value("${cashout.complete.url}")
    private String completeUrl;

    @Value("${cashout.reject.url}")
    private String rejectUrl;

    @Value("${platform.admin-access-token}")
    private String adminAccessToken;

    @Override
    public Boolean syncCashOut() {
        return dataSyncService.syncCashOut();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CashOutDTO> searchCashOut(FinanceSearchDTO request) {
    // 构建动态查询条件
        StringBuilder whereClause = new StringBuilder();

        List<Object> params = new ArrayList<>();

        if (request.getUserId() != null) {
            whereClause.append(" AND c.user_id = ? ");
            params.add(request.getUserId());
        }
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            whereClause.append(" AND u.username LIKE ? ");
            params.add("%" + request.getUsername() + "%");
        }
        if (request.getFullname() != null && !request.getFullname().isEmpty()) {
            whereClause.append(" AND u.fullname LIKE ? ");
            params.add("%" + request.getFullname() + "%");
        }
        if (request.getInviterId() != null) {
            whereClause.append(" AND u.inviter_id = ? ");
            params.add(request.getInviterId());
        }
        if (request.getInviterName() != null && !request.getInviterName().isEmpty()) {
            whereClause.append(" AND u.inviter_name LIKE ? ");
            params.add("%" + request.getInviterName() + "%");
        }
        if (request.getInviterFullname() != null && !request.getInviterFullname().isEmpty()) {
            whereClause.append(" AND u.inviter_fullname LIKE ? ");
            params.add("%" + request.getInviterFullname() + "%");
        }
        if (request.getManagerId() != null) {
            whereClause.append(" AND u.manager_id = ? ");
            params.add(request.getManagerId());
        }
        if (request.getManagerName() != null && !request.getManagerName().isEmpty()) {
            whereClause.append(" AND u.manager_name LIKE ? ");
            params.add("%" + request.getManagerName() + "%");
        }
        if (request.getManagerFullname() != null && !request.getManagerFullname().isEmpty()) {
            whereClause.append(" AND u.manager_fullname LIKE ? ");
            params.add("%" + request.getManagerFullname() + "%");
        }
        if (request.getPlatformId() != null) {
            whereClause.append(" AND u.platform_id = ? ");
            params.add(request.getPlatformId());
        }
        if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            whereClause.append(" AND c.order_number LIKE ? ");
            params.add("%" + request.getOrderNumber() + "%");
        }
        if (request.getState() != null) {
            whereClause.append(" AND c.state = ? ");
            params.add(request.getState());
        }
        if (request.getRecipient() != null && !request.getRecipient().isEmpty()) {
            whereClause.append(" AND c.recipient LIKE ? ");
            params.add("%" + request.getRecipient() + "%");
        }
        if (request.getBankNumber() != null && !request.getBankNumber().isEmpty()) {
            whereClause.append(" AND c.bank_number LIKE ? ");
            params.add("%" + request.getBankNumber() + "%");
        }
        if (request.getBankName() != null && !request.getBankName().isEmpty()) {
            whereClause.append(" AND c.bank_name LIKE ? ");
            params.add("%" + request.getBankName() + "%");
        }
        if (request.getCreatedAfter() != null) {
            whereClause.append(" AND c.create_at >= ? ");
            params.add(request.getCreatedAfter().toString());
        }
        if (request.getCreatedBefore() != null) {
            whereClause.append(" AND c.create_at <= ? ");
            params.add(request.getCreatedBefore().toString());
        }
        if (request.getOutAfter() != null) {
            whereClause.append(" AND c.out_at >= ? ");
            params.add(request.getOutAfter().toString());
        }
        if (request.getOutBefore() != null) {
            whereClause.append(" AND c.out_at <= ? ");
            params.add(request.getOutBefore().toString());
        }

        // 构建分页信息
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").ascending());

        // 执行查询
        Page<Object[]> resultPage = localCashOutRepository.findCashOutWithUser(
                whereClause.toString(), params.toArray(), pageable);

        // 将结果映射到 CashOutDTO
        List<CashOutDTO> cashOutDTOs = resultPage.stream().map(this::mapToCashOutDTO).collect(Collectors.toList());

        return new PageImpl<>(cashOutDTOs, pageable, resultPage.getTotalElements());
    }

    private CashOutDTO mapToCashOutDTO(Object[] objects) {
        CashOutDTO dto = new CashOutDTO();
        int index = 0;
        dto.setId(objects[index] != null ? ((Number) objects[index]).longValue() : null); index++; // Index 0
        dto.setCreateAt(objects[index] != null ? (String) objects[index] : null); index++;         // Index 1
        dto.setMoney(objects[index] != null ? (String) objects[index] : null); index++;            // Index 2
        dto.setOutAt(objects[index] != null ? (String) objects[index] : null); index++;            // Index 3
        dto.setPlatformId(objects[index] != null ? ((Number) objects[index]).longValue() : null); index++; // Index 4
        dto.setUserId(objects[index] != null ? ((Number) objects[index]).longValue() : null); index++; // Index 5
        dto.setUsername(objects[index] != null ? (String) objects[index] : null); index++;                 // Index 7
        dto.setFullname(objects[index] != null ? (String) objects[index] : null); index++;                 // Index 8
        dto.setInviterId(objects[index] != null ? ((Number) objects[index]).longValue() : null); index++;  // Index 9
        dto.setInviterName(objects[index] != null ? (String) objects[index] : null); index++;              // Index 10
        dto.setInviterFullname(objects[index] != null ? (String) objects[index] : null); index++;          // Index 11
        dto.setManagerId(objects[index] != null ? ((Number) objects[index]).longValue() : null); index++;  // Index 12
        dto.setManagerName(objects[index] != null ? (String) objects[index] : null); index++;              // Index 13
        dto.setManagerFullname(objects[index] != null ? (String) objects[index] : null); index++;          // Index 14
        dto.setOrderNumber(objects[index] != null ? (String) objects[index] : null); index++;              // Index 15
        dto.setState(objects[index] != null ? ((Number) objects[index]).intValue() : null); index++;       // Index 16
        dto.setRefund(objects[index] != null ? (String) objects[index] : null); index++;                   // Index 17
        dto.setClassify(objects[index] != null ? ((Number) objects[index]).intValue() : null); index++;    // Index 18
        dto.setRate(objects[index] != null ? ((Number) objects[index]).doubleValue() : null); index++;     // Index 19
        dto.setRecipient(objects[index] != null ? (String) objects[index] : null); index++;                // Index 20
        dto.setBankNumber(objects[index] != null ? (String) objects[index] : null); index++;               // Index 21
        dto.setBankName(objects[index] != null ? (String) objects[index] : null); index++;                 // Index 22
        dto.setBankAddress(objects[index] != null ? (String) objects[index] : null); index++;              // Index 23
        dto.setBankCode(objects[index] != null ? (String) objects[index] : null); index++;                 // Index 24
        dto.setType(objects[index] != null ? ((Number) objects[index]).intValue() : null); index++;        // Index 25
        return dto;
    }

    @Override
    public String completeCashOut(Long id) {
        String url = completeUrl.replace("{id}", id.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", adminAccessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PlatformResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PlatformResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                PlatformResponseDTO apiResponse = response.getBody();
                if (apiResponse != null && apiResponse.getCode() == 0) {
                    syncCashOut();
                    return "success";
                } else if (apiResponse != null) {
                    return apiResponse.getMsg();
                } else {
                    logger.warn("completeCashOut 请求失败，URL: {}, 响应体为空", url);
                    return "响应体为空";
                }
            } else {
                String errorMsg = "请求失败，状态码：" + response.getStatusCode();
                logger.warn("completeCashOut 请求失败，URL: {}, 状态码: {}", url, response.getStatusCode());
                return errorMsg;
            }
        } catch (Exception e) {
            logger.error("completeCashOut 发生异常，URL: {}", url, e);
            return "请求异常：" + e.getMessage();
        }
    }

    @Override
    public String rejectCashOut(CashOutRejectDTO request) {
        String encodedComment = UriUtils.encode(request.getComment(), StandardCharsets.UTF_8);
        String url = rejectUrl.replace("{id}", request.getId().toString())
                               .replace("{comment}", encodedComment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", adminAccessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PlatformResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PlatformResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                PlatformResponseDTO apiResponse = response.getBody();
                if (apiResponse != null && apiResponse.getCode() == 0) {
                    syncCashOut();
                    return "success";
                } else if (apiResponse != null) {
                    return apiResponse.getMsg();
                } else {
                    logger.warn("rejectCashOut 请求失败，URL: {}, 响应体为空", url);
                    return "响应体为空";
                }
            } else {
                String errorMsg = "请求失败，状态码：" + response.getStatusCode();
                logger.warn("rejectCashOut 请求失败，URL: {}, 状态码: {}", url, response.getStatusCode());
                return errorMsg;
            }
        } catch (Exception e) {
            logger.error("rejectCashOut 发生异常，URL: {}", url, e);
            return "请求异常：" + e.getMessage();
        }
    }

    @Override
    public ByteArrayInputStream exportCashOutToExcel(FinanceSearchDTO request) throws Exception {
        // 获取所有符合条件的记录
        request.setPage(0);
        request.setSize(Integer.MAX_VALUE);
        List<CashOutDTO> cashOutList = searchCashOut(request).getContent();

        // 创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CashOut");

        // 创建表头
        String[] headers = {
            "ID", "姓名", "邀请人", "管理人", "平台ID", "用户名", "金额", "创建时间", "出款时间",
            "订单号", "状态", "备注", "费率", "收款人", "银行卡号", "银行名称", "银行地址", "银行代码"
        };

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < headers.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers[col]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // 填充数据
        int rowIdx = 1;
        for (CashOutDTO cashOut : cashOutList) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(cashOut.getId() != null ? cashOut.getId() : 0);
            row.createCell(1).setCellValue(cashOut.getFullname() != null ? cashOut.getFullname() : "");
            row.createCell(2).setCellValue(cashOut.getInviterFullname() != null ? cashOut.getInviterFullname() : "");
            row.createCell(3).setCellValue(cashOut.getManagerFullname() != null ? cashOut.getManagerFullname() : "");
            row.createCell(4).setCellValue(cashOut.getPlatformId() != null ? cashOut.getPlatformId() : 0);
            row.createCell(5).setCellValue(cashOut.getUsername() != null ? cashOut.getUsername() : "");
            row.createCell(6).setCellValue(cashOut.getMoney() != null ? cashOut.getMoney() : "");
            row.createCell(7).setCellValue(cashOut.getCreateAt() != null ? cashOut.getCreateAt() : "");
            row.createCell(8).setCellValue(cashOut.getOutAt() != null ? cashOut.getOutAt() : "");
            row.createCell(9).setCellValue(cashOut.getOrderNumber() != null ? cashOut.getOrderNumber() : "");
            row.createCell(10).setCellValue(mapState(cashOut.getState()));
            row.createCell(11).setCellValue(cashOut.getRefund() != null ? cashOut.getRefund() : "");
            row.createCell(12).setCellValue(cashOut.getRate() != null ? cashOut.getRate() : 0.0);
            row.createCell(13).setCellValue(cashOut.getRecipient() != null ? cashOut.getRecipient() : "");
            row.createCell(14).setCellValue(cashOut.getBankNumber() != null ? cashOut.getBankNumber() : "");
            row.createCell(15).setCellValue(cashOut.getBankName() != null ? cashOut.getBankName() : "");
            row.createCell(16).setCellValue(cashOut.getBankAddress() != null ? cashOut.getBankAddress() : "");
            row.createCell(17).setCellValue(cashOut.getBankCode() != null ? cashOut.getBankCode() : "");
        }

        // 自动调整列宽
        for (int col = 0; col < headers.length; col++) {
            sheet.autoSizeColumn(col);
        }

        // 写入输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // 新增收款账户
    @Override
    @Transactional
    public Boolean addPaymentAccount(PaymentAccountDTO request) {

        paymentAccountRepository.save(PaymentAccount.builder()
            .accountName(request.getAccountName())
            .accountNumber(request.getAccountNumber())
            .accountType(request.getAccountType())
            .accountBank(request.getAccountBank())
            .accountHolder(request.getAccountHolder())
            .accountCurrency(request.getAccountCurrency())
            .accountCurrencyCode(request.getAccountCurrencyCode())
            .accountRegion(request.getAccountRegion())
            .accountStatus(request.getAccountStatus())
            .accountComments(request.getAccountComments())
            .build());

        return true;
    }

    // 更新收款账户
    @Override
    @Transactional
    public Boolean updatePaymentAccount(PaymentAccountDTO request) {
        PaymentAccount account = paymentAccountRepository.findById(request.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("收款账户不存在"));

        account.setAccountName(request.getAccountName());
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setAccountBank(request.getAccountBank());
        account.setAccountHolder(request.getAccountHolder());
        account.setAccountCurrency(request.getAccountCurrency());
        account.setAccountCurrencyCode(request.getAccountCurrencyCode());
        account.setAccountRegion(request.getAccountRegion());
        account.setAccountStatus(request.getAccountStatus());
        account.setAccountComments(request.getAccountComments());

        paymentAccountRepository.save(account);

        return true;
    }

    // 查询收款账户
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAccountDTO> getPaymentAccount(PaymentAccountDTO request) {
        // 创建分页对象
    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

    // 创建查询条件
    Specification<PaymentAccount> spec = (root, query, builder) -> {
        Predicate predicate = builder.conjunction();

        // 根据请求的查询条件动态添加条件
        if (request.getAccountName() != null && !request.getAccountName().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountName"), "%" + request.getAccountName() + "%"));
        }
        if (request.getAccountNumber() != null && !request.getAccountNumber().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountNumber"), "%" + request.getAccountNumber() + "%"));
        }
        if (request.getAccountType() != null && !request.getAccountType().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountType"), "%" + request.getAccountType() + "%"));
        }
        if (request.getAccountRegion() != null && !request.getAccountRegion().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountRegion"), "%" + request.getAccountRegion() + "%"));
        }
        if (request.getAccountBank() != null && !request.getAccountBank().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountBank"), "%" + request.getAccountBank() + "%"));
        }
        if (request.getAccountHolder() != null && !request.getAccountHolder().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountHolder"), "%" + request.getAccountHolder() + "%"));
        }
        if (request.getAccountCurrency() != null && !request.getAccountCurrency().isEmpty()) {
            predicate = builder.and(predicate, builder.like(root.get("accountCurrency"), "%" + request.getAccountCurrency() + "%"));
        }
        if (request.getAccountStatus() != null) {
            predicate = builder.and(predicate, builder.equal(root.get("accountStatus"), request.getAccountStatus()));
        }

        return predicate;
    };

    // 使用 Specification 和 Pageable 执行查询并返回分页结果
    Page<PaymentAccount> pageResult = paymentAccountRepository.findAll(spec, pageable);

    // 转换为 DTO
    List<PaymentAccountDTO> dtoList = pageResult.getContent().stream()
        .map(account -> PaymentAccountDTO.builder()
            .accountId(account.getAccountId())
            .accountName(account.getAccountName())
            .accountNumber(account.getAccountNumber())
            .accountType(account.getAccountType())
            .accountBank(account.getAccountBank())
            .accountHolder(account.getAccountHolder())
            .accountCurrency(account.getAccountCurrency())
            .accountCurrencyCode(account.getAccountCurrencyCode())
            .accountRegion(account.getAccountRegion())
            .accountStatus(account.getAccountStatus())
            .accountComments(account.getAccountComments())
            .build())
        .collect(Collectors.toList());

    // 返回分页结果
    return new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
    }

    // 删除收款账户
    @Override
    @Transactional
    public Boolean deletePaymentAccount(Long accountId) {
        paymentAccountRepository.deleteById(accountId);
        return true;
    }

    /**
     * 将 state 数值转换为对应的文字描述
     *
     * @param state 状态数值
     * @return 对应的文字描述
     */
    private String mapState(Integer state) {
        if (state == null) {
            return "";
        }
        switch (state) {
            case 1:
                return "已支付";
            case 0:
                return "申请中";
            case -1:
                return "已拒绝";
            default:
                return String.valueOf(state); // 如果有其他未定义的状态，返回其数值
        }
    }

}