package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Product;
import com.retailpos.repository.ProductRepository;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createProduct(
            String name,
            String sku,
            String barcode,
            String category,
            double purchasePrice,
            double sellingPrice,
            int stockQuantity,
            int minimumStock,
            String supplier
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException(
                    "SKU is required."
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Category is required."
            );
        }

        if (purchasePrice < 0) {
            throw new IllegalArgumentException(
                    "Purchase price cannot be negative."
            );
        }

        if (sellingPrice < 0) {
            throw new IllegalArgumentException(
                    "Selling price cannot be negative."
            );
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative."
            );
        }

        if (minimumStock < 0) {
            throw new IllegalArgumentException(
                    "Minimum stock cannot be negative."
            );
        }

        if (sellingPrice < purchasePrice) {
            throw new IllegalArgumentException(
                    "Selling price cannot be lower than purchase price."
            );
        }

        if (productRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException(
                    "SKU already exists."
            );
        }

        if (barcode != null &&
                !barcode.isBlank() &&
                productRepository
                        .findByBarcode(barcode)
                        .isPresent()) {

            throw new IllegalArgumentException(
                    "Barcode already exists."
            );
        }

        Product product =
                new Product(
                        name,
                        sku,
                        barcode,
                        category,
                        purchasePrice,
                        sellingPrice,
                        stockQuantity,
                        minimumStock,
                        supplier,
                        "ACTIVE"
                );

        productRepository.save(product);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String name) {

        if (name == null || name.isBlank()) {
            return productRepository.findAll();
        }

        return productRepository.findByName(name);
    }

    public void updateProduct(Product product) {

        if (product == null ||
                product.getId() == null ||
                product.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid product."
            );
        }

        if (product.getName() == null ||
                product.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        if (product.getSellingPrice() <
                product.getPurchasePrice()) {

            throw new IllegalArgumentException(
                    "Selling price cannot be lower than purchase price."
            );
        }

        if (product.getStockQuantity() < 0) {

            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative."
            );
        }

        if (product.getMinimumStock() < 0) {

            throw new IllegalArgumentException(
                    "Minimum stock cannot be negative."
            );
        }

        productRepository.update(product);
    }

    public void deleteProduct(
            String productId
    ) {

        if (productId == null ||
                productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        productRepository.deleteById(
                productId
        );
    }

    public void reduceStock(
            String productId,
            int quantity
    ) {

        if (productId == null ||
                productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        int currentStock =
                product.getStockQuantity();

        if (quantity > currentStock) {

            throw new IllegalArgumentException(
                    "Insufficient stock."
            );
        }

        product.setStockQuantity(
                currentStock - quantity
        );

        productRepository.update(product);
    }

    public void increaseStock(
            String productId,
            int quantity
    ) {

        if (productId == null ||
                productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        int currentStock =
                product.getStockQuantity();

        product.setStockQuantity(
                currentStock + quantity
        );

        productRepository.update(product);
    }
}