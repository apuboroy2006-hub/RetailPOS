package com.retailpos.model;

public class PurchaseItem {

    private String id;
    private String purchaseId;
    private String productId;
    private String productName;
    private String sku;

    private int quantity;
    private double unitPrice;
    private double discount;
    private double total;

    public PurchaseItem() {
    }

    public PurchaseItem(
            String purchaseId,
            String productId,
            String productName,
            String sku,
            int quantity,
            double unitPrice,
            double discount,
            double total
    ) {

        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(
            String purchaseId
    ) {
        this.purchaseId = purchaseId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(
            String productId
    ) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName
    ) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(
            String sku
    ) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(
            int quantity
    ) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(
            double unitPrice
    ) {
        this.unitPrice = unitPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(
            double discount
    ) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(
            double total
    ) {
        this.total = total;
    }
}