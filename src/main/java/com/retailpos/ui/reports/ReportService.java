package com.retailpos.report;

import java.util.List;

import com.retailpos.model.Purchase;
import com.retailpos.model.Sale;
import com.retailpos.model.SaleItem;
import com.retailpos.repository.PurchaseRepository;
import com.retailpos.repository.SaleItemRepository;
import com.retailpos.repository.SaleRepository;
import com.retailpos.service.ProductService;

public class ReportService {
private final SaleItemRepository saleItemRepository;
private final ProductService productService;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

   public ReportService(
        SaleRepository saleRepository,
        PurchaseRepository purchaseRepository,
        SaleItemRepository saleItemRepository,
        ProductService productService
) {
    this.saleRepository = saleRepository;
    this.purchaseRepository = purchaseRepository;
    this.saleItemRepository = saleItemRepository;
    this.productService = productService;
}

    public List<Sale> getAllSales() {

        return saleRepository.findAll();
    }

    public List<Purchase> getAllPurchases() {

        return purchaseRepository.findAll();
    }
    public double calculateTotalSales() {

    double totalSales = 0;

    List<Sale> sales =
            saleRepository.findAll();

    for (Sale sale : sales) {

        totalSales +=
                sale.getTotal();
    }

    return totalSales;
}

public double calculateTotalPurchases() {

    double totalPurchases = 0;

    List<Purchase> purchases =
            purchaseRepository.findAll();

    for (Purchase purchase :
            purchases) {

        totalPurchases +=
                purchase.getTotal();
    }

    return totalPurchases;
}

public double calculateGrossProfit() {

    double totalSales =
            calculateTotalSales();

    double costOfGoodsSold = 0;

    List<Sale> sales =
            saleRepository.findAll();

    for (Sale sale : sales) {

        List<SaleItem> items =
                saleItemRepository.findBySaleId(
                        sale.getId()
                );

        for (SaleItem item : items) {

            double purchasePrice =
        productService
                .findById(
                        item.getProductId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Product not found."
                        )
                )
                .getPurchasePrice();

            costOfGoodsSold +=
                    purchasePrice *
                    item.getQuantity();
        }
    }

    return totalSales
            - costOfGoodsSold;
}
}