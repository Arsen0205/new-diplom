package com.example.diplom.controller;

import com.example.diplom.dto.request.ConfirmedOrderDtoRequest;
import com.example.diplom.dto.request.CreateOrderRequest;
import com.example.diplom.models.Order;
import com.example.diplom.models.enums.OrderStatus;
import com.example.diplom.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PutMapping("/{id}/status")
//    public ResponseEntity<Order> updateOrderStatus(@RequestBody Long id,  OrderStatus status) {
//        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/confirmed")
    public ResponseEntity<Order> confirmedOrder(@RequestBody ConfirmedOrderDtoRequest request){
        return ResponseEntity.ok(orderService.confirmedOrder(request));
    }

    @PostMapping("/cancelled")
    public ResponseEntity<Order> cancelledOrder(@RequestBody ConfirmedOrderDtoRequest request){
        return ResponseEntity.ok(orderService.cancelledOrder(request));
    }

    @PostMapping("/delivered")
    public ResponseEntity<Order> deliveredOrder(@RequestBody ConfirmedOrderDtoRequest request){
        return ResponseEntity.ok(orderService.deliveredOrder(request));
    }

    @PostMapping("/shipped")
    public ResponseEntity<Order> shippedOrder(@RequestBody ConfirmedOrderDtoRequest request){
        return ResponseEntity.ok(orderService.shippedOrder(request));
    }
}
