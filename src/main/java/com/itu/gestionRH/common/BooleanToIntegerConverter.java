package com.itu.gestionRH.common;

import jakarta.persistence.AttributeConverter;

/** Adapte les colonnes Oracle NUMBER(1) aux proprietes Java Boolean. */
public class BooleanToIntegerConverter implements AttributeConverter<Boolean, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        return value == null ? null : (value ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Integer value) {
        return value == null ? null : value != 0;
    }
}
