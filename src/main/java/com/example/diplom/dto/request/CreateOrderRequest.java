package com.example.diplom.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long supplierId;
    private String city;
    private String address;
    private List<OrderItemDtoRequest> items;
}
