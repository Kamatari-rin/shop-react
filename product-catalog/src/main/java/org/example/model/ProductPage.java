package org.example.model;

import lombok.Value;
import org.example.dto.ProductDTO;

import java.util.List;

@Value
public class ProductPage {
    List<ProductDTO> content;
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPages;
}