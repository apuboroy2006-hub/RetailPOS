package com.retailpos.repository.mongodb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Sale;
import com.retailpos.repository.SaleRepository;

public class SaleRepositoryImpl implements SaleRepository {

    private final MongoCollection<Document> collection;

    public SaleRepositoryImpl() {
        MongoDatabase database = DatabaseConfig.getDatabase();
        collection = database.getCollection("sales");
    }

    @Override
    public void save(Sale sale) {

        Document document = new Document()
                .append("invoiceNumber", sale.getInvoiceNumber())
                .append("customerId", sale.getCustomerId())
                .append("customerName", sale.getCustomerName())
                .append("subtotal", sale.getSubtotal())
                .append("discount", sale.getDiscount())
                .append("tax", sale.getTax())
                .append("total", sale.getTotal())
                .append("paymentMethod", sale.getPaymentMethod())
                .append("status", sale.getStatus())
                .append("soldBy", sale.getSoldBy())
                .append("createdAt", sale.getCreatedAt().toString());

        collection.insertOne(document);

        sale.setId(
                document.getObjectId("_id").toHexString()
        );
    }

    @Override
    public Optional<Sale> findById(String id) {

        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        Document document = collection.find(
                eq("_id", new ObjectId(id))
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toSale(document));
    }

    @Override
    public Optional<Sale> findByInvoiceNumber(
            String invoiceNumber
    ) {

        Document document = collection.find(
                eq("invoiceNumber", invoiceNumber)
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toSale(document));
    }

    @Override
    public List<Sale> findAll() {

        List<Sale> sales = new ArrayList<>();

        for (Document document : collection.find()) {
            sales.add(toSale(document));
        }

        return sales;
    }

    @Override
    public List<Sale> findByCustomerId(
            String customerId
    ) {

        List<Sale> sales = new ArrayList<>();

        for (Document document : collection.find(
                eq("customerId", customerId)
        )) {
            sales.add(toSale(document));
        }

        return sales;
    }

    @Override
    public void update(Sale sale) {

        if (!ObjectId.isValid(sale.getId())) {
            return;
        }

        Document update = new Document()
                .append("invoiceNumber", sale.getInvoiceNumber())
                .append("customerId", sale.getCustomerId())
                .append("customerName", sale.getCustomerName())
                .append("subtotal", sale.getSubtotal())
                .append("discount", sale.getDiscount())
                .append("tax", sale.getTax())
                .append("total", sale.getTotal())
                .append("paymentMethod", sale.getPaymentMethod())
                .append("status", sale.getStatus())
                .append("soldBy", sale.getSoldBy());

        collection.updateOne(
                eq("_id", new ObjectId(sale.getId())),
                new Document("$set", update)
        );
    }

    private Sale toSale(Document document) {

        Sale sale = new Sale();

        sale.setId(
                document.getObjectId("_id").toHexString()
        );

        sale.setInvoiceNumber(
                document.getString("invoiceNumber")
        );

        sale.setCustomerId(
                document.getString("customerId")
        );

        sale.setCustomerName(
                document.getString("customerName")
        );

        sale.setSubtotal(
                document.getDouble("subtotal")
        );

        sale.setDiscount(
                document.getDouble("discount")
        );

        sale.setTax(
                document.getDouble("tax")
        );

        sale.setTotal(
                document.getDouble("total")
        );

        sale.setPaymentMethod(
                document.getString("paymentMethod")
        );

        sale.setStatus(
                document.getString("status")
        );

        sale.setSoldBy(
                document.getString("soldBy")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {
            sale.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        return sale;
    }
}