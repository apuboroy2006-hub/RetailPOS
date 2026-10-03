package com.retailpos.repository.mongodb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Purchase;
import com.retailpos.repository.PurchaseRepository;

public class PurchaseRepositoryImpl
        implements PurchaseRepository {

    private final MongoCollection<Document> collection;

    public PurchaseRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        collection =
                database.getCollection("purchases");
    }

    @Override
    public void save(Purchase purchase) {

        Document document =
                new Document();

        document.append(
                "invoiceNumber",
                purchase.getInvoiceNumber()
        );

        document.append(
                "supplierId",
                purchase.getSupplierId()
        );

        document.append(
                "supplierName",
                purchase.getSupplierName()
        );

        document.append(
                "subtotal",
                purchase.getSubtotal()
        );

        document.append(
                "discount",
                purchase.getDiscount()
        );

        document.append(
                "tax",
                purchase.getTax()
        );

        document.append(
                "total",
                purchase.getTotal()
        );

        document.append(
                "paymentMethod",
                purchase.getPaymentMethod()
        );

        document.append(
                "status",
                purchase.getStatus()
        );

        document.append(
                "purchasedBy",
                purchase.getPurchasedBy()
        );

        document.append(
                "createdAt",
                purchase.getCreatedAt().toString()
        );

        collection.insertOne(document);

        purchase.setId(
                document.getObjectId("_id").toHexString()
        );
    }

    @Override
    public Optional<Purchase> findById(
            String id
    ) {

        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        Document document =
                collection.find(
                        new Document(
                                "_id",
                                new ObjectId(id)
                        )
                ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                mapDocumentToPurchase(document)
        );
    }

    @Override
    public Optional<Purchase> findByInvoiceNumber(
            String invoiceNumber
    ) {

        Document document =
                collection.find(
                        new Document(
                                "invoiceNumber",
                                invoiceNumber
                        )
                ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                mapDocumentToPurchase(document)
        );
    }

    @Override
    public List<Purchase> findAll() {

        List<Purchase> purchases =
                new ArrayList<>();

        for (Document document : collection.find()) {

            purchases.add(
                    mapDocumentToPurchase(document)
            );
        }

        return purchases;
    }

    @Override
    public List<Purchase> findBySupplierId(
            String supplierId
    ) {

        List<Purchase> purchases =
                new ArrayList<>();

        for (
                Document document :
                collection.find(
                        new Document(
                                "supplierId",
                                supplierId
                        )
                )
        ) {

            purchases.add(
                    mapDocumentToPurchase(document)
            );
        }

        return purchases;
    }

    @Override
    public void update(
            Purchase purchase
    ) {

        if (!ObjectId.isValid(purchase.getId())) {
            throw new IllegalArgumentException(
                    "Invalid purchase ID."
            );
        }

        Document document =
                new Document();

        document.append(
                "invoiceNumber",
                purchase.getInvoiceNumber()
        );

        document.append(
                "supplierId",
                purchase.getSupplierId()
        );

        document.append(
                "supplierName",
                purchase.getSupplierName()
        );

        document.append(
                "subtotal",
                purchase.getSubtotal()
        );

        document.append(
                "discount",
                purchase.getDiscount()
        );

        document.append(
                "tax",
                purchase.getTax()
        );

        document.append(
                "total",
                purchase.getTotal()
        );

        document.append(
                "paymentMethod",
                purchase.getPaymentMethod()
        );

        document.append(
                "status",
                purchase.getStatus()
        );

        document.append(
                "purchasedBy",
                purchase.getPurchasedBy()
        );

        document.append(
                "createdAt",
                purchase.getCreatedAt().toString()
        );

        collection.replaceOne(
                new Document(
                        "_id",
                        new ObjectId(purchase.getId())
                ),
                document
        );
    }

    private Purchase mapDocumentToPurchase(
            Document document
    ) {

        Purchase purchase =
                new Purchase();

        purchase.setId(
                document.getObjectId("_id")
                        .toHexString()
        );

        purchase.setInvoiceNumber(
                document.getString("invoiceNumber")
        );

        purchase.setSupplierId(
                document.getString("supplierId")
        );

        purchase.setSupplierName(
                document.getString("supplierName")
        );

        purchase.setSubtotal(
                document.getDouble("subtotal")
        );

        purchase.setDiscount(
                document.getDouble("discount")
        );

        purchase.setTax(
                document.getDouble("tax")
        );

        purchase.setTotal(
                document.getDouble("total")
        );

        purchase.setPaymentMethod(
                document.getString("paymentMethod")
        );

        purchase.setStatus(
                document.getString("status")
        );

        purchase.setPurchasedBy(
                document.getString("purchasedBy")
        );

        purchase.setCreatedAt(
                LocalDateTime.parse(
                        document.getString("createdAt")
                )
        );

        return purchase;
    }
}