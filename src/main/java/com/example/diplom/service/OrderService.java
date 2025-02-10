package com.example.diplom.service;

import com.example.diplom.dto.request.ConfirmedOrderDtoRequest;
import com.example.diplom.dto.request.CreateOrderRequest;
import com.example.diplom.dto.request.OrderItemDtoRequest;
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
import java.util.Optional;

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


    //Создание заказа
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Поставщик не найден"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Заказ должен содержать хотя бы один товар.");
        }

        // Создаем заказ
        Order newOrder = Order.builder()
                .supplier(supplier)
                .totalCost(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .profit(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .city(request.getCity())
                .address(request.getAddress())
                .orderItems(new ArrayList<>())
                .build();

        // Сохраняем заказ, чтобы получить его ID
        Order savedOrder = orderRepository.save(newOrder);

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDtoRequest itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Продукт не найден"));

            // Проверяем доступное количество товара
            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара: " + product.getTitle());
            }

            BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            BigDecimal totalPrice = product.getSellingPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            // Создаем позицию заказа
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .costPrice(product.getPrice())
                    .sellingPrice(product.getSellingPrice())
                    .totalCost(totalCost)
                    .totalPrice(totalPrice)
                    .build();

            orderItems.add(orderItem);

            // Уменьшаем количество товара на складе
            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);
        }

        // Сохраняем позиции заказа
        orderItemRepository.saveAll(orderItems);

        // Обновляем заказ с товарами
        savedOrder.getOrderItems().addAll(orderItems);

        // Обновляем итоговые суммы заказа
        BigDecimal totalCost = orderItems.stream().map(OrderItem::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = orderItems.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = totalPrice.subtract(totalCost);

        savedOrder.setTotalCost(totalCost);
        savedOrder.setTotalPrice(totalPrice);
        savedOrder.setProfit(profit);

        // Отправка уведомления поставщику
        telegramNotificationService.sendOrderNotification(supplier, savedOrder);

        return orderRepository.save(savedOrder);
    }


    //Обновления статуса заказа
    public Order updateOrderStatus(Long id, OrderStatus status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    //Удаление заказа
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    //Подтверждение заказа через сайт
    public Order confirmedOrder(ConfirmedOrderDtoRequest request){
        return orderRepository.findById(request.getId()).map(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            telegramNotificationService.acceptOrder(request.getId());
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    public Order cancelledOrder(ConfirmedOrderDtoRequest request){
        return orderRepository.findById(request.getId()).map(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            telegramNotificationService.rejectOrder(request.getId());
            return orderRepository.save(order);
        }).orElseThrow(()-> new RuntimeException("Заказ не найден"));
    }
}
