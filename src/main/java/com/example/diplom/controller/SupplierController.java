package com.example.diplom.controller;


import com.example.diplom.dto.request.SupplierRegisterDtoRequest;
import com.example.diplom.service.SupplierService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supplier")
@AllArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody SupplierRegisterDtoRequest request){
        supplierService.registerSupplier(request);
        return ResponseEntity.ok("Вы успешно зарегистрировались!");
    }
}
