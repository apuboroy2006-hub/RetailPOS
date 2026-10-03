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
import static com.mongodb.client.model.Filters.regex;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Supplier;
import com.retailpos.repository.SupplierRepository;

public class SupplierRepositoryImpl implements SupplierRepository {

    private final MongoCollection<Document> collection;

    public SupplierRepositoryImpl() {
        MongoDatabase database = DatabaseConfig.getDatabase();
        collection = database.getCollection("suppliers");
    }

    @Override
    public void save(Supplier supplier) {

        Document document = new Document()
                .append("name", supplier.getName())
                .append("phone", supplier.getPhone())
                .append("email", supplier.getEmail())
                .append("address", supplier.getAddress())
                .append("status", supplier.getStatus())
                .append("createdAt", supplier.getCreatedAt().toString());

        collection.insertOne(document);

        supplier.setId(document.getObjectId("_id").toHexString());
    }

    @Override
    public Optional<Supplier> findById(String id) {

        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        Document document = collection.find(
                eq("_id", new ObjectId(id))
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toSupplier(document));
    }

    @Override
    public Optional<Supplier> findByPhone(String phone) {

        Document document = collection.find(
                eq("phone", phone)
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toSupplier(document));
    }

    @Override
    public List<Supplier> findAll() {

        List<Supplier> suppliers = new ArrayList<>();

        for (Document document : collection.find()) {
            suppliers.add(toSupplier(document));
        }

        return suppliers;
    }

    @Override
    public List<Supplier> findByName(String name) {

        List<Supplier> suppliers = new ArrayList<>();

        for (Document document : collection.find(
                regex("name", name, "i")
        )) {
            suppliers.add(toSupplier(document));
        }

        return suppliers;
    }

    @Override
    public void update(Supplier supplier) {

        if (!ObjectId.isValid(supplier.getId())) {
            return;
        }

        Document update = new Document()
                .append("name", supplier.getName())
                .append("phone", supplier.getPhone())
                .append("email", supplier.getEmail())
                .append("address", supplier.getAddress())
                .append("status", supplier.getStatus());

        collection.updateOne(
                eq("_id", new ObjectId(supplier.getId())),
                new Document("$set", update)
        );
    }

    @Override
    public void deleteById(String id) {

        if (!ObjectId.isValid(id)) {
            return;
        }

        collection.deleteOne(
                eq("_id", new ObjectId(id))
        );
    }

    private Supplier toSupplier(Document document) {

        Supplier supplier = new Supplier();

        supplier.setId(
                document.getObjectId("_id").toHexString()
        );

        supplier.setName(
                document.getString("name")
        );

        supplier.setPhone(
                document.getString("phone")
        );

        supplier.setEmail(
                document.getString("email")
        );

        supplier.setAddress(
                document.getString("address")
        );

        supplier.setStatus(
                document.getString("status")
        );

        String createdAt = document.getString("createdAt");

        if (createdAt != null) {
            supplier.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        return supplier;
    }
}