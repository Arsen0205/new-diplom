package com.example.diplom.controller;

import com.example.diplom.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final TelegramNotificationService telegramNotificationService;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveUpdate(@RequestBody Map<String, Object> update) {
        try {
            // Достаем текст сообщения из JSON-ответа Telegram
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                return ResponseEntity.ok("No message field.");
            }

            String text = (String) message.get("text");
            if (text != null && (text.startsWith("/accept_") || text.startsWith("/reject_"))) {
                telegramNotificationService.handleOrderResponse(text);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing Telegram update");
        }
    }
}
