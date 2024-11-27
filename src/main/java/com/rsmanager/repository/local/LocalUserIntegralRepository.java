package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserIntegral;

@Repository
public interface LocalUserIntegralRepository extends JpaRepository<UserIntegral, Integer> {

}
