package com.retailpos.model;

import java.time.LocalDateTime;
import com.retailpos.model.Product;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.service.ProductService;

import java.util.List;
public class Product {

    private String id;
    private String name;
    private String sku;
    private String barcode;
    private String category;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;
    private int minimumStock;
    private String supplier;
    private String status;
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(
            String name,
            String sku,
            String barcode,
            String category,
            double purchasePrice,
            double sellingPrice,
            int stockQuantity,
            int minimumStock,
            String supplier,
            String status
    ) {
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.minimumStock = minimumStock;
        this.supplier = supplier;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + sku + ")";
    }
}