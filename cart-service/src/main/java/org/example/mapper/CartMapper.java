package org.example.mapper;

import org.example.dto.CartDTO;
import org.example.model.Cart;
import org.example.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "items", source = "itemDTOs")
    @Mapping(target = "totalAmount", source = "totalAmount")
    CartDTO toCartDTO(Cart cart, List<CartDTO.CartItemDTO> itemDTOs, BigDecimal totalAmount);

    CartDTO.CartItemDTO toCartItemDTO(CartItem item, String productName, String imageUrl);
}