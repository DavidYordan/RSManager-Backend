package com.rsmanager.service;

public interface TiktokService {

    /**
     * 检查用户是否存在
     *
     * @param tiktokAccount
     * @return
     */
    String userExists(String tiktokAccount);

}
