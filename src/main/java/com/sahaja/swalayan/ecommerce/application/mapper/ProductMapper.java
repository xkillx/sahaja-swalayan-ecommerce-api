package com.sahaja.swalayan.ecommerce.application.mapper;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.List;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "imageUrl", target = "imageUrl")
    ProductDTO toDto(Product product);

    @Mapping(source = "categoryId", target = "category", qualifiedByName = "uuidToCategory")
    @Mapping(source = "imageUrl", target = "imageUrl")
    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDtoList(List<Product> products);

    @Named("uuidToCategory")
    default Category uuidToCategory(UUID id) {
        return id != null ? new Category(id, null, null) : null;
    }

    @Named("categoryToUuid")
    default UUID categoryToUuid(Category category) {
        return category != null ? category.getId() : null;
    }
}
