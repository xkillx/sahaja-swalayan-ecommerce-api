package com.sahaja.swalayan.ecommerce.application.mapper;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import java.util.List;
import com.sahaja.swalayan.ecommerce.domain.model.product.Price;
import com.sahaja.swalayan.ecommerce.domain.model.product.Stock;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "price", target = "price", qualifiedByName = "priceToBigDecimal")
    @Mapping(source = "stock", target = "stock", qualifiedByName = "stockToInteger")
    ProductDTO toDto(Product product);

    @Mapping(source = "price", target = "price", qualifiedByName = "bigDecimalToPrice")
    @Mapping(source = "stock", target = "stock", qualifiedByName = "integerToStock")
    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDtoList(List<Product> products);

    @Named("priceToBigDecimal")
    default BigDecimal priceToBigDecimal(Price price) {
        return price != null ? price.getValue() : null;
    }

    @Named("bigDecimalToPrice")
    default Price bigDecimalToPrice(BigDecimal value) {
        return value != null ? new Price(value) : null;
    }

    @Named("stockToInteger")
    default Integer stockToInteger(Stock stock) {
        return stock != null ? stock.getValue() : null;
    }

    @Named("integerToStock")
    default Stock integerToStock(Integer value) {
        return value != null ? new Stock(value) : null;
    }
}
