package com.example.retailpos_backend.controller;

import com.example.retailpos_backend.model.Product;
import com.example.retailpos_backend.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // =========================
    // GET ALL PRODUCTS
    // =========================

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // =========================
    // GET PRODUCT BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable String id) {

        Optional<Product> product =
                productRepository.findById(id);

        return product
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =========================
    // CREATE PRODUCT
    // =========================

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        product.setId(null);

        Product savedProduct =
                productRepository.save(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedProduct);
    }

    // =========================
    // UPDATE PRODUCT
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestBody Product product) {

        Optional<Product> existingProduct =
                productRepository.findById(id);

        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        product.setId(id);

        Product updatedProduct =
                productRepository.save(product);

        return ResponseEntity.ok(updatedProduct);
    }

    // =========================
    // DELETE PRODUCT
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id) {

        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        productRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}