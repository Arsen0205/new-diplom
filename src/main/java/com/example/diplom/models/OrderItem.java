package com.example.diplom.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Заказ

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Продукт

    @Column(name = "quantity", nullable = false)
    private int quantity; // Количество

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice; // Себестоимость единицы

    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice; // Цена продажи единицы

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost; // Общая себестоимость (costPrice * quantity)

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice; // Общая цена продажи (sellingPrice * quantity)
}
