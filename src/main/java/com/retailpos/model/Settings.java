package com.retailpos.model;

public class Settings {

    private String storeName;
    private String storeAddress;
    private String phone;
    private String email;
    private String currency;
    private String taxNumber;
    private String invoicePrefix;
    private boolean showTaxOnInvoice;

    public Settings() {
    }

    // Existing 6-parameter constructor
    public Settings(
            String storeName,
            String storeAddress,
            String phone,
            String email,
            String currency,
            String taxNumber
    ) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.phone = phone;
        this.email = email;
        this.currency = currency;
        this.taxNumber = taxNumber;
        this.invoicePrefix = "INV-";
        this.showTaxOnInvoice = true;
    }

    // New 8-parameter constructor
    public Settings(
            String storeName,
            String storeAddress,
            String phone,
            String email,
            String currency,
            String taxNumber,
            String invoicePrefix,
            boolean showTaxOnInvoice
    ) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.phone = phone;
        this.email = email;
        this.currency = currency;
        this.taxNumber = taxNumber;
        this.invoicePrefix = invoicePrefix;
        this.showTaxOnInvoice = showTaxOnInvoice;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getInvoicePrefix() {
        return invoicePrefix;
    }

    public void setInvoicePrefix(String invoicePrefix) {
        this.invoicePrefix = invoicePrefix;
    }

    public boolean isShowTaxOnInvoice() {
        return showTaxOnInvoice;
    }

    public void setShowTaxOnInvoice(
            boolean showTaxOnInvoice
    ) {
        this.showTaxOnInvoice = showTaxOnInvoice;
    }
}