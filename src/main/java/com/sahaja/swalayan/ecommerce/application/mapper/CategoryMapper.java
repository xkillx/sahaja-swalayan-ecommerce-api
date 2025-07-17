package com.sahaja.swalayan.ecommerce.application.mapper;

import java.util.List;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);
    Category toEntity(CategoryDTO dto);

    List<CategoryDTO> toDTOList(List<Category> categories);
    List<Category> toEntityList(List<CategoryDTO> dtos);
}
