package org.example.mapper;

import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemDTO;
import org.example.dto.OrderListDTO;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "items", ignore = true)
    OrderDetailDTO toDto(Order order);

    @Mapping(target = "productName", ignore = true)
    OrderItemDTO toItemDto(OrderItem item);

    default OrderDetailDTO toDto(Order order, List<OrderItem> items) {
        if (order == null) return null;
        List<OrderItemDTO> itemDtos = items.stream()
                .map(this::toItemDto)
                .toList();
        OrderDetailDTO dto = toDto(order);
        return new OrderDetailDTO(
                dto.id(),
                dto.userId(),
                dto.orderDate(),
                dto.status(),
                dto.totalAmount(),
                itemDtos);
    }

    OrderListDTO.OrderDTO toListDto(Order order);
}