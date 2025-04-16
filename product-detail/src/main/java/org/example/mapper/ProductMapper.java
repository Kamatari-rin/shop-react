package org.example.mapper;

import org.example.dto.ProductDetailDTO;
import org.example.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryName", ignore = true)
    ProductDetailDTO toDto(Product product);

    default ProductDetailDTO updateCategoryName(ProductDetailDTO dto, String categoryName) {
        return new ProductDetailDTO(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.price(),
                dto.imageUrl(),
                dto.categoryId(),
                categoryName,
                dto.createdAt(),
                dto.updatedAt()
        );
    }
}