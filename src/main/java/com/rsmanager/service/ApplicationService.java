package com.rsmanager.service;

import com.rsmanager.dto.application.*;

import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {

    /**
     * 创建流程单
     *
     * @param request 申请创建请求DTO
     * @param files   附件文件
     * @return ServiceResponseDTO
     */
    Long createApplication(ApplicationCreateRequestDTO request, MultipartFile[] files);

    /**
     * 上传合同文件
     *
     * @param processId 流程单ID
     * @param files     合同文件
     * @return ServiceResponseDTO
     */
    Boolean uploadContractFiles(Long processId, MultipartFile[] files);

    /**
     * 根据条件搜索待办
     *
     * @param request  查询条件
     * @return 申请流程记录DTO分页
     */
    Page<ViewApplicationResponseDTO> searchTodoApplications(ApplicationSearchDTO request);


    /**
     * 根据条件搜索申请
     *
     * @param request  查询条件
     * @return 申请流程记录DTO分页
     */
    Page<ViewApplicationResponseDTO> searchApplications(ApplicationSearchDTO request);

    /**
     * 提交申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean submitApplication(ApplicationActionDTO request);

    /**
     * 更新申请
     *
     * @param request 更新请求DTO
     * @return Boolean
     */
    Boolean updateApplication(ApplicationProcessUpdateDTO request);

    /**
     * 撤回申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean withdrawApplication(ApplicationActionDTO request);


    /**
     * 切换为升级角色编辑态
     */
    Boolean updateRoleEditing(ApplicationUpdateRoleDTO request);

    /**
     * 取消角色编辑态
     */
    Boolean cancelUpdateRoleEditing(ApplicationActionDTO request);

    /**
     * 保存编辑中的升级角色信息
     */
    Boolean saveRoleEditing(ApplicationUpdateRoleDTO request);

    /**
     * 提交升级角色审核
     */
    Boolean submitRoleUpgrade(ApplicationActionDTO request);

    /**
     * 财务通过角色升级审核
     */
    Boolean approveRoleUpgradeByFinance(ApplicationActionDTO request);

    /**
     * 主管通过角色升级审核
     */
    Boolean approveRoleUpgradeByManager(ApplicationActionDTO request);

    /**
     * 补充历史角色信息
     */
    Boolean updateRoleHistory(ApplicationUpdateRoleDTO request);

    /**
     * 审核历史角色信息
     */
    Boolean approveRoleHistory(ApplicationActionDTO request);

    /**
     * 查看申请详情
     *
     * @param processId 流程单ID
     * @return 申请详情DTO
     */
    Optional<ApplicationInfoResponseDTO> viewApplication(Long processId);

    /**
     * 财务审批申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean approveFinanceApplication(ApplicationActionDTO request);

    /**
     * 链接审批申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean approvelink(ApplicationActionDTO request);


    /**
     * 申请链接
     *
     * @param request 提交申请请求DTO
     * @return Boolean
     */
    Boolean submitForLink(ApplicationSubmitForLinkDTO request);

    /**
     * 完成申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean finishedApplication(ApplicationActionDTO request);

    /**
     * 归档申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean archiveApplication(ApplicationActionDTO request);

    /**
     * 拒绝申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean cancelApplication(ApplicationActionDTO request);

    /**
     * 激活申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean activateApplication(ApplicationActionDTO request);

    /**
     * 添加支付记录
     *
     * @param PaymentAddDTO
     * @return Boolean
     */
    Boolean addPaymentRecord(PaymentAddDTO applicationPaymentRecordDTO, MultipartFile[] files);

    /**
     * 审核支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    Boolean approvePaymentRecord(PaymentActionDTO request);

    /**
     * 撤销审核支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    Boolean disApprovePaymentRecord(PaymentActionDTO request);

    /**
     * 编辑支付记录
     *
     * @param PaymentUpdateDTO
     * @param files 附件文件
     * @return Boolean
     */
    Boolean updatePaymentRecord(PaymentUpdateDTO requset, MultipartFile[] files);

    /**
     * 删除支付记录
     *
     * @param PaymentActionDTO
     * @return Boolean
     */
    Boolean deletePaymentRecord(PaymentActionDTO request);

    /**
     * 检查用户姓名是否存在
     *
     * @param fullname 用户姓名
     * @return 
     */
    String checkFullname(String fullname);
}
