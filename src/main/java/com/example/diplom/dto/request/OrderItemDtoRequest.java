package com.example.diplom.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDtoRequest {
    private Long productId;
    private int quantity;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
}

