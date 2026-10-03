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
import com.retailpos.model.Customer;
import com.retailpos.repository.CustomerRepository;

public class CustomerRepositoryImpl
        implements CustomerRepository {

    private final MongoCollection<Document> collection;

    public CustomerRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        this.collection =
                database.getCollection("customers");
    }

    @Override
    public void save(Customer customer) {

        Document document = new Document()
                .append("name", customer.getName())
                .append("phone", customer.getPhone())
                .append("email", customer.getEmail())
                .append("address", customer.getAddress())
                .append("status", customer.getStatus())
                .append(
                        "createdAt",
                        customer.getCreatedAt().toString()
                );

        collection.insertOne(document);

        customer.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );
    }

    @Override
    public Optional<Customer> findById(String id) {

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
                toCustomer(document)
        );
    }

    @Override
    public Optional<Customer> findByPhone(
            String phone
    ) {

        Document document =
                collection.find(
                        new Document(
                                "phone",
                                phone
                        )
                ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                toCustomer(document)
        );
    }

    @Override
    public List<Customer> findAll() {

        List<Customer> customers =
                new ArrayList<>();

        for (Document document :
                collection.find()) {

            customers.add(
                    toCustomer(document)
            );
        }

        return customers;
    }

    @Override
    public List<Customer> findByName(
            String name
    ) {

        List<Customer> customers =
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

            customers.add(
                    toCustomer(document)
            );
        }

        return customers;
    }

    @Override
    public void update(Customer customer) {

        if (!ObjectId.isValid(
                customer.getId()
        )) {
            return;
        }

        Document update =
                new Document()
                        .append(
                                "name",
                                customer.getName()
                        )
                        .append(
                                "phone",
                                customer.getPhone()
                        )
                        .append(
                                "email",
                                customer.getEmail()
                        )
                        .append(
                                "address",
                                customer.getAddress()
                        )
                        .append(
                                "status",
                                customer.getStatus()
                        );

        collection.updateOne(
                new Document(
                        "_id",
                        new ObjectId(
                                customer.getId()
                        )
                ),
                new Document(
                        "$set",
                        update
                )
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

    private Customer toCustomer(
            Document document
    ) {

        Customer customer =
                new Customer();

        customer.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );

        customer.setName(
                document.getString("name")
        );

        customer.setPhone(
                document.getString("phone")
        );

        customer.setEmail(
                document.getString("email")
        );

        customer.setAddress(
                document.getString("address")
        );

        customer.setStatus(
                document.getString("status")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {

            customer.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        return customer;
    }
}