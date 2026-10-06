package com.example.retailpos_backend.repository;

import com.example.retailpos_backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}