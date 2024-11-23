package com.rsmanager.config;

import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import com.rsmanager.model.*;
import com.rsmanager.dto.api.PaginationDTO;
import com.rsmanager.dto.system.RolePermissionDTO;

@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // 全局配置
        modelMapper.getConfiguration()
                   .setMatchingStrategy(org.modelmapper.convention.MatchingStrategies.STRICT)
                   .setFieldMatchingEnabled(true)
                   .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                   .setSkipNullEnabled(true)
                   .setPropertyCondition(Conditions.isNotNull());
        
        // 配置 RolePermission -> RolePermissionDTO 映射
        TypeMap<RolePermission, RolePermissionDTO> rolePermissionMap = modelMapper.createTypeMap(RolePermission.class, RolePermissionDTO.class);
        rolePermissionMap.addMappings(mapper -> {
            mapper.map(src -> src.getId().getRoleId(), RolePermissionDTO::setRoleId);
            mapper.map(src -> src.getId().getPermissionId(), RolePermissionDTO::setPermissionName);
        });
        
        // 定义 Converter 将 Page 对象转换为 PaginationDTO
        Converter<Page<?>, PaginationDTO> pageToPaginationConverter = new Converter<Page<?>, PaginationDTO>() {
            @Override
            public PaginationDTO convert(MappingContext<Page<?>, PaginationDTO> context) {
                Page<?> source = context.getSource();
                if (source == null) {
                    return null;
                }
                return PaginationDTO.builder()
                        .totalItems(source.getTotalElements())
                        .totalPages(source.getTotalPages())
                        .currentPage(source.getNumber())
                        .pageSize(source.getSize())
                        .build();
            }
        };
        
        modelMapper.addConverter(pageToPaginationConverter);
        
        return modelMapper;
    }
}
