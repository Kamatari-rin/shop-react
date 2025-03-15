package org.example.mapper;

import org.example.dto.CartDTO;
import org.example.dto.CartItemDTO;
import org.example.model.Cart;
import org.example.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "cart.userId")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(computeTotalAmount(items))")
    CartDTO toCartDTO(Cart cart, List<CartItemDTO> items);

    @Mapping(target = "productId", source = "item.productId")
    @Mapping(target = "priceAtTime", source = "item.priceAtTime")
    @Mapping(target = "quantity", source = "item.quantity")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "imageUrl", source = "imageUrl")
    CartItemDTO toCartItemDTO(CartItem item, String productName, String imageUrl);

    default java.math.BigDecimal computeTotalAmount(List<CartItemDTO> items) {
        return items.stream()
                .map(item -> item.priceAtTime().multiply(java.math.BigDecimal.valueOf(item.quantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}