package com.example.diplom.service;


import com.example.diplom.models.Order;
import com.example.diplom.models.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.ref.PhantomReference;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final RestTemplate restTemplate;

    private final String TELEGRAM_BOT_TOKEN="8067199276:AAHkNyreW9RthpGYZsTBc6V543MvZOf-Vu0";
    private final String TELEGRAM_CHAT_ID="1003385031";

    public void sendOrderNotification(Supplier supplier, Order order) {
        String message = "📦 Новый заказ! \n" +
                "ID заказа: " + order.getId() + "\n" +
                "Общая стоимость: " + order.getTotalPrice() + " ₽\n" +
                "Прибыль: " + order.getProfit() + " ₽\n" +
                "Статус: " + order.getStatus() + "\n\n" +
                "Принять: /accept_" + order.getId() + "\n" +
                "Отклонить: /reject_" + order.getId();

        String url = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage?chat_id=" + TELEGRAM_CHAT_ID + "&text=" + message;
        restTemplate.getForObject(url, String.class);
    }
}
