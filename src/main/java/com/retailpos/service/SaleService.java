package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Sale;
import com.retailpos.model.SaleItem;
import com.retailpos.repository.SaleItemRepository;
import com.retailpos.repository.SaleRepository;

public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    public SaleService(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository
    ) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
    }

    public void createSale(
            Sale sale,
            List<SaleItem> items
    ) {

        if (sale == null) {
            throw new IllegalArgumentException(
                    "Sale cannot be null."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "A sale must contain at least one item."
            );
        }

        if (sale.getInvoiceNumber() == null ||
                sale.getInvoiceNumber().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Invoice number is required."
            );
        }

        Optional<Sale> existing =
                saleRepository.findByInvoiceNumber(
                        sale.getInvoiceNumber().trim()
                );

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Invoice number already exists."
            );
        }

        validateItems(items);

        saleRepository.save(sale);

        for (SaleItem item : items) {
            item.setSaleId(sale.getId());
            saleItemRepository.save(item);
        }
    }

    public Optional<Sale> findById(String id) {
        return saleRepository.findById(id);
    }

    public Optional<Sale> findByInvoiceNumber(
            String invoiceNumber
    ) {
        return saleRepository.findByInvoiceNumber(
                invoiceNumber
        );
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<Sale> getCustomerSales(
            String customerId
    ) {
        return saleRepository.findByCustomerId(
                customerId
        );
    }

    public List<SaleItem> getSaleItems(
            String saleId
    ) {
        return saleItemRepository.findBySaleId(
                saleId
        );
    }

    public void updateSale(Sale sale) {

        if (sale == null) {
            throw new IllegalArgumentException(
                    "Sale cannot be null."
            );
        }

        if (sale.getId() == null ||
                sale.getId().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Sale ID is required."
            );
        }

        saleRepository.update(sale);
    }

    private void validateItems(
            List<SaleItem> items
    ) {

        for (SaleItem item : items) {

            if (item == null) {
                throw new IllegalArgumentException(
                        "Sale item cannot be null."
                );
            }

            if (item.getProductId() == null ||
                    item.getProductId().trim().isEmpty()) {

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
    }
}