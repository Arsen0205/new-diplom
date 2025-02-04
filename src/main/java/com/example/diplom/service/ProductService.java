package com.example.diplom.service;


import com.example.diplom.dto.request.AddProductRequestDto;
import com.example.diplom.models.Product;
import com.example.diplom.models.Supplier;
import com.example.diplom.repository.ProductRepository;
import com.example.diplom.repository.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public Product createProduct(AddProductRequestDto request){
        Supplier supplier = supplierRepository.findByLogin(request.getLogin()).orElseThrow(()-> new IllegalArgumentException("Пользователя с таким логином не найден!"));

        Product product = new Product();

        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setQuantity(request.getQuantity());
        product.setSupplier(supplier);

        return productRepository.save(product);
    }
}
