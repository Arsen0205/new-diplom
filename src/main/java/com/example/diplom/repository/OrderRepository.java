package com.example.diplom.repository;

import com.example.diplom.models.Order;
import com.example.diplom.models.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Найти все заказы определенного поставщика
    List<Order> findBySupplierId(Long supplierId);

    // Найти все заказы с определенным статусом
    List<Order> findByStatus(OrderStatus status);
}
