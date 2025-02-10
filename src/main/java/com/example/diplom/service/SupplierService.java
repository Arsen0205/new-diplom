package com.example.diplom.service;


import com.example.diplom.dto.request.SupplierRegisterDtoRequest;
import com.example.diplom.models.Supplier;
import com.example.diplom.repository.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final PasswordEncoder passwordEncoder;
    private final TelegramNotificationService telegramNotificationService;

    //Регистрация поставщика
    public Supplier registerSupplier(SupplierRegisterDtoRequest request){
        if(supplierRepository.findByLogin(request.getLogin()).isPresent()){
            throw new IllegalArgumentException("Данный логин уже зарегистрирован!");
        }

        Supplier supplier = new Supplier();
        supplier.setLogin(request.getLogin());
        supplier.setPassword(passwordEncoder.encode(request.getPassword()));
        supplier.setLoginTelegram(request.getLoginTelegram());
        supplier.setRole(request.getRole());
        supplier.setChatId(telegramNotificationService.getChatIdByUsername(request.getLoginTelegram()));

        return supplierRepository.save(supplier);
    }
}
