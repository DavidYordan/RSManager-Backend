package com.rsmanager.service;

public interface TiktokAccountService {

    /**
     * 检查用户是否存在
     *
     * @param tiktokAccount
     * @return
     */
    boolean userExists(String tiktokAccount);

}
