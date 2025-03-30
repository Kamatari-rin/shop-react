package org.example.mapper;

import org.example.dto.ProductDetailDTO;
import org.example.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryName", expression = "java(product.getCategoryId() != null ? categoryRepository.findById(product.getCategoryId()).map(org.example.model.Category::getName).orElse(null) : null)")
    ProductDetailDTO toDto(Product product, @org.mapstruct.Context org.example.repository.CategoryRepository categoryRepository);
}