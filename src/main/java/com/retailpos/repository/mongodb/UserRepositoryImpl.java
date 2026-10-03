package com.retailpos.repository.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.User;
import com.retailpos.repository.UserRepository;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final MongoCollection<Document> collection;

    public UserRepositoryImpl() {
        MongoDatabase database = DatabaseConfig.getDatabase();
        this.collection = database.getCollection("users");
    }

    @Override
    public void save(User user) {

        Document document = new Document()
                .append("username", user.getUsername())
                .append("passwordHash", user.getPasswordHash())
                .append("fullName", user.getFullName())
                .append("role", user.getRole())
                .append("status", user.getStatus())
                .append("createdAt", user.getCreatedAt().toString());

        collection.insertOne(document);

        user.setId(document.getObjectId("_id").toString());
    }

    @Override
    public Optional<User> findByUsername(String username) {

        Document document = collection.find(
                new Document("username", username)
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toUser(document));
    }

    @Override
    public Optional<User> findById(String id) {

        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        Document document = collection.find(
                new Document("_id", new ObjectId(id))
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toUser(document));
    }

    @Override
    public List<User> findAll() {

        List<User> users = new ArrayList<>();

        for (Document document : collection.find()) {
            users.add(toUser(document));
        }

        return users;
    }

    @Override
    public void update(User user) {

        if (!ObjectId.isValid(user.getId())) {
            return;
        }

        Document update = new Document()
                .append("username", user.getUsername())
                .append("passwordHash", user.getPasswordHash())
                .append("fullName", user.getFullName())
                .append("role", user.getRole())
                .append("status", user.getStatus());

        collection.updateOne(
                new Document("_id", new ObjectId(user.getId())),
                new Document("$set", update)
        );
    }

    @Override
    public void deleteById(String id) {

        if (!ObjectId.isValid(id)) {
            return;
        }

        collection.deleteOne(
                new Document("_id", new ObjectId(id))
        );
    }

    private User toUser(Document document) {

        User user = new User();

        user.setId(document.getObjectId("_id").toString());
        user.setUsername(document.getString("username"));
        user.setPasswordHash(document.getString("passwordHash"));
        user.setFullName(document.getString("fullName"));
        user.setRole(document.getString("role"));
        user.setStatus(document.getString("status"));

        String createdAt = document.getString("createdAt");

        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt));
        }

        return user;
    }
}