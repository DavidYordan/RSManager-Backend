package com.rsmanager.service.impl;

import com.rsmanager.repository.local.TiktokAccountRepository;
import com.rsmanager.service.TiktokAccountService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TiktokAccountServiceImpl implements TiktokAccountService {

    private final TiktokAccountRepository tiktokAccountRepository;


    /**
     * 检查用户是否存在
     *
     * @param tiktokAccount
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public boolean userExists(String tiktokAccount) {
        return tiktokAccountRepository.findByTiktokAccount(tiktokAccount).isPresent();
    }
}
