package com.rsmanager.service.impl;

import com.rsmanager.model.BackendUser;
import com.rsmanager.model.TiktokRelationship;
import com.rsmanager.repository.local.TikTokRelationshipRepository;
import com.rsmanager.service.TiktokService;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TiktokServiceImpl implements TiktokService {

    private final TikTokRelationshipRepository tikTokRelationshipRepository;

    /**
     * 检查用户是否存在
     *
     * @param tiktokAccount
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public String userExists(String tiktokAccount) {

        Optional<TiktokRelationship> tiktokOptional = tikTokRelationshipRepository.findByTiktokAccountAndStatus(tiktokAccount);

        if (tiktokOptional.isPresent()) {
            BackendUser backendUser = tiktokOptional.get().getUser();
            return "tiktok账户已被用户" + backendUser.getFullname() + "绑定";
        } else {
            return "success";
        }
    }
}
