package com.sahaja.swalayan.ecommerce.domain.model.product;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StockConverter implements AttributeConverter<Stock, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Stock stock) {
        return stock != null ? stock.getValue() : null;
    }

    @Override
    public Stock convertToEntityAttribute(Integer dbData) {
        return dbData != null ? new Stock(dbData) : null;
    }
}
