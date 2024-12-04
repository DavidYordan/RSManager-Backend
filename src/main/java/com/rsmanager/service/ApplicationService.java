package com.rsmanager.service;

import com.rsmanager.dto.application.*;

import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {

    /**
     * 创建流程单
     */
    Long createApplication(ApplicationCreateRequestDTO request, MultipartFile[] files);

    /**
     * 删除流程单
     */
    Boolean deleteApplication(ApplicationActionDTO request);

    /**
     * 激活申请
     */
    Boolean activateApplication(ApplicationActionDTO request);

    /**
     * 取消申请
     */
    Boolean cancelApplication(ApplicationActionDTO request);

    /**
     * 提交申请
     */
    Boolean submitApplication(ApplicationActionDTO request);

    /**
     * 更新申请
     */
    Boolean updateApplication(ApplicationProcessUpdateDTO request);

    /**
     * 撤回申请
     */
    Boolean withdrawApplication(ApplicationActionDTO request);

    /**
     * 财务审批申请
     */
    Boolean approveFinanceApplication(ApplicationActionDTO request);

    /**
     * 申请链接
     */
    Boolean submitForLink(ApplicationSubmitForLinkDTO request);

    /**
     * 链接审批申请
     */
    Boolean approvelink(ApplicationActionDTO request);

    /**
     * 完成申请
     */
    Boolean finishedApplication(ApplicationActionDTO request);

    /**
     * 归档申请
     */
    Boolean archiveApplication(ApplicationActionDTO request);

    /**
     * 退款申请
     */
    Boolean refundApplication(ApplicationActionDTO request);

    /**
     * 添加支付记录
     */
    Boolean addPaymentRecord(PaymentAddDTO request, MultipartFile[] files);

    /**
     * 提交支付记录
     */
    Boolean submitPaymentRecord(PaymentActionDTO request);

    /**
     * 审核支付记录
     */
    Boolean approvePaymentRecord(PaymentActionDTO request);

    /**
     * 拒绝支付记录
     */
    Boolean rejectPaymentRecord(PaymentActionDTO request);

    /**
     * 撤销审核支付记录
     */
    Boolean disApprovePaymentRecord(PaymentActionDTO request);

    /**
     * 编辑支付记录
     */
    Boolean updatePaymentRecord(PaymentUpdateDTO requset, MultipartFile[] files);

    /**
     * 删除支付记录
     */
    Boolean deletePaymentRecord(PaymentActionDTO request);

    /**
     * 切换为补充角色编辑态
     */
    Boolean addRoleEditing(ActionStrDTO request);

    /**
     * 切换为升级角色编辑态
     */
    Boolean upgradeRoleEditing(ActionStrDTO request);

    /**
     * 取消角色编辑态
     */
    Boolean cancelRoleEditing(ApplicationActionDTO request);

    /**
     * 保存编辑中的补充角色信息
     */
    Boolean saveAddRoleEditing(ActionStrDTO request);

    /**
     * 保存编辑中的升级角色信息
     */
    Boolean saveUpgradeRoleEditing(ActionStrDTO request);

    /**
     * 提交补充角色审核
     */
    Boolean submitAddRole(ApplicationActionDTO request);

    /**
     * 提交升级角色审核
     */
    Boolean submitUpgradeRole(ApplicationActionDTO request);

    /**
     * 财务通过角色升级审核
     */
    Boolean approveRoleAddByFinance(ApplicationActionDTO request);

    /**
     * 财务通过角色升级审核
     */
    Boolean approveRoleUpgradeByFinance(ApplicationActionDTO request);

    /**
     * 上传合同文件
     */
    Boolean uploadContractFiles(Long processId, MultipartFile[] files);

    /**
     * 查看申请详情
     */
    ApplicationResponseDTO viewApplication(Long processId);

    /**
     * 根据条件搜索待办
     */
    Page<ApplicationResponseDTO> searchTodoApplications(ApplicationSearchDTO request);


    /**
     * 根据条件搜索申请
     */
    Page<ApplicationResponseDTO> searchApplications(ApplicationSearchDTO request);

    /**
     * 修改管理人
     */
    ApplicationResponseDTO changeManager(ApplicationUpdateDTO request);

    /**
     * 检查用户姓名是否存在
     */
    String checkFullname(String fullname);

    /**
     * 检查平台账号是否存在及是否允许创建
     */
    ValidatePlatformAccountDTO validatePlatformAccount(String platformAccount);
}
