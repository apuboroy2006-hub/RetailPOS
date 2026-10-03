package com.retailpos.repository;

import com.retailpos.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String id);

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    List<Product> findAll();

    List<Product> findByName(String name);

    void update(Product product);

    void deleteById(String id);
}