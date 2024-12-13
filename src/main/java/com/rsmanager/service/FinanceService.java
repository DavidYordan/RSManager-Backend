package com.rsmanager.service;

import org.springframework.data.domain.Page;

import com.rsmanager.dto.finance.*;

public interface FinanceService {

    /**
     * 同步提款单据
     * 
     * @return Boolean 是否同步成功
     */
    Boolean syncCashOut();

    /**
     * 查询提现记录
     * 
     * @param request FinanceSearchDTO 请求参数
     * @return Page<CashOutDTO> 分页的 CashOutDTO 列表
     */
    Page<CashOutDTO> searchCashOut(FinanceSearchDTO request);

    /**
     * 完成提款
     * 
     * @param id 提款记录 ID
     * @return String 消息
     */
    String completeCashOut(Long id);

    /**
     * 拒绝提款
     * 
     * @param request CashOutRejectDTO 请求参数
     * @return String 消息
     */
    String rejectCashOut(CashOutRejectDTO request);

    /**
     * 导出提款记录到 Excel
     * 
     * @param request FinanceSearchDTO 请求参数
     * @return ByteArrayInputStream 导出的 Excel 文件
     * @throws Exception
     */
    // ByteArrayInputStream exportCashOutToExcel(FinanceSearchDTO request) throws Exception;

    /**
     * 导出提款记录到 ZIP
     * 
     * @param request FinanceSearchDTO 请求参数
     * @return ExportResult 导出的 ZIP 文件
     * @throws Exception
     */
    ExportResult exportCashOutZip(FinanceSearchDTO request) throws Exception;

    // 更新收款账户
    Boolean updatePaymentAccount(PaymentAccountDTO request);

    // 添加收款账户
    Boolean addPaymentAccount(PaymentAccountDTO request);

    // 查询收款账户
    Page<PaymentAccountDTO> getPaymentAccount(PaymentAccountDTO request);

    // 删除收款账户
    Boolean deletePaymentAccount(Long accountId);
}
