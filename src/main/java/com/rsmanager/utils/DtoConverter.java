package com.rsmanager.utils;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.rsmanager.dto.api.PaginationDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    private final ModelMapper modelMapper;

    @Autowired
    public DtoConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * 将实体转换为DTO
     */
    public <D, E> D convertEntityToDto(E entity, Class<D> dtoClass) {
        if (entity == null) {
            return null;
        }
        return modelMapper.map(entity, dtoClass);
    }

    /**
     * 将DTO转换为实体
     */
    public <E, D> E convertDtoToEntity(D dto, Class<E> entityClass) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, entityClass);
    }

    /**
     * 将实体列表转换为DTO列表
     */
    public <D, E> List<D> convertEntityListToDtoList(List<E> entityList, Class<D> dtoClass) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                         .map(entity -> modelMapper.map(entity, dtoClass))
                         .collect(Collectors.toList());
    }

    /**
     * 将DTO列表转换为实体列表
     */
    public <E, D> List<E> convertDtoListToEntityList(List<D> dtoList, Class<E> entityClass) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                      .map(dto -> modelMapper.map(dto, entityClass))
                      .collect(Collectors.toList());
    }

    /**
     * 复制DTO的属性到实体
     */
    public <D, E> void copyProperties(D sourceDto, E destinationEntity) {
        if (sourceDto == null || destinationEntity == null) {
            return;
        }
        modelMapper.map(sourceDto, destinationEntity);
    }

    /**
     * 将Page对象转换为PaginationDTO
     */
    public PaginationDTO convertPageToPaginationDto(Page<?> page) {
        if (page == null) {
            return null;
        }
        return modelMapper.map(page, PaginationDTO.class);
    }

    /**
     * 将一个DTO转换为另一个DTO
     */
    public <D, S> D convertDtoToDto(S sourceDto, Class<D> dtoClass) {
        if (sourceDto == null) {
            return null;
        }
        return modelMapper.map(sourceDto, dtoClass);
    }

    /**
     * 将DTO列表转换为另一个DTO列表
     */
    public <D, S> List<D> convertDtoListToDtoList(List<S> sourceList, Class<D> dtoClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                         .map(source -> modelMapper.map(source, dtoClass))
                         .collect(Collectors.toList());
    }
}
