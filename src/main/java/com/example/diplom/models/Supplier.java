package com.example.diplom.models;


import com.example.diplom.models.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "date_of_created")
    private LocalDateTime dateOfCreated;

    @Column(name="login_telegram", unique = true)
    private String loginTelegram;

    @Column(name = "chat_id", nullable = true, unique = true)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    private void init() {
        dateOfCreated = LocalDateTime.now();
    }
}
