package com.rsmanager.service;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.rsmanager.dto.api.ServiceResponseDTO;
import com.rsmanager.dto.user.*;
import com.rsmanager.model.BackendUser;


public interface UserService {

    // /**
    //  * 创建新的用户
    //  *
    //  * @param request
    //  * @param authentication
    //  * @return
    //  */
    // Optional<BackendUserDTO> createUser(BackendUserDTO request, Authentication authentication);

    /**
     * 强制创建用户
     *
     * @param request
     * @return
     */
    ServiceResponseDTO superCreateUser(SuperCreateUserDTO request);

    /**
     * 查找用户
     * 
     * @param userId
     * @param username
     * @param fullname
     * @return
     */
    Optional<FindUserDTO> findUser(FindUserDTO request);

    /**
     * 检查用户是否存在
     *
     * @param username
     * @return
     */
    Boolean userExists(String username);

    /**
     * 根据用户名获取用户
     * 
     * @param username
     * @return
     */
    Long findIdByUsername(String username);

    // /**
    //  * 根据用户名获取用户
    //  *
    //  * @param request
    //  * @param authentication
    //  * @return
    //  */
    // Optional<SearchResponseDTO> findUserInfoByUsername(String username, Authentication authentication);

    /**
     * 根据 userId 获取用户详情
     *
     * @param request
     * @return
     */
    Optional<BackendUser> findByUserId(Long userId);

    // /**
    //  * 获取用户列表
    //  *
    //  * @param request
    //  * @param authentication
    //  * @return
    //  */
    // Page<SearchResponseDTO> getUserList(Pageable pageable, BackendUserSearchDTO request, Authentication authentication);

    /**
     * 重置用户密码，可以通过 userId 或 username 来进行查找
     *
     * @param request
     * @return
     */
    ServiceResponseDTO resetPassword(BackendUserResetPasswordDTO request);

    /**
     * 根据查询条件搜索用户，支持分页
     *
     * @param request
     * @return
     */
    Page<SearchUsersResponseDTO> searchUsers(SearchUsersDTO request);

    /**
     * 根据查询条件搜索用户，支持分页
     *
     * @param request
     * @return
     */
    Page<OldSearchUsersResponseDTO> oldSearchUsers(SearchUsersDTO request);

    /**
     * 更新用户信息
     *
     * @param request
     * @return Boolean
     */
    Boolean updateUser(BackendUserUpdateDTO request);

    // /**
    //  * 禁用用户
    //  *
    //  * @param request
    //  * @param authentication
    //  * @return
    //  */
    // ServiceResponseDTO disableUser(BackendUserDTO request, Authentication authentication);

    // /**
    //  * 启用用户
    //  *
    //  * @param request
    //  * @param authentication
    //  * @return
    //  */
    // ServiceResponseDTO enableUser(BackendUserDTO request, Authentication authentication);

    // /**
    //  * 检查用户是否存在
    //  *
    //  * @param username
    //  * @return
    //  */
    // boolean userExists(String username);

    /**
     * 获取用户的利润汇总
     *
     * @param username
     * @return 
     */
    OwnerSummaryDTO getOwnerSummary(YearMonth selectedMonth);

    /**
     * 获取用户的下属
     *
     * @param ids
     * @return
     */
    // List<Long> findSubordinates(List<Long> ids);

}
