package com.example.diplom.service;


import com.example.diplom.models.Order;
import com.example.diplom.models.OrderItem;
import com.example.diplom.models.Product;
import com.example.diplom.models.Supplier;
import com.example.diplom.models.enums.OrderStatus;
import com.example.diplom.repository.OrderRepository;
import com.example.diplom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final RestTemplate restTemplate;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final String TELEGRAM_BOT_TOKEN="8067199276:AAHkNyreW9RthpGYZsTBc6V543MvZOf-Vu0";
    private final String TELEGRAM_CHAT_ID="1003385031";
    private final String CUSTOMER_CHAT_ID="953484652";

    public void sendOrderNotification(Supplier supplier, Order order) {
        String message = "📦 Новый заказ! \n" +
                "ID заказа: " + order.getId() + "\n" +
                "Общая стоимость: " + order.getTotalPrice() + " ₽\n" +
                "Прибыль: " + order.getProfit() + " ₽\n" +
                "Статус: " + order.getStatus() + "\n\n" +
                "Товары в заказе:\n";

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                message += item.getProduct().getTitle() + " (Кол-во: " + item.getQuantity() + ", Цена: " + item.getProduct().getSellingPrice() + " ₽)\n";
            }
        } else {
            message += "Товары в заказе отсутствуют.\n";
        }
               message += "Принять: /accept_" + order.getId() + "\n" +
                "Отклонить: /reject_" + order.getId();

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + message;
        restTemplate.getForObject(url, String.class);
    }


    public void handleOrderResponse(String command){
        if(command.startsWith("/accept_")){
            Long orderId = Long.parseLong(command.replace("/accept_", ""));
            acceptOrder(orderId);
        }else if(command.startsWith("/reject_")){
            Long orderId = Long.parseLong(command.replace("/reject_",""));
            rejectOrder(orderId);
        }
    }

    private void acceptOrder(Long id){
        Optional<Order> orderOptional = orderRepository.findById(id);

        Order order = orderOptional.get();

        for(OrderItem item : order.getOrderItems()){
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity()-item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + "Вы подтвердили заказ!";
        restTemplate.getForObject(url, String.class);
        String text = "✅ Ваш заказ №" + id + " подтвержден поставщиком!";
        String url1 = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + CUSTOMER_CHAT_ID + "&text=" + text;
        restTemplate.getForObject(url1, String.class);
    }

    private void rejectOrder(Long id){

    }

    private void sendMessage(String chatId, String text) {
        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + text;
        restTemplate.getForObject(url, String.class);
    }
}
