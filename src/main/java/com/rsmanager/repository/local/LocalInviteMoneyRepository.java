package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.InviteMoney;

@Repository
public interface LocalInviteMoneyRepository extends JpaRepository<InviteMoney, Long> {
}
