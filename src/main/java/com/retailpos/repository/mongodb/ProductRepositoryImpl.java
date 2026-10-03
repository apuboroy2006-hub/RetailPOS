package com.retailpos.repository.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Product;
import com.retailpos.repository.ProductRepository;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    private final MongoCollection<Document> collection;

    public ProductRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        collection =
                database.getCollection("products");
    }

    @Override
    public void save(Product product) {

        Document document =
                new Document()
                        .append("name", product.getName())
                        .append("sku", product.getSku())
                        .append("barcode", product.getBarcode())
                        .append("category", product.getCategory())
                        .append("purchasePrice", product.getPurchasePrice())
                        .append("sellingPrice", product.getSellingPrice())
                        .append("stockQuantity", product.getStockQuantity())
                        .append("minimumStock", product.getMinimumStock())
                        .append("supplier", product.getSupplier())
                        .append("status", product.getStatus())
                        .append(
                                "createdAt",
                                product.getCreatedAt().toString()
                        );

        collection.insertOne(document);

        product.setId(
                document.getObjectId("_id").toString()
        );
    }

    @Override
    public Optional<Product> findById(String id) {

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
                toProduct(document)
        );
    }

    @Override
    public Optional<Product> findBySku(String sku) {

        Document document =
                collection.find(
                        new Document("sku", sku)
                ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                toProduct(document)
        );
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {

        Document document =
                collection.find(
                        new Document("barcode", barcode)
                ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                toProduct(document)
        );
    }

    @Override
    public List<Product> findAll() {

        List<Product> products =
                new ArrayList<>();

        for (Document document :
                collection.find()) {

            products.add(
                    toProduct(document)
            );
        }

        return products;
    }

    @Override
    public List<Product> findByName(String name) {

        List<Product> products =
                new ArrayList<>();

        Document filter =
                new Document(
                        "name",
                        new Document(
                                "$regex",
                                name
                        ).append(
                                "$options",
                                "i"
                        )
                );

        for (Document document :
                collection.find(filter)) {

            products.add(
                    toProduct(document)
            );
        }

        return products;
    }

    @Override
    public void update(Product product) {

        if (!ObjectId.isValid(product.getId())) {
            return;
        }

        Document update =
                new Document()
                        .append("name", product.getName())
                        .append("sku", product.getSku())
                        .append("barcode", product.getBarcode())
                        .append("category", product.getCategory())
                        .append("purchasePrice", product.getPurchasePrice())
                        .append("sellingPrice", product.getSellingPrice())
                        .append("stockQuantity", product.getStockQuantity())
                        .append("minimumStock", product.getMinimumStock())
                        .append("supplier", product.getSupplier())
                        .append("status", product.getStatus());

        collection.updateOne(
                new Document(
                        "_id",
                        new ObjectId(product.getId())
                ),
                new Document("$set", update)
        );
    }

    @Override
    public void deleteById(String id) {

        if (!ObjectId.isValid(id)) {
            return;
        }

        collection.deleteOne(
                new Document(
                        "_id",
                        new ObjectId(id)
                )
        );
    }

    private Product toProduct(Document document) {

        Product product =
                new Product();

        product.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );

        product.setName(
                document.getString("name")
        );

        product.setSku(
                document.getString("sku")
        );

        product.setBarcode(
                document.getString("barcode")
        );

        product.setCategory(
                document.getString("category")
        );

        product.setPurchasePrice(
                document.getDouble("purchasePrice")
        );

        product.setSellingPrice(
                document.getDouble("sellingPrice")
        );

        product.setStockQuantity(
                document.getInteger("stockQuantity")
        );

        product.setMinimumStock(
                document.getInteger("minimumStock")
        );

        product.setSupplier(
                document.getString("supplier")
        );

        product.setStatus(
                document.getString("status")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {

            product.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        return product;
    }
}