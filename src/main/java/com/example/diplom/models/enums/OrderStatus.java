package com.example.diplom.models.enums;

public enum OrderStatus {
    PENDING,    // Ожидает подтверждения
    CONFIRMED,  // Подтвержден поставщиком
    SHIPPED,    // В пути
    DELIVERED,  // Доставлен
    CANCELLED   // Отменен
}
