package com.example.diplom.dto.request;

import com.example.diplom.models.Supplier;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequestDto {
    private String title;
    private int quantity;
    private BigDecimal price;
    private BigDecimal sellingPrice;
    private String login;
}
