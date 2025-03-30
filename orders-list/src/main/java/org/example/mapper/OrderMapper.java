package org.example.mapper;

import org.example.dto.OrderDTO;
import org.example.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toDto(Order order);
}