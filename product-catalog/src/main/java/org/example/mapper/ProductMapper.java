package org.example.mapper;

import org.example.dto.ProductDTO;
import org.example.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);
}