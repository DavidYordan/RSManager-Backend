package com.rsmanager.service;

import java.io.ByteArrayInputStream;

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
    ByteArrayInputStream exportCashOutToExcel(FinanceSearchDTO request) throws Exception;
}
