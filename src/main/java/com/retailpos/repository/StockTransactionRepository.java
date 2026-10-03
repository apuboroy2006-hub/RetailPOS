package com.retailpos.repository;

import com.retailpos.model.StockTransaction;

import java.util.List;

public interface StockTransactionRepository {

    void save(StockTransaction transaction);

    List<StockTransaction> findAll();

    List<StockTransaction> findByProductId(String productId);
}