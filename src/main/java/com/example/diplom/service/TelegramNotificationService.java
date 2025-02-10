package com.example.diplom.service;


import com.example.diplom.models.Order;
import com.example.diplom.models.OrderItem;
import com.example.diplom.models.Product;
import com.example.diplom.models.Supplier;
import com.example.diplom.models.enums.OrderStatus;
import com.example.diplom.repository.OrderRepository;
import com.example.diplom.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final String CUSTOMER_CHAT_ID="1003385031";
    private final String API_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN;


    //Отправка оповещения в телеграмме при создании заказа поставщику
    public void sendOrderNotification(Supplier supplier, Order order) {
        String message = "📦 Новый заказ! \n" +
                "ID заказа: " + order.getId() + "\n" +
                "Общая стоимость: " + order.getTotalPrice() + " ₽\n" +
                "Прибыль: " + order.getProfit() + " ₽\n" +
                "Статус: " + order.getStatus() + "\n" +
                "Адрес: г." + order.getCity() + ", " + order.getAddress() + "\n\n" +
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

    //Проверяет на какую кнопку нажал поставщик в телеграмме
    public void handleOrderResponse(String command){
        if(command.startsWith("/accept_")){
            Long orderId = Long.parseLong(command.replace("/accept_", ""));
            acceptOrder(orderId);
        }else if(command.startsWith("/reject_")){
            Long orderId = Long.parseLong(command.replace("/reject_",""));
            rejectOrder(orderId);
        } else if (command.startsWith("/shipped_")) {
            Long orderId = Long.parseLong(command.replace("/shipped_",""));
            shippedOrder(orderId);

        }
    }

    //Принятие заказа в телеграмм
    public void acceptOrder(Long id){
        Optional<Order> orderOptional = orderRepository.findById(id);

        Order order = orderOptional.get();

        //Уменьшение количества товара на складе
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);
            }


        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        String message = "Вы подтвердили заказ! \n\n" +
                "Изменить статус заказа: \n" +
                "В пути: /shipped_" + id + "\n" +
                "Отменить заказ: /reject_" + id;

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + message;
        restTemplate.getForObject(url, String.class);
        String text = "✅ Ваш заказ №" + id + " подтвержден поставщиком!";
        String url1 = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + CUSTOMER_CHAT_ID + "&text=" + text;
        restTemplate.getForObject(url1, String.class);
    }

    //Отклонение заказа в телеграмм
    public void rejectOrder(Long id){
        Optional<Order> orderOptional = orderRepository.findById(id);
        Order order = orderOptional.get();

        //Проверка на статус заказа, если заказ еще не подтвердился, то возвращать товары обратно нет необходимости
        if(order.getStatus() != OrderStatus.PENDING) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        orderRepository.delete(order);

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + "Вы отменили заказ!";
        restTemplate.getForObject(url, String.class);
        String text = "❌ Ваш заказ №" + id + " Отменен поставщиком!";
        String url1 = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + CUSTOMER_CHAT_ID + "&text=" + text;
        restTemplate.getForObject(url1, String.class);

    }

    //Изменение статуса на - в пути
    public void shippedOrder(Long id){
        Optional<Order> orderOptional = orderRepository.findById(id);
        Order order = orderOptional.get();

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        String message ="Статус заказа изменен \n\n" +
                "Изменить статус заказа: \n" +
                "Доставлен: /delivered_" + id +"\n" +
                "Отменить заказ: /reject_" + id;

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + message;
        restTemplate.getForObject(url, String.class);
        String text = "\uD83D\uDE9A Ваш заказ №" + id + " В пути";
        String url1 = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + CUSTOMER_CHAT_ID + "&text=" + text;
        restTemplate.getForObject(url1, String.class);
    }

    public  void deliveredOrder(Long id){
        Optional<Order> orderOptional = orderRepository.findById(id);
        Order order = orderOptional.get();

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        String message = "Заказ успешно доставлен";
        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + message;
        restTemplate.getForObject(url, String.class);
        String text = "✅ Ваш заказ №" + id + " Доставлен";
        String url1 = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + CUSTOMER_CHAT_ID + "&text=" + text;
        restTemplate.getForObject(url1, String.class);
    }

    private void sendMessage(String chatId, String text) {
        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + text;
        restTemplate.getForObject(url, String.class);
    }

    //Логика сохранения чат-айди пользователя по логину телеграмма
    public Long getChatIdByUsername(String username){
        try{
            String url = API_URL + "/getUpdates";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            for (JsonNode update : jsonNode.get("result")) {
                JsonNode message = update.get("message");
                if (message != null && message.has("chat")) {
                    JsonNode chat = message.get("chat");
                    if (chat.has("username") && username.equals(chat.get("username").asText())) {
                        return chat.get("id").asLong();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
