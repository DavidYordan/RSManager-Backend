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
     * 删除流程单
     * 
     * @param request 删除请求DTO
     * @return Boolean
     */
    Boolean deleteApplication(ApplicationActionDTO request);

    /**
     * 激活申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean activateApplication(ApplicationActionDTO request);

    /**
     * 取消申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean cancelApplication(ApplicationActionDTO request);

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
     * 财务审批申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean approveFinanceApplication(ApplicationActionDTO request);

    /**
     * 申请链接
     *
     * @param request 提交申请请求DTO
     * @return Boolean
     */
    Boolean submitForLink(ApplicationSubmitForLinkDTO request);

    /**
     * 链接审批申请
     *
     * @param ApplicationActionDTO
     * @return Boolean
     */
    Boolean approvelink(ApplicationActionDTO request);

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
    Boolean submitAddRoleUpgrade(ApplicationActionDTO request);

    /**
     * 提交升级角色审核
     */
    Boolean submitUpgradeRoleUpgrade(ApplicationActionDTO request);

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
     *
     * @param processId 流程单ID
     * @param files     合同文件
     * @return ServiceResponseDTO
     */
    Boolean uploadContractFiles(Long processId, MultipartFile[] files);

    /**
     * 查看申请详情
     *
     * @param processId 流程单ID
     * @return 申请详情DTO
     */
    Optional<SearchApplicationResponseDTO> viewApplication(Long processId);

    /**
     * 根据条件搜索待办
     *
     * @param request  查询条件
     * @return 申请流程记录DTO分页
     */
    Page<SearchApplicationResponseDTO> searchTodoApplications(ApplicationSearchDTO request);


    /**
     * 根据条件搜索申请
     *
     * @param request  查询条件
     * @return 申请流程记录DTO分页
     */
    Page<SearchApplicationResponseDTO> searchApplications(ApplicationSearchDTO request);

    /**
     * 检查用户姓名是否存在
     *
     * @param fullname 用户姓名
     * @return 
     */
    String checkFullname(String fullname);
}
