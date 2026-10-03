package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Purchase;

public interface PurchaseRepository {

    void save(Purchase purchase);

    Optional<Purchase> findById(String id);

    Optional<Purchase> findByInvoiceNumber(
            String invoiceNumber
    );

    List<Purchase> findAll();

    List<Purchase> findBySupplierId(
            String supplierId
    );

    void update(Purchase purchase);
}