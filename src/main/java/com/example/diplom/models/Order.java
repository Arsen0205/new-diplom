package com.example.diplom.models;

import com.example.diplom.models.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;// Итогова себестоимость

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice; // Итогова цена продажи

    @Column(name = "profit", nullable = false)
    private BigDecimal profit; //Прибыль

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
