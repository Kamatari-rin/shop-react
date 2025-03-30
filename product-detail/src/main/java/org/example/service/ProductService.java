package org.example.service;

import org.example.dto.ProductDetailDTO;

public interface ProductService {
    ProductDetailDTO getProductById(Integer id);
}