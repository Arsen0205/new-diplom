package com.example.diplom.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long supplierId;
    private List<OrderItemDtoRequest> items;
}
