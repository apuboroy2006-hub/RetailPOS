package com.retailpos.model;

import java.time.LocalDateTime;

public class Purchase {

    private String id;
    private String invoiceNumber;
    private String supplierId;
    private String supplierName;

    private double subtotal;
    private double discount;
    private double tax;
    private double total;

    private String paymentMethod;
    private String status;
    private String purchasedBy;

    private LocalDateTime createdAt;

    public Purchase() {
    }

    public Purchase(
            String invoiceNumber,
            String supplierId,
            String supplierName,
            double subtotal,
            double discount,
            double tax,
            double total,
            String paymentMethod,
            String status,
            String purchasedBy
    ) {

        this.invoiceNumber = invoiceNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.purchasedBy = purchasedBy;

        this.createdAt =
                LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(
            String invoiceNumber
    ) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(
            String supplierId
    ) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(
            String supplierName
    ) {
        this.supplierName = supplierName;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            double subtotal
    ) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(
            double discount
    ) {
        this.discount = discount;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(
            double tax
    ) {
        this.tax = tax;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(
            double total
    ) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            String paymentMethod
    ) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status
    ) {
        this.status = status;
    }

    public String getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(
            String purchasedBy
    ) {
        this.purchasedBy = purchasedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}