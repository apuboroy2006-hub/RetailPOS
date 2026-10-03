package com.retailpos.repository;

import java.util.List;

import com.retailpos.model.SaleItem;

public interface SaleItemRepository {

    void save(SaleItem saleItem);

    List<SaleItem> findBySaleId(String saleId);

    void deleteBySaleId(String saleId);
}