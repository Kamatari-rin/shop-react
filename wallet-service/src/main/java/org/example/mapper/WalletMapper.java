package org.example.mapper;

import org.example.dto.WalletDTO;
import org.example.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    WalletDTO toDto(Wallet wallet);
}