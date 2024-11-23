package com.rsmanager.service;

import java.util.List;

public interface DataSyncService {

    /**
     * 每隔 30 分钟执行一次同步任务
     */
    void syncAllData();

    /**
     * 同步 TbUser 数据
     * @return 同步的 user_id 列表
     */
    void syncTbUser();

    /**
     * 同步 SysUser 数据
     */
    void syncSysUser();

    /**
     * 同步 UserIntegral 数据
     * @param userIds 需要同步的 user_id 列表
     */
    void syncUserIntegral(List<Integer> userIds);

    /**
     * 同步 UserMoney 数据
     * @param userIds 需要同步的 user_id 列表
     */
    void syncUserMoney(List<Integer> userIds);

    /**
     * 同步 UserIntegralDetails 数据
     * @return 同步的 user_id 列表
     */
    void syncUserIntegralDetails();

    /**
     * 同步 UserMoneyDetails 数据
     * @return 同步的 user_id 列表
     */
    void syncUserMoneyDetails();

    /**
     * 同步 Invite 数据
     * @return 同步的 user_id 列表
     */
    void syncInvite();

    /**
     * 同步 InviteMoney 数据
     * @param userIds 需要同步的 user_id 列表
     */
    void syncInviteMoney(List<Integer> userIds);

    /**
     * 同步 AgentMoney 数据
     */
    void syncAgentMoney();

    /**
     * 同步 AgentWidthdraw 数据
     */
    void syncAgentWidthdraw();

    /**
     * 同步 CashOut 数据
     */
    Boolean syncCashOut();

    void syncTikTokRelationshipToRemoteB();

    void syncTikTokAccountFromRemoteB();

    void syncTikTokVideoDetailsFromRemoteB();
}
