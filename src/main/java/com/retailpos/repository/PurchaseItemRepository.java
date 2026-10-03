package com.retailpos.repository;

import java.util.List;

import com.retailpos.model.PurchaseItem;

public interface PurchaseItemRepository {

    void save(PurchaseItem purchaseItem);

    List<PurchaseItem> findByPurchaseId(
            String purchaseId
    );

    void deleteByPurchaseId(
            String purchaseId
    );
}