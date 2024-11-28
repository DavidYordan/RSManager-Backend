package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.BackendUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BackendUserRepository extends JpaRepository<BackendUser, Long>, JpaSpecificationExecutor<BackendUser>, BackendUserRepositoryCustom {
    
    // 根据 username 查找用户
    // @Query("SELECT u FROM BackendUser u WHERE BINARY(u.username) = :username")
    Optional<BackendUser> findByUsername(String username);

    // 根据 fullname 查找用户列表
    Optional<BackendUser> findByFullname(String fullname);

    // 根据 managerId 查找被管理用户id列表
    @Query("SELECT r.user.userId FROM ManagerRelationship r WHERE r.manager.userId = :managerId AND r.status = true")
    List<Long> findUserIdsByManagerId(@Param("managerId") Long managerId);

    // 根据 managerIds 查找被管理用户id列表
    @Query("SELECT r.user.userId FROM ManagerRelationship r WHERE r.manager.userId IN :managerIds AND r.status = true")
    Set<Long> findUserIdsByManagerIds(@Param("managerIds") Set<Long> managerIds);

    @Query("SELECT COUNT(r) FROM InviterRelationship r WHERE r.inviter.userId = :inviterId AND r.status = true")
    Long countByInviterId(Long inviterId);
}
