package com.rsmanager.repository.local;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.Region;


@Repository
public interface RegionRepository extends JpaRepository<Region, Integer>, JpaSpecificationExecutor<Region> {
    
    // 通过 regionName 查询
    List<Region> findByRegionName(String regionName);
    
    // 通过 regionCode 查询
    List<Region> findByRegionCode(Integer regionCode);

    // 通过 regionId 查询
    Optional<Region> findByRegionId(Integer regionId);
}