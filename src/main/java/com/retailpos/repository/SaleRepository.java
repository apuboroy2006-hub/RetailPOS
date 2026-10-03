package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Sale;

public interface SaleRepository {

    void save(Sale sale);

    Optional<Sale> findById(String id);

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    List<Sale> findAll();

    List<Sale> findByCustomerId(String customerId);

    void update(Sale sale);
}