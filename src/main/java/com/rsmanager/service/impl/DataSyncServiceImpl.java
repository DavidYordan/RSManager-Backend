package com.rsmanager.service.impl;

import com.rsmanager.model.*;
import com.rsmanager.repository.local.*;
import com.rsmanager.repository.remote.*;
import com.rsmanager.repository.remoteB.RemoteBTikTokRelationshipRepository;
import com.rsmanager.repository.remoteB.RemoteBTikTokVideoDetailsRepository;
import com.rsmanager.repository.remoteB.RemoteBTiktokAccountRepository;
import com.rsmanager.service.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    @Value("${remote.sync.batch.size}")
    private int batchSize;

    @Value("${remote.sync.tiktokRelationship}")
    private boolean canSyncTikTokRelationship;

    // 本地 Repository 接口
    @Autowired
    private LocalTbUserRemoteRepository localTbUserRemoteRepository;

    @Autowired
    private LocalUserIntegralRemoteRepository localUserIntegralRemoteRepository;

    @Autowired
    private LocalUserMoneyRemoteRepository localUserMoneyRemoteRepository;

    @Autowired
    private LocalUserIntegralDetailsRemoteRepository localUserIntegralDetailsRemoteRepository;

    @Autowired
    private LocalUserMoneyDetailsRemoteRepository localUserMoneyDetailsRemoteRepository;

    @Autowired
    private LocalInviteRemoteRepository localInviteRemoteRepository;

    @Autowired
    private LocalInviteMoneyRemoteRepository localInviteMoneyRemoteRepository;

    @Autowired
    private LocalAgentMoneyRepository localAgentMoneyRepository;

    @Autowired
    private LocalAgentWidthdrawRepository localAgentWidthdrawRepository;

    @Autowired
    private LocalCashOutRemoteRepository localCashOutRemoteRepository;

    @Autowired
    private LocalSysUserRepository localSysUserRepository;

    @Autowired
    private TikTokRelationshipRemoteRepository tikTokRelationshipRemoteRepository;

    @Autowired
    private TiktokUserDetailsRemoteRepository tikTokUserDetailsRemoteRepository;

    @Autowired
    private TikTokVideoDetailsRemoteRepository tikTokVideoDetailsRemoteRepository;

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

        List<Integer> userIds = new ArrayList<>();

        try {
            String lastUpdateTime = localTbUserRemoteRepository.findMaxUpdateTime();
            if (lastUpdateTime == null || lastUpdateTime.isEmpty()) {
                lastUpdateTime = "1970-01-01 00:00:00";
                logger.warn("TbUser 本地数据库的最新更新时间为空，设置为默认值: {}", lastUpdateTime);
            } else {
                logger.debug("TbUser 本地数据库的最新更新时间: {}", lastUpdateTime);
            }

            int page = 0;
            List<TbUserRemote> updatedRemoteTbUsers;

            do {
                updatedRemoteTbUsers = remoteTbUserRepository.findByUpdateTimeAfter(lastUpdateTime, PageRequest.of(page, batchSize));
                if (!updatedRemoteTbUsers.isEmpty()) {
                    localTbUserRemoteRepository.saveAll(updatedRemoteTbUsers);
                    logger.info("已保存第 {} 页 {} 条 TbUser 更新记录到本地数据库", page + 1, updatedRemoteTbUsers.size());

                    List<Integer> batchUserIds = updatedRemoteTbUsers.stream()
                            .map(TbUserRemote::getUserId)
                            .filter(Objects::nonNull)
                            .map(Long::intValue)
                            .collect(Collectors.toList());

                    userIds.addAll(batchUserIds);
                    logger.info("同步 TbUser 后提取到的 user_id 数量: {}", batchUserIds.size());
                    page++;
                }
            } while (updatedRemoteTbUsers.size() == batchSize);

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
            Long lastUserId = localSysUserRepository.findMaxUserId();
            if (lastUserId == null) {
                lastUserId = 0L;
                logger.warn("SysUser 本地数据库的最大 userId 为空，设置为默认值: {}", lastUserId);
            } else {
                logger.debug("SysUser 本地数据库的最大 userId: {}", lastUserId);
            }

            int page = 0;
            List<SysUser> updatedRemoteSysUsers;

            do {
                updatedRemoteSysUsers = remoteSysUserRepository.findByUserIdGreaterThan(lastUserId, PageRequest.of(page, batchSize));
                if (!updatedRemoteSysUsers.isEmpty()) {
                    localSysUserRepository.saveAll(updatedRemoteSysUsers);
                    logger.info("已保存第 {} 页 {} 条 SysUser 更新记录到本地数据库", page + 1, updatedRemoteSysUsers.size());
                    page++;
                }
            } while (updatedRemoteSysUsers.size() == batchSize);

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
            int page = 0;
            List<UserIntegralRemote> remoteUserIntegrals;

            do {
                // 假设 remoteUserIntegralRepository 支持分页查询
                remoteUserIntegrals = remoteUserIntegralRepository.findByUserIdIn(userIds, PageRequest.of(page, batchSize));
                if (!remoteUserIntegrals.isEmpty()) {
                    localUserIntegralRemoteRepository.saveAll(remoteUserIntegrals);
                    logger.info("已保存第 {} 页 {} 条 UserIntegral 记录到本地数据库", page + 1, remoteUserIntegrals.size());
                    page++;
                }
            } while (remoteUserIntegrals.size() == batchSize);

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
            int page = 0;
            List<UserMoneyRemote> remoteUserMoneys;

            do {
                // 假设 remoteUserMoneyRepository 支持分页查询
                remoteUserMoneys = remoteUserMoneyRepository.findByUserIdIn(userIds, PageRequest.of(page, batchSize));
                if (!remoteUserMoneys.isEmpty()) {
                    localUserMoneyRemoteRepository.saveAll(remoteUserMoneys);
                    logger.info("已保存第 {} 页 {} 条 UserMoney 记录到本地数据库", page + 1, remoteUserMoneys.size());
                    page++;
                }
            } while (remoteUserMoneys.size() == batchSize);

        } catch (Exception e) {
            logger.error("同步 UserMoney 数据时发生异常: ", e);
        }
    }

    /**
     * 同步 UserIntegralDetails 数据
     */
    @Override
    public void syncUserIntegralDetails() {
        logger.info("开始同步 UserIntegralDetails 数据...");
    
        List<Integer> userIds = new ArrayList<>();
    
        try {
            Integer lastId = localUserIntegralDetailsRemoteRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;
                logger.warn("UserIntegralDetails 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("UserIntegralDetails 本地数据库的最大 id: {}", lastId);
            }
    
            int page = 0;
            List<UserIntegralDetailsRemote> updatedRemoteDetails;
    
            do {
                updatedRemoteDetails = remoteUserIntegralDetailsRepository.findByIdGreaterThan(lastId, PageRequest.of(page, batchSize));
                if (!updatedRemoteDetails.isEmpty()) {
                    localUserIntegralDetailsRemoteRepository.saveAll(updatedRemoteDetails);
                    logger.info("已保存第 {} 页 {} 条 UserIntegralDetails 更新记录到本地数据库", page + 1, updatedRemoteDetails.size());
    
                    List<Integer> batchUserIds = updatedRemoteDetails.stream()
                            .map(UserIntegralDetailsRemote::getUserId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    
                    userIds.addAll(batchUserIds);
                    logger.info("同步 UserIntegralDetails 后提取到的 user_id 数量: {}", batchUserIds.size());
                    page++;
                }
            } while (updatedRemoteDetails.size() == batchSize);
    
        } catch (Exception e) {
            logger.error("同步 UserIntegralDetails 数据时发生异常: ", e);
        }
        
        if (!userIds.isEmpty()) {
            syncUserIntegral(userIds);
        }
    }

    /**
     * 同步 UserMoneyDetails 数据
     */
    @Override
    public void syncUserMoneyDetails() {
        logger.info("开始同步 UserMoneyDetails 数据...");
    
        Set<Integer> userIds = new HashSet<>();
    
        try {
            Integer lastId = localUserMoneyDetailsRemoteRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;
                logger.warn("UserMoneyDetails 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("UserMoneyDetails 本地数据库的最大 id: {}", lastId);
            }
    
            int page = 0;
            List<UserMoneyDetailsRemote> updatedRemoteDetails;
    
            do {
                updatedRemoteDetails = remoteUserMoneyDetailsRepository.findByIdGreaterThan(lastId, PageRequest.of(page, batchSize));
                if (!updatedRemoteDetails.isEmpty()) {
                    localUserMoneyDetailsRemoteRepository.saveAll(updatedRemoteDetails);
                    logger.info("已保存第 {} 页 {} 条 UserMoneyDetails 更新记录到本地数据库", page + 1, updatedRemoteDetails.size());
    
                    List<Integer> batchUserIds = updatedRemoteDetails.stream()
                            .map(UserMoneyDetailsRemote::getUserId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    
                    userIds.addAll(batchUserIds);
                    logger.info("同步 UserMoneyDetails 后提取到的 user_id 数量: {}", batchUserIds.size());
                    page++;
                }
            } while (updatedRemoteDetails.size() == batchSize);
    
        } catch (Exception e) {
            logger.error("同步 UserMoneyDetails 数据时发生异常: ", e);
        }
    
        if (!userIds.isEmpty()) {
            syncUserMoney(new ArrayList<>(userIds));
        }
    }

    /**
     * 同步 Invite 数据
     */
    @Override
    public void syncInvite() {
        logger.info("开始同步 Invite 数据...");
    
        Set<Integer> userIds = new HashSet<>();
    
        try {
            Integer lastId = localInviteRemoteRepository.findMaxId();
            if (lastId == null) {
                lastId = 0;
                logger.warn("Invite 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("Invite 本地数据库的最大 id: {}", lastId);
            }
    
            int page = 0;
            List<InviteRemote> updatedRemoteInvites;
    
            do {
                updatedRemoteInvites = remoteInviteRepository.findByIdGreaterThan(lastId, PageRequest.of(page, batchSize));
                if (!updatedRemoteInvites.isEmpty()) {
                    localInviteRemoteRepository.saveAll(updatedRemoteInvites);
                    logger.info("已保存第 {} 页 {} 条 Invite 更新记录到本地数据库", page + 1, updatedRemoteInvites.size());
    
                    List<Integer> batchUserIds = updatedRemoteInvites.stream()
                            .map(InviteRemote::getUserId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    
                    Set<Integer> inviteeUserIds = updatedRemoteInvites.stream()
                            .map(InviteRemote::getInviteeUserId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
    
                    userIds.addAll(batchUserIds);
                    userIds.addAll(inviteeUserIds);
    
                    logger.info("同步 Invite 后提取到的 user_id 数量: {}", batchUserIds.size() + inviteeUserIds.size());
                    page++;
                }
            } while (updatedRemoteInvites.size() == batchSize);
    
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
            int page = 0;
            List<InviteMoneyRemote> remoteInviteMoneys;

            do {
                // 假设 remoteInviteMoneyRepository 支持分页查询
                remoteInviteMoneys = remoteInviteMoneyRepository.findByUserIdIn(userIds, PageRequest.of(page, batchSize));
                if (!remoteInviteMoneys.isEmpty()) {
                    localInviteMoneyRemoteRepository.saveAll(remoteInviteMoneys);
                    logger.info("已保存第 {} 页 {} 条 InviteMoney 记录到本地数据库", page + 1, remoteInviteMoneys.size());
                    page++;
                }
            } while (remoteInviteMoneys.size() == batchSize);

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
            Long lastId = localAgentMoneyRepository.findMaxId();
            if (lastId == null) {
                lastId = 0L;
                logger.warn("AgentMoney 本地数据库的最大 id 为空，设置为默认值: {}", lastId);
            } else {
                logger.debug("AgentMoney 本地数据库的最大 id: {}", lastId);
            }
    
            int page = 0;
            List<AgentMoney> remoteAgentMoneys;
    
            do {
                remoteAgentMoneys = remoteAgentMoneyRepository.findByIdGreaterThan(lastId, PageRequest.of(page, batchSize));
                if (!remoteAgentMoneys.isEmpty()) {
                    localAgentMoneyRepository.saveAll(remoteAgentMoneys);
                    logger.info("已保存第 {} 页 {} 条 AgentMoney 记录到本地数据库", page + 1, remoteAgentMoneys.size());
                    page++;
                }
            } while (remoteAgentMoneys.size() == batchSize);
    
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
            Instant lastUpdateTime = localAgentWidthdrawRepository.findMaxUpdateTime();
            if (lastUpdateTime == null) {
                lastUpdateTime = Instant.EPOCH;
                logger.warn("AgentWidthdraw 本地数据库的最新 updateTime 为空，设置为默认值: {}", lastUpdateTime);
            } else {
                logger.debug("AgentWidthdraw 本地数据库的最新 updateTime: {}", lastUpdateTime);
            }

            int page = 0;
            List<AgentWidthdraw> remoteAgentWidthdraws;

            do {
                remoteAgentWidthdraws = remoteAgentWidthdrawRepository.findByUpdateTimeAfter(lastUpdateTime, PageRequest.of(page, batchSize));
                if (!remoteAgentWidthdraws.isEmpty()) {
                    localAgentWidthdrawRepository.saveAll(remoteAgentWidthdraws);
                    logger.info("已保存第 {} 页 {} 条 AgentWidthdraw 记录到本地数据库", page + 1, remoteAgentWidthdraws.size());
                    page++;
                }
            } while (remoteAgentWidthdraws.size() == batchSize);

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
            String lastCreateAt = localCashOutRemoteRepository.findMaxCreateAt();
            String lastOutAt = localCashOutRemoteRepository.findMaxOutAt();
    
            if (lastCreateAt == null || lastCreateAt.isEmpty()) {
                lastCreateAt = "1970-01-01 00:00:00";
                logger.warn("CashOut 本地数据库的最新 createAt 为空，设置为默认值: {}", lastCreateAt);
            } else {
                logger.debug("CashOut 本地数据库的最新 createAt: {}", lastCreateAt);
            }
    
            if (lastOutAt == null || lastOutAt.isEmpty()) {
                lastOutAt = "1970-01-01 00:00:00";
                logger.warn("CashOut 本地数据库的最新 outAt 为空，设置为默认值: {}", lastOutAt);
            } else {
                logger.debug("CashOut 本地数据库的最新 outAt: {}", lastOutAt);
            }
    
            int page = 0;
            List<CashOutRemote> remoteCashOutList;

            Set<CashOutRemote> remoteCashOutSet = new HashSet<>();

            do {
                List<CashOutRemote> remoteCashOutByCreateAt = remoteCashOutRepository.findByCreateAtAfter(lastCreateAt, PageRequest.of(page, batchSize));
                List<CashOutRemote> remoteCashOutByOutAt = remoteCashOutRepository.findByOutAtAfter(lastOutAt, PageRequest.of(page, batchSize));
                
                remoteCashOutSet.clear();
                remoteCashOutSet.addAll(remoteCashOutByCreateAt);
                remoteCashOutSet.addAll(remoteCashOutByOutAt);
                
                remoteCashOutList = new ArrayList<>(remoteCashOutSet);

                if (!remoteCashOutList.isEmpty()) {
                    localCashOutRemoteRepository.saveAll(remoteCashOutList);
                    logger.info("已保存第 {} 页 {} 条 CashOut 记录到本地数据库", page + 1, remoteCashOutList.size());
                    page++;
                }
            } while (remoteCashOutList.size() == batchSize);
    
            return true;
            
        } catch (Exception e) {
            logger.error("同步 CashOut 数据时发生异常: ", e);
            return false;
        }
    }

    /**
     * 将本地 TikTokRelationship 数据同步到远程服务器 B
     */
    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(transactionManager = "remoteBTransactionManager")
    public void syncTikTokRelationshipToRemoteB() {
        if (!canSyncTikTokRelationship) {
            logger.info("不需要将本地 TikTokRelationship 数据同步到远程服务器 B。");
            return;
        }

        logger.info("开始将本地 TikTokRelationship 数据同步到远程服务器 B...");

        try {
            Instant now = Instant.now();
            List<TiktokRelationshipRemote> relationships = tikTokRelationshipRemoteRepository.findAll();

            if (!relationships.isEmpty()) {
                List<TiktokRelationshipRemote> remoteRelationships = relationships.stream()
                        .map(rel -> TiktokRelationshipRemote.builder()
                                .recordId(rel.getRecordId())
                                .tiktokAccount(rel.getTiktokAccount())
                                .startDate(rel.getStartDate())
                                .endDate(rel.getEndDate())
                                .status(rel.getStatus())
                                .updatedAt(now)
                                .build())
                        .collect(Collectors.toList());

                // 按批次保存
                int page = 0;
                List<TiktokRelationshipRemote> batchRelationships;

                do {
                    int fromIndex = page * batchSize;
                    int toIndex = Math.min(fromIndex + batchSize, remoteRelationships.size());
                    if (fromIndex >= toIndex) break;

                    batchRelationships = remoteRelationships.subList(fromIndex, toIndex);
                    remoteBTikTokRelationshipRepository.saveAll(batchRelationships);
                    logger.info("已将第 {} 批 {} 条 TikTokRelationship 记录同步到远程服务器 B。", page + 1, batchRelationships.size());
                    page++;
                } while (batchRelationships.size() == batchSize);

            } else {
                logger.info("本地没有 TikTokRelationship 数据需要同步。");
            }
        } catch (Exception e) {
            logger.error("同步 TikTokRelationship 数据到远程服务器 B 时发生异常：", e);
        }
    }

    /**
     * 同步远程服务器 B 的 TikTokAccount 数据到本地
     */
    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(transactionManager = "localTransactionManager")
    public void syncTikTokAccountFromRemoteB() {
        logger.info("开始同步远程服务器 B 的 TikTokAccount 数据到本地...");

        try {
            Instant lastUpdatedAt = tikTokUserDetailsRemoteRepository.findMaxUpdateAt();
            if (lastUpdatedAt == null) {
                lastUpdatedAt = Instant.EPOCH;
                logger.warn("本地 TikTokAccount 的最新 updatedAt 为空，使用默认时间：{}", lastUpdatedAt);
            }

            int page = 0;
            List<TiktokUserDetailsRemote> updatedAccounts;

            do {
                updatedAccounts = remoteBTikTokAccountRepository.findByUpdatedAtAfterAndUpdatedAtIsNotNull(lastUpdatedAt, PageRequest.of(page, batchSize));

                if (!updatedAccounts.isEmpty()) {
                    tikTokUserDetailsRemoteRepository.saveAll(updatedAccounts);
                    logger.info("已同步第 {} 页 {} 条 TikTokAccount 记录到本地。", page + 1, updatedAccounts.size());
                    page++;
                }
            } while (updatedAccounts.size() == batchSize);

            logger.info("远程 TikTokAccount 同步完成。");
        } catch (Exception e) {
            logger.error("同步 TikTokAccount 数据时发生异常：", e);
        }
    }

    /**
     * 同步远程服务器 B 的 TikTokVideoDetails 数据到本地
     */
    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(transactionManager = "localTransactionManager")
    public void syncTikTokVideoDetailsFromRemoteB() {
        logger.info("开始同步远程服务器 B 的 TikTokVideoDetails 数据到本地...");

        try {
            Instant lastUpdatedAt = tikTokVideoDetailsRemoteRepository.findMaxUpdateAt();
            if (lastUpdatedAt == null) {
                lastUpdatedAt = Instant.EPOCH;
                logger.warn("本地 TikTokVideoDetails 的最新 updatedAt 为空，使用默认时间：{}", lastUpdatedAt);
            }

            int page = 0;
            List<TiktokVideoDetailsRemote> updatedVideoDetails;

            do {
                updatedVideoDetails = remoteBTikTokVideoDetailsRepository.findByUpdatedAtAfterAndUpdatedAtIsNotNull(
                        lastUpdatedAt, PageRequest.of(page, batchSize));

                if (!updatedVideoDetails.isEmpty()) {
                    tikTokVideoDetailsRemoteRepository.saveAll(updatedVideoDetails);
                    logger.info("已同步第 {} 页 {} 条 TikTokVideoDetails 记录到本地。", page + 1, updatedVideoDetails.size());
                    page++;
                }
            } while (updatedVideoDetails.size() == batchSize);

            logger.info("远程 TikTokVideoDetails 同步完成。");
        } catch (Exception e) {
            logger.error("同步 TikTokVideoDetails 数据时发生异常：", e);
        }
    }
}
