package com.example.diplom.controller;


import com.example.diplom.dto.request.AddProductRequestDto;
import com.example.diplom.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;


    @PostMapping("/create")
    public ResponseEntity<String> create(@Valid @RequestBody AddProductRequestDto request){
        productService.createProduct(request);
        return ResponseEntity.ok("Товар успешно создан");
    }
}
