package com.retailpos.service;

import com.retailpos.model.Product;
import com.retailpos.model.StockTransaction;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.StockTransactionRepository;

import java.util.List;

public class InventoryService {

    private final ProductRepository productRepository;
    private final StockTransactionRepository transactionRepository;

    public InventoryService(
            ProductRepository productRepository,
            StockTransactionRepository transactionRepository
    ) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    // =========================
    // STOCK IN
    // =========================

    public void stockIn(
            String productId,
            int quantity,
            String reason,
            String performedBy
    ) {

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Stock quantity must be greater than zero."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        int previousStock =
                product.getStockQuantity();

        int newStock =
                previousStock + quantity;

        product.setStockQuantity(newStock);

        productRepository.update(product);

        StockTransaction transaction =
                new StockTransaction(
                        product.getId(),
                        product.getName(),
                        "STOCK_IN",
                        quantity,
                        previousStock,
                        newStock,
                        reason,
                        performedBy
                );

        transactionRepository.save(transaction);
    }

    // =========================
    // STOCK OUT
    // =========================

    public void stockOut(
            String productId,
            int quantity,
            String reason,
            String performedBy
    ) {

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Stock quantity must be greater than zero."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        int previousStock =
                product.getStockQuantity();

        if (quantity > previousStock) {
            throw new IllegalArgumentException(
                    "Insufficient stock."
            );
        }

        int newStock =
                previousStock - quantity;

        product.setStockQuantity(newStock);

        productRepository.update(product);

        StockTransaction transaction =
                new StockTransaction(
                        product.getId(),
                        product.getName(),
                        "STOCK_OUT",
                        quantity,
                        previousStock,
                        newStock,
                        reason,
                        performedBy
                );

        transactionRepository.save(transaction);
    }

    // =========================
    // STOCK ADJUSTMENT
    // =========================

    public void adjustStock(
            String productId,
            int newStock,
            String reason,
            String performedBy
    ) {

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot be negative."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        int previousStock =
                product.getStockQuantity();

        if (previousStock == newStock) {
            throw new IllegalArgumentException(
                    "New stock is the same as current stock."
            );
        }

        int quantity =
                Math.abs(newStock - previousStock);

        product.setStockQuantity(newStock);

        productRepository.update(product);

        StockTransaction transaction =
                new StockTransaction(
                        product.getId(),
                        product.getName(),
                        "ADJUSTMENT",
                        quantity,
                        previousStock,
                        newStock,
                        reason,
                        performedBy
                );

        transactionRepository.save(transaction);
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    public List<StockTransaction> getAllTransactions() {

        return transactionRepository.findAll();
    }

    public List<StockTransaction> getProductTransactions(
            String productId
    ) {

        return transactionRepository.findByProductId(
                productId
        );
    }
}