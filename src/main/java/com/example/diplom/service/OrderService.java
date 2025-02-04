package com.example.diplom.service;

import com.example.diplom.dto.request.CreateOrderRequest;
import com.example.diplom.models.Order;
import com.example.diplom.models.OrderItem;
import com.example.diplom.models.Product;
import com.example.diplom.models.Supplier;
import com.example.diplom.models.enums.OrderStatus;
import com.example.diplom.repository.OrderItemRepository;
import com.example.diplom.repository.OrderRepository;
import com.example.diplom.repository.ProductRepository;
import com.example.diplom.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final OrderItemRepository orderItemRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }


    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Поставщик не найден"));

        // Создаем заказ без позиций
        Order newOrder = Order.builder()
                .supplier(supplier)
                .totalCost(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .profit(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>()) // Инициализация списка
                .build();

        // Сохраняем заказ, чтобы получить его ID
        Order savedOrder = orderRepository.save(newOrder);

        // Создаем позиции заказа
        List<OrderItem> orderItems = request.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Продукт не найден"));

            //BigDecimal totalCost = itemDto.getCostPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            //BigDecimal totalPrice = itemDto.getSellingPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            BigDecimal totalPrice = product.getSellingPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            return OrderItem.builder()
                    .order(savedOrder) // Указываем сохраненный Order
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .costPrice(itemDto.getCostPrice())
                    .sellingPrice(itemDto.getSellingPrice())
                    .totalCost(totalCost)
                    .totalPrice(totalPrice)
                    .build();
        }).collect(Collectors.toList());

        // Устанавливаем позиции в заказ
        savedOrder.setOrderItems(orderItems);

        // Сохраняем позиции заказа
        orderItemRepository.saveAll(orderItems);

        // Обновляем итоговые суммы заказа
        BigDecimal totalCost = orderItems.stream().map(OrderItem::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = orderItems.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = totalPrice.subtract(totalCost);

        savedOrder.setTotalCost(totalCost);
        savedOrder.setTotalPrice(totalPrice);
        savedOrder.setProfit(profit);

        // Отправка уведомления
        telegramNotificationService.sendOrderNotification(supplier, savedOrder);

        return orderRepository.save(savedOrder);
    }


    public Order updateOrderStatus(Long id, OrderStatus status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
