package com.rsmanager.service;

import com.rsmanager.model.TbUser;
import com.rsmanager.repository.local.LocalTbUserRepository;
import com.rsmanager.dto.tbuser.TbuserInfoDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TbUserService {

    private final LocalTbUserRepository tbUserRepository;

    // 构造器注入
    public TbUserService(LocalTbUserRepository tbUserRepository) {
        this.tbUserRepository = tbUserRepository;
    }

    /**
     * 根据手机号查找用户ID
     *
     * @param phone 用户手机号
     * @return Optional 包装的用户ID
     */
    public Long findUserIdByPhone(String phone) {
        return tbUserRepository.findByPhone(phone)
                .map(TbUser::getUserId)
                .orElse(null);
    }

    /**
     * 根据用户ID查找用户邀请码
     *
     * @param userId 用户ID
     * @return 用户邀请码
     */
    public String findInvitationCodeByUserId(Long userId) {
        return tbUserRepository.findInvitationCodeByUserId(userId);
    }

    /**
     * 根据邀请码查找用户ID
     *
     * @param invitationCode 用户邀请码
     * @return 用户ID
     */
    public Long findUserIdByInvitationCode(String invitationCode) {
        return tbUserRepository.findUserIdByInvitationCode(invitationCode);
    }

    /**
     * 根据用户ID列表获取用户信息列表
     *
     * @param userIds 用户ID列表
     * @return 用户信息DTO列表
     */
    public List<TbuserInfoDTO> getUserInfoListByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询数据库中符合条件的用户
        List<TbUser> userList = tbUserRepository.findByUserIdIn(userIds);

        // 将 TbUser 转换为 UserInfoDTO
        return userList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据inviterCode查找用户
     *
     * @param userId 用户ID
     * @return List
     */
    public List<TbUser> findByInviterCode(String inviterCode) {
        List<TbUser> userList = tbUserRepository.findByInviterCode(inviterCode);
        return userList;
    }

    /**
     * 将 TbUser 转换为 UserInfoDTO
     *
     * @param user TbUser 实例
     * @return UserInfoDTO 实例
     */
    private TbuserInfoDTO convertToDTO(TbUser user) {
        return TbuserInfoDTO.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .phone(user.getPhone())
                .password(user.getPassword())
                .created_time(user.getCreateTime())
                .update_time(user.getUpdateTime())
                .sys_phone(user.getSysPhone())
                .status(user.getStatus())
                .platform(user.getPlatform())
                .invitation_code(user.getInvitationCode())
                .invitation_type(user.getInviterType())
                .inviter_code(user.getInviterCode())
                .inviter_type(user.getInviterType())
                .inviter_custom_id(user.getInviterCustomId())
                .inviter_url(user.getInviterUrl())
                .clientid(user.getClientId())
                .on_line_time(user.getOnLineTime())
                .recipient(user.getRecipient())
                .bank_number(user.getBankNumber())
                .bank_name(user.getBankName())
                .bank_address(user.getBankAddress())
                .bank_code(user.getBankCode())
                .agent0_money(user.getAgent0Money())
                .agent1_money(user.getAgent1Money())
                .agent0_money_delete(user.getAgent0MoneyDelete())
                .agent1_money_delete(user.getAgent1MoneyDelete())
                .first_name(user.getFirstName())
                .last_name(user.getLastName())
                .email(user.getEmail())
                .area_code(user.getAreaCode())
                .fake(user.getFake())
                .build();

    }
}
