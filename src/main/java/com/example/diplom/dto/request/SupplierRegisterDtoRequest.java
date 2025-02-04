package com.example.diplom.dto.request;


import com.example.diplom.models.enums.Role;
import lombok.Data;

@Data
public class SupplierRegisterDtoRequest {
    private String login;
    private String password;
    private String loginTelegram;
    private Role role;
}
