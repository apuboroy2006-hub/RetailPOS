package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Purchase;
import com.retailpos.model.PurchaseItem;
import com.retailpos.repository.PurchaseItemRepository;
import com.retailpos.repository.PurchaseRepository;

public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductService productService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            ProductService productService
    ) {

        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.productService = productService;
    }

    public void createPurchase(
            Purchase purchase,
            List<PurchaseItem> items
    ) {

        if (purchase == null) {
            throw new IllegalArgumentException(
                    "Purchase is required."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must contain at least one item."
            );
        }

        if (purchase.getInvoiceNumber() == null ||
                purchase.getInvoiceNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number is required."
            );
        }

        if (purchaseRepository
                .findByInvoiceNumber(
                        purchase.getInvoiceNumber()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Invoice number already exists."
            );
        }

        for (PurchaseItem item : items) {

            if (item == null) {
                throw new IllegalArgumentException(
                        "Invalid purchase item."
                );
            }

            if (item.getProductId() == null ||
                    item.getProductId().isBlank()) {

                throw new IllegalArgumentException(
                        "Product ID is required."
                );
            }

            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero."
                );
            }

            if (item.getUnitPrice() < 0) {

                throw new IllegalArgumentException(
                        "Unit price cannot be negative."
                );
            }

            if (item.getDiscount() < 0) {

                throw new IllegalArgumentException(
                        "Discount cannot be negative."
                );
            }

            if (item.getTotal() < 0) {

                throw new IllegalArgumentException(
                        "Item total cannot be negative."
                );
            }
        }

        purchaseRepository.save(purchase);

        for (PurchaseItem item : items) {

            item.setPurchaseId(
                    purchase.getId()
            );

            purchaseItemRepository.save(item);

            productService.increaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }
    }

    public Optional<Purchase> findById(
            String id
    ) {

        return purchaseRepository.findById(id);
    }

    public Optional<Purchase> findByInvoiceNumber(
            String invoiceNumber
    ) {

        return purchaseRepository
                .findByInvoiceNumber(invoiceNumber);
    }

    public List<Purchase> getAllPurchases() {

        return purchaseRepository.findAll();
    }

    public List<Purchase> getSupplierPurchases(
            String supplierId
    ) {

        return purchaseRepository
                .findBySupplierId(supplierId);
    }

    public List<PurchaseItem> getPurchaseItems(
            String purchaseId
    ) {

        return purchaseItemRepository
                .findByPurchaseId(purchaseId);
    }

    public void updatePurchase(
            Purchase purchase
    ) {

        if (purchase == null ||
                purchase.getId() == null ||
                purchase.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid purchase."
            );
        }

        purchaseRepository.update(purchase);
    }
}