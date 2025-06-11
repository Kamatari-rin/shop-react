package org.example.mapper;

import org.example.dto.PurchaseResponseDTO;
import org.example.model.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {
    @Mapping(source = "purchase.orderId", target = "orderId")
    @Mapping(source = "purchase.userId", target = "userId")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "purchase.paymentStatus", target = "paymentStatus")
    @Mapping(source = "purchase.transactionDate", target = "transactionDate")
    PurchaseResponseDTO toResponseDto(Purchase purchase, BigDecimal totalAmount);
}