package org.example.mapper;

import org.example.dto.CartDTO;
import org.example.model.Cart;
import org.example.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "id", source = "cart.id", qualifiedByName = "uuidToString")
    @Mapping(target = "userId", source = "cart.userId")
    @Mapping(target = "items", source = "itemDTOs")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "createdAt", source = "cart.createdAt")
    CartDTO toCartDTO(Cart cart, List<CartDTO.CartItemDTO> itemDTOs, BigDecimal totalAmount);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "productId", source = "item.productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "priceAtTime", source = "item.priceAtTime")
    @Mapping(target = "quantity", source = "item.quantity")
    @Mapping(target = "imageUrl", source = "imageUrl")
    CartDTO.CartItemDTO toCartItemDTO(CartItem item, String productName, String imageUrl);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}