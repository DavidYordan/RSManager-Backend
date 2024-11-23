package com.rsmanager.service.impl;

import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.repository.remote.*;
import com.rsmanager.repository.remoteB.RemoteBTikTokRelationshipRepository;
import com.rsmanager.repository.remoteB.RemoteBTikTokVideoDetailsRepository;
import com.rsmanager.repository.remoteB.RemoteBTiktokAccountRepository;
import com.rsmanager.service.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    // 本地 Repository 接口
    @Autowired
    private LocalTbUserRepository localTbUserRepository;

    @Autowired
    private LocalUserIntegralRepository localUserIntegralRepository;

    @Autowired
    private LocalUserMoneyRepository localUserMoneyRepository;

    @Autowired
    private LocalUserIntegralDetailsRepository localUserIntegralDetailsRepository;

    @Autowired
    private LocalUserMoneyDetailsRepository localUserMoneyDetailsRepository;

    @Autowired
    private LocalInviteRepository localInviteRepository;

    @Autowired
    private LocalInviteMoneyRepository localInviteMoneyRepository;

    @Autowired
    private LocalAgentMoneyRepository localAgentMoneyRepository;

    @Autowired
    private LocalAgentWidthdrawRepository localAgentWidthdrawRepository;

    @Autowired
    private LocalCashOutRepository localCashOutRepository;

    @Autowired
    private LocalSysUserRepository localSysUserRepository;

    @Autowired
    private TikTokRelationshipRepository localTikTokRelationshipRepository;

    @Autowired
    private TiktokAccountRepository localTikTokAccountRepository;

    @Autowired
    private TikTokVideoDetailsRepository localTikTokVideoDetailsRepository;

    // 远程 Repository 接口
    @Autowired
    private RemoteTbUserRepository remoteTbUserRepository;

    @Autowired
    private RemoteUserIntegralRepository remoteUserIntegralRepository;

    @Autowired
    private RemoteUserMoneyRepository remoteUserMoneyRepository;

    @Autowired
    private RemoteUserIntegralDetailsRepository remoteUserIntegralDetailsRepository;

    @Autowired
    private RemoteUserMoneyDetailsRepository remoteUserMoneyDetailsRepository;

    @Autowired
    private RemoteInviteRepository remoteInviteRepository;

    @Autowired
    private RemoteInviteMoneyRepository remoteInviteMoneyRepository;

    @Autowired
    private RemoteAgentMoneyRepository remoteAgentMoneyRepository;

    @Autowired
    private RemoteAgentWidthdrawRepository remoteAgentWidthdrawRepository;

    @Autowired
    private RemoteCashOutRepository remoteCashOutRepository;

    @Autowired
    private RemoteSysUserRepository remoteSysUserRepository;

    // 远程 Repository2 接口
    @Autowired
    private RemoteBTikTokRelationshipRepository remoteBTikTokRelationshipRepository;

    @Autowired
    private RemoteBTiktokAccountRepository remoteBTikTokAccountRepository;

    @Autowired
    private RemoteBTikTokVideoDetailsRepository remoteBTikTokVideoDetailsRepository;

    /**
     * 每隔 30 分钟执行一次同步任务
     */
    @Override
    @Scheduled(fixedRate = 30 * 60 * 1000)  // 30 分钟
    @Transactional(transactionManager = "localTransactionManager")
    public void syncAllData() {
        logger.info("开始同步所有数据...");

        try {
            // 同步 TbUser 数据
            syncTbUser();

            // 同步 SysUser 数据
            syncSysUser();

            // 同步 UserIntegralDetails 数据
            syncUserIntegralDetails();

            // 同步 UserMoneyDetails 数据
            syncUserMoneyDetails();

            // 同步 Invite 数据
            syncInvite();

            // 同步 AgentMoney 数据
            syncAgentMoney();
            
            // 同步 AgentWidthdraw 数据
            syncAgentWidthdraw();

            // 同步 CashOut 数据
            syncCashOut();

            logger.info("所有数据同步完成。");
        } catch (Exception e) {
            logger.error("数据同步过程中发生异常: ", e);
        }
    }

    /**
     * 同步 TbUser 数据
     * @return 同步的 user_id 列表
     */
    @Override
    public void syncTbUser() {
        logger.info("开始同步 TbUser 数据...");

        List<Integer> userIds = Collections.emptyList();

        try {
            // 从本地数据库获取最新的 updateTime
            String lastUpdateTime = localTbUserRepository.findMaxUpdateTime();
            if (lastUpdateTime == null || lastUpdateTime.isEmpty()) {
                lastUpdateTime = "1970-01-01 00:00:00";  // 如果本地数据库为空，设置一个默认时间
                logger.warn("TbUser 本地数据库的最新更新时间为空，设置为默认值: {}", lastUpdateTime);
            } else {
                logger.debug("TbUser 本地数据库的最新更新时间: {}", lastUpdateTime);
            }

            // 从远程数据库查询比 lastUpdateTime 更新的 TbUser
            List<TbUser> updatedRemoteTbUsers = remoteTbUserRepository.findByUpdateTimeAfter(lastUpdateTime);
            logger.debug("从远程数据库查询到的 TbUser 更新数量: {}", updatedRemoteTbUsers.size());

            if (!updatedRemoteTbUsers.isEmpty()) {
                // 保存到本地数据库
                localTbUserRepository.saveAll(updatedRemoteTbUsers);
                logger.info("已保存 {} 条 TbUser 更新记录到本地数据库", updatedRemoteTbUsers.size());

                // 提取所有 user_id 并去重
                userIds = updatedRemoteTbUsers.stream()
                        .map(TbUser::getUserId)
                        .filter(Objects::nonNull)
                        .map(Long::intValue)
                        .collect(Collectors.toSet())  // 去重
                        .stream()
                        .collect(Collectors.toList());

                logger.info("同步 TbUser 后提取到的 user_id 数量: {}", userIds.size());
            } else {
                logger.info("没有 TbUser 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 TbUser 数据时发生异常: ", e);
        }

        if (!userIds.isEmpty()) {
            syncUserIntegral(userIds);
            syncUserMoney(userIds);
        }
    }

    /**
     * 同步 SysUser 数据
     */
    @Override
    public void syncSysUser() {
        logger.info("开始同步 SysUser 数据...");

        try {
            // 从本地数据库获取最大的 userId
            Long lastUserId = localSysUserRepository.findMaxUserId();
            if (lastUserId == null) {
                lastUserId = 0L;  // 如果本地数据库为空，设置为默认值
                logger.warn("SysUser 本地数据库的最大 userId 为空，设置为默认值: {}", lastUserId);
            } else {
                logger.debug("SysUser 本地数据库的最大 userId: {}", lastUserId);
            }

            // 从远程数据库查询 userId 大于 lastUserId 的 SysUser
            List<SysUser> updatedRemoteSysUsers = remoteSysUserRepository.findByUserIdGreaterThan(lastUserId);
            logger.debug("从远程数据库查询到的 SysUser 更新数量: {}", updatedRemoteSysUsers.size());

            if (!updatedRemoteSysUsers.isEmpty()) {
                // 保存到本地数据库
                localSysUserRepository.saveAll(updatedRemoteSysUsers);
                logger.info("已保存 {} 条 SysUser 更新记录到本地数据库", updatedRemoteSysUsers.size());
            } else {
                logger.info("没有 SysUser 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 SysUser 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 UserIntegral 数据
     * @param userIds 需要同步的 user_id 列表
     */
    @Override
    public void syncUserIntegral(List<Integer> userIds) {
        logger.info("开始同步 UserIntegral 数据，涉及的 user_id 数量: {}", userIds.size());

        try {
            // 从远程数据库查询指定 userIds 的 UserIntegral 记录
            List<UserIntegral> remoteUserIntegrals = remoteUserIntegralRepository.findByUserIdIn(userIds);
            logger.debug("从远程数据库查询到的 UserIntegral 记录数量: {}", remoteUserIntegrals.size());

            if (!remoteUserIntegrals.isEmpty()) {
                // 保存到本地数据库
                localUserIntegralRepository.saveAll(remoteUserIntegrals);
                logger.info("已保存 {} 条 UserIntegral 记录到本地数据库", remoteUserIntegrals.size());
            } else {
                logger.info("没有 UserIntegral 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 UserIntegral 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 UserMoney 数据
     * @param userIds 需要同步的 user_id 列表
     */
    @Override
    public void syncUserMoney(List<Integer> userIds) {
        logger.info("开始同步 UserMoney 数据，涉及的 user_id 数量: {}", userIds.size());

        try {
            // 从远程数据库查询指定 userIds 的 UserMoney 记录
            List<UserMoney> remoteUserMoneys = remoteUserMoneyRepository.findByUserIdIn(userIds);
            logger.debug("从远程数据库查询到的 UserMoney 记录数量: {}", remoteUserMoneys.size());

            if (!remoteUserMoneys.isEmpty()) {
                // 保存到本地数据库
                localUserMoneyRepository.saveAll(remoteUserMoneys);
                logger.info("已保存 {} 条 UserMoney 记录到本地数据库", remoteUserMoneys.size());
            } else {
                logger.info("没有 UserMoney 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 UserMoney 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 UserIntegralDetails 数据
     * @return 同步的 user_id 列表
     */
    @Override
    public void syncUserIntegralDetails() {
        logger.info("开始同步 UserIntegralDetails 数据...");
    
        List<Integer> userIds = Collections.emptyList();
    
        try {
            // 从本地数据库获取最大的 id
            Integer lastId = localUserIntegralDetailsRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;  // 如果本地数据库为空，设置为默认值
                logger.warn("UserIntegralDetails 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("UserIntegralDetails 本地数据库的最大 id: {}", lastId);
            }
    
            // 从远程数据库查询 id 大于 lastId 的 UserIntegralDetails
            List<UserIntegralDetails> updatedRemoteDetails = remoteUserIntegralDetailsRepository.findByIdGreaterThan(lastId);
            logger.debug("从远程数据库查询到的 UserIntegralDetails 更新数量: {}", updatedRemoteDetails.size());
    
            if (!updatedRemoteDetails.isEmpty()) {
                // 保存到本地数据库
                localUserIntegralDetailsRepository.saveAll(updatedRemoteDetails);
                logger.info("已保存 {} 条 UserIntegralDetails 更新记录到本地数据库", updatedRemoteDetails.size());
    
                // 提取所有 user_id 并去重
                userIds = updatedRemoteDetails.stream()
                        .map(UserIntegralDetails::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
                        .stream()
                        .collect(Collectors.toList());
    
                logger.info("同步 UserIntegralDetails 后提取到的 user_id 数量: {}", userIds.size());
            } else {
                logger.info("没有 UserIntegralDetails 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 UserIntegralDetails 数据时发生异常: ", e);
        }
        
        if (!userIds.isEmpty()) {
            syncUserIntegral(userIds);
        }
    }

    /**
     * 同步 UserMoneyDetails 数据
     * @return 同步的 user_id 列表
     */
    @Override
    public void syncUserMoneyDetails() {
        logger.info("开始同步 UserMoneyDetails 数据...");
    
        Set<Integer> userIds = new HashSet<>();
    
        try {
            // 从本地数据库获取最大的 id
            Integer lastId = localUserMoneyDetailsRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;  // 如果本地数据库为空，设置为默认值
                logger.warn("UserMoneyDetails 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("UserMoneyDetails 本地数据库的最大 id: {}", lastId);
            }
    
            // 从远程数据库查询 id 大于 lastId 的 UserMoneyDetails
            List<UserMoneyDetails> updatedRemoteDetails = remoteUserMoneyDetailsRepository.findByIdGreaterThan(lastId);
            logger.debug("从远程数据库查询到的 UserMoneyDetails 更新数量: {}", updatedRemoteDetails.size());
    
            if (!updatedRemoteDetails.isEmpty()) {
                // 保存到本地数据库
                localUserMoneyDetailsRepository.saveAll(updatedRemoteDetails);
                logger.info("已保存 {} 条 UserMoneyDetails 更新记录到本地数据库", updatedRemoteDetails.size());
    
                // 提取所有 user_id 并去重
                userIds = updatedRemoteDetails.stream()
                        .map(UserMoneyDetails::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    
                logger.info("同步 UserMoneyDetails 后提取到的 user_id 数量: {}", userIds.size());
            } else {
                logger.info("没有 UserMoneyDetails 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 UserMoneyDetails 数据时发生异常: ", e);
        }
    
        if (!userIds.isEmpty()) {
            syncUserMoney(new ArrayList<>(userIds));
        }
    }

    /**
     * 同步 Invite 数据
     * @return 同步的 user_id 列表
     */
    @Override
    public void syncInvite() {
        logger.info("开始同步 Invite 数据...");
    
        Set<Integer> userIds = new HashSet<>();
    
        try {
            // 从本地数据库获取最大的 id
            Integer lastId = localInviteRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;  // 如果本地数据库为空，设置为默认值
                logger.warn("Invite 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("Invite 本地数据库的最大 id: {}", lastId);
            }
    
            // 从远程数据库查询 id 大于 lastId 的 Invite
            List<Invite> updatedRemoteInvites = remoteInviteRepository.findByIdGreaterThan(lastId);
            logger.debug("从远程数据库查询到的 Invite 更新数量: {}", updatedRemoteInvites.size());
    
            if (!updatedRemoteInvites.isEmpty()) {
                // 保存到本地数据库
                localInviteRepository.saveAll(updatedRemoteInvites);
                logger.info("已保存 {} 条 Invite 更新记录到本地数据库", updatedRemoteInvites.size());
    
                // 提取所有 user_id 和 invitee_user_id 并去重
                userIds = updatedRemoteInvites.stream()
                        .map(Invite::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    
                Set<Integer> inviteeUserIds = updatedRemoteInvites.stream()
                        .map(Invite::getInviteeUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    
                userIds.addAll(inviteeUserIds);
    
                logger.info("同步 Invite 后提取到的 user_id 数量: {}", userIds.size());
            } else {
                logger.info("没有 Invite 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 Invite 数据时发生异常: ", e);
        }
    
        if (!userIds.isEmpty()) {
            syncInviteMoney(new ArrayList<>(userIds));
        }
    }

    /**
     * 同步 InviteMoney 数据
     * @param userIds 需要同步的 user_id 列表
     */
    @Override
    public void syncInviteMoney(List<Integer> userIds) {
        logger.info("开始同步 InviteMoney 数据，涉及的 user_id 数量: {}", userIds.size());

        try {
            // 从远程数据库查询指定 userIds 的 InviteMoney 记录
            List<InviteMoney> remoteInviteMoneys = remoteInviteMoneyRepository.findByUserIdIn(userIds);
            logger.debug("从远程数据库查询到的 InviteMoney 记录数量: {}", remoteInviteMoneys.size());

            if (!remoteInviteMoneys.isEmpty()) {
                // 保存到本地数据库
                localInviteMoneyRepository.saveAll(remoteInviteMoneys);
                logger.info("已保存 {} 条 InviteMoney 记录到本地数据库", remoteInviteMoneys.size());
            } else {
                logger.info("没有 InviteMoney 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 InviteMoney 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 AgentMoney 数据
     */
    @Override
    public void syncAgentMoney() {
        logger.info("开始同步 AgentMoney 数据...");
    
        try {
            // 从本地数据库获取最大的 id
            Long lastId = localAgentMoneyRepository.findMaxId();
            if (lastId == null) {
                lastId = 0L;  // 默认 id
                logger.warn("AgentMoney 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("AgentMoney 本地数据库的最大 id: {}", lastId);
            }
    
            // 从远程数据库查询 id 大于 lastId 的 AgentMoney
            List<AgentMoney> remoteAgentMoneys = remoteAgentMoneyRepository.findByIdGreaterThan(lastId);
            logger.debug("从远程数据库查询到的 AgentMoney 更新数量: {}", remoteAgentMoneys.size());
    
            if (!remoteAgentMoneys.isEmpty()) {
                // 保存到本地数据库
                localAgentMoneyRepository.saveAll(remoteAgentMoneys);
                logger.info("已保存 {} 条 AgentMoney 记录到本地数据库", remoteAgentMoneys.size());
            } else {
                logger.info("没有 AgentMoney 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 AgentMoney 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 AgentWidthdraw 数据
     */
    @Override
    public void syncAgentWidthdraw() {
        logger.info("开始同步 AgentWidthdraw 数据...");

        try {
            // 从本地数据库获取最新的 updateTime
            LocalDateTime lastUpdateTime = localAgentWidthdrawRepository.findMaxUpdateTime();
            if (lastUpdateTime == null) {
                lastUpdateTime = LocalDateTime.of(1970, 1, 1, 0, 0);  // 默认时间
                logger.warn("AgentWidthdraw 本地数据库的最新 updateTime 为空，设置为默认值: {}", lastUpdateTime);
            } else {
                logger.debug("AgentWidthdraw 本地数据库的最新 updateTime: {}", lastUpdateTime);
            }

            // 从远程数据库查询比 lastUpdateTime 更新的 AgentWidthdraw
            List<AgentWidthdraw> remoteAgentWidthdraws = remoteAgentWidthdrawRepository.findByUpdateTimeAfter(lastUpdateTime);
            logger.debug("从远程数据库查询到的 AgentWidthdraw 更新数量: {}", remoteAgentWidthdraws.size());

            if (!remoteAgentWidthdraws.isEmpty()) {
                // 保存到本地数据库
                localAgentWidthdrawRepository.saveAll(remoteAgentWidthdraws);
                logger.info("已保存 {} 条 AgentWidthdraw 记录到本地数据库", remoteAgentWidthdraws.size());
            } else {
                logger.info("没有 AgentWidthdraw 需要同步的数据。");
            }
        } catch (Exception e) {
            logger.error("同步 AgentWidthdraw 数据时发生异常: ", e);
        }

    }

    /**
     * 同步 CashOut 数据
     */
    @Override
    public Boolean syncCashOut() {
        logger.info("开始同步 CashOut 数据...");
    
        try {
            // 从本地数据库获取最新的 createAt 和 outAt
            String lastCreateAt = localCashOutRepository.findMaxCreateAt();
            String lastOutAt = localCashOutRepository.findMaxOutAt();
    
            if (lastCreateAt == null || lastCreateAt.isEmpty()) {
                lastCreateAt = "1970-01-01 00:00:00";  // 默认时间
                logger.warn("CashOut 本地数据库的最新 createAt 为空，设置为默认值: {}", lastCreateAt);
            } else {
                logger.debug("CashOut 本地数据库的最新 createAt: {}", lastCreateAt);
            }
    
            if (lastOutAt == null || lastOutAt.isEmpty()) {
                lastOutAt = "1970-01-01 00:00:00";  // 默认时间
                logger.warn("CashOut 本地数据库的最新 outAt 为空，设置为默认值: {}", lastOutAt);
            } else {
                logger.debug("CashOut 本地数据库的最新 outAt: {}", lastOutAt);
            }
    
            // 从远程数据库查询比 lastCreateAt 更新的 CashOut
            List<CashOut> remoteCashOutByCreateAt = remoteCashOutRepository.findByCreateAtAfter(lastCreateAt);
            List<CashOut> remoteCashOutByOutAt = remoteCashOutRepository.findByOutAtAfter(lastOutAt);
    
            // 合并两个列表，去重
            Set<CashOut> remoteCashOutSet = new HashSet<>(remoteCashOutByCreateAt);
            remoteCashOutSet.addAll(remoteCashOutByOutAt);
    
            List<CashOut> remoteCashOutList = new ArrayList<>(remoteCashOutSet);
    
            logger.debug("从远程数据库查询到的 CashOut 更新数量: {}", remoteCashOutList.size());
    
            if (!remoteCashOutList.isEmpty()) {
                // 保存到本地数据库
                localCashOutRepository.saveAll(remoteCashOutList);
                logger.info("已保存 {} 条 CashOut 记录到本地数据库", remoteCashOutList.size());
            } else {
                logger.info("没有 CashOut 需要同步的数据。");
            }

            return true;
            
        } catch (Exception e) {
            logger.error("同步 CashOut 数据时发生异常: ", e);
            return false;
        }
    }

    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)  // 每5分钟执行一次
    @Transactional(transactionManager = "remoteBTransactionManager")
    public void syncTikTokRelationshipToRemoteB() {
        logger.info("开始将本地 TikTokRelationship 数据同步到远程服务器 B...");

        try {
            List<TiktokRelationship> relationships = localTikTokRelationshipRepository.findAll();

            LocalDateTime updatedAt = LocalDateTime.now();

            if (!relationships.isEmpty()) {
                // 将 TiktokRelationship 映射到 TiktokRelationshipRemoteB
                List<TiktokRelationshipRemoteB> remoteRelationships = relationships.stream()
                        .map(rel -> TiktokRelationshipRemoteB.builder()
                                .recordId(rel.getRecordId())
                                .tiktokAccount(rel.getTiktoker().getTiktokAccount())
                                .startDate(rel.getStartDate())
                                .endDate(rel.getEndDate())
                                .status(rel.getStatus())
                                .updatedAt(updatedAt)
                                .build())
                        .collect(Collectors.toList());

                remoteBTikTokRelationshipRepository.saveAll(remoteRelationships);
                logger.info("已将 {} 条 TikTokRelationship 记录同步到远程服务器 B。", relationships.size());
            } else {
                logger.info("本地没有 TikTokRelationship 数据需要同步。");
            }
        } catch (Exception e) {
            logger.error("同步 TikTokRelationship 数据到远程服务器 B 时发生异常：", e);
        }
    }

    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)  // 每5分钟执行一次
    @Transactional(transactionManager = "localTransactionManager")
    public void syncTikTokAccountFromRemoteB() {
        logger.info("开始同步远程服务器 B 的 TikTokAccount 数据到本地...");

        try {
            // 获取本地最新的 updatedAt
            LocalDateTime lastUpdatedAt = localTikTokAccountRepository.findMaxUpdateAt();
            if (lastUpdatedAt == null) {
                lastUpdatedAt = LocalDateTime.of(1970, 1, 1, 0, 0);
                logger.warn("本地 TikTokAccount 的最新 updatedAt 为空，使用默认时间：{}", lastUpdatedAt);
            }

            // 从远程 B 获取 updatedAt 大于 lastUpdatedAt 的记录
            List<TiktokAccount> updatedAccounts = remoteBTikTokAccountRepository.findByUpdatedAtAfterAndUpdatedAtIsNotNull(lastUpdatedAt);

            if (!updatedAccounts.isEmpty()) {
                localTikTokAccountRepository.saveAll(updatedAccounts);
                logger.info("已同步 {} 条 TikTokAccount 记录到本地。", updatedAccounts.size());
            } else {
                logger.info("没有 TikTokAccount 数据需要同步。");
            }
        } catch (Exception e) {
            logger.error("同步 TikTokAccount 数据时发生异常：", e);
        }
    }

    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)  // 每5分钟执行一次
    @Transactional(transactionManager = "localTransactionManager")
    public void syncTikTokVideoDetailsFromRemoteB() {
        logger.info("开始同步远程服务器 B 的 TikTokVideoDetails 数据到本地...");

        try {
            // 获取本地最新的 updatedAt
            LocalDateTime lastUpdatedAt = localTikTokVideoDetailsRepository.findMaxUpdateAt();
            if (lastUpdatedAt == null) {
                lastUpdatedAt = LocalDateTime.of(1970, 1, 1, 0, 0);
                logger.warn("本地 TikTokVideoDetails 的最新 updatedAt 为空，使用默认时间：{}", lastUpdatedAt);
            }

            // 从远程 B 获取 updatedAt 大于 lastUpdatedAt 的记录，且 updated_at 不为空
            List<TiktokVideoDetails> updatedVideoDetails = remoteBTikTokVideoDetailsRepository.findByUpdatedAtAfterAndUpdatedAtIsNotNull(lastUpdatedAt);

            if (!updatedVideoDetails.isEmpty()) {
                localTikTokVideoDetailsRepository.saveAll(updatedVideoDetails);
                logger.info("已同步 {} 条 TikTokVideoDetails 记录到本地。", updatedVideoDetails.size());
            } else {
                logger.info("没有 TikTokVideoDetails 数据需要同步。");
            }
        } catch (Exception e) {
            logger.error("同步 TikTokVideoDetails 数据时发生异常：", e);
        }
    }
}