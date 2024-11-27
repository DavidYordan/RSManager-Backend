package com.rsmanager.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IntegerToLongConverter implements AttributeConverter<Integer, Long> {

    @Override
    public Long convertToDatabaseColumn(Integer attribute) {
        return attribute == null ? null : attribute.longValue();
    }

    @Override
    public Integer convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : dbData.intValue();
    }
}
