package org.example.mapper;

import org.example.dto.OrderDetailDTO;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDetailDTO toDto(Order order);

    @Mapping(target = "productName", ignore = true)
    OrderDetailDTO.OrderItemDTO toItemDto(OrderItem item);

    default OrderDetailDTO mapOrder(Order order) {
        if (order == null) return null;
        return toDto(order);
    }
}