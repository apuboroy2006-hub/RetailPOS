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
import com.retailpos.model.User;
import com.retailpos.repository.UserRepository;

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
                .append("email", user.getEmail())
                .append("role", user.getRole())
                .append("status", user.getStatus())
                .append("createdAt", user.getCreatedAt().toString())
                .append("failedLoginAttempts",
                        user.getFailedLoginAttempts())
               .append("lockedUntil",
        user.getLockedUntil() == null
                ? null
                : user.getLockedUntil().toString())
.append("lastLoginAt",
        user.getLastLoginAt() == null
                ? null
                : user.getLastLoginAt().toString())
.append("failedOtpAttempts",
        user.getFailedOtpAttempts())
.append("otpLockedUntil",
        user.getOtpLockedUntil() == null
                ? null
                : user.getOtpLockedUntil().toString())
.append("otpLockLevel",
        user.getOtpLockLevel());

        collection.insertOne(document);

        user.setId(
                document.getObjectId("_id").toString()
        );
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
            .append("email", user.getEmail())
            .append("role", user.getRole())
            .append("status", user.getStatus())

            // Password login security
            .append("failedLoginAttempts",
                    user.getFailedLoginAttempts())

            .append("lockedUntil",
                    user.getLockedUntil() == null
                            ? null
                            : user.getLockedUntil().toString())

            .append("lastLoginAt",
                    user.getLastLoginAt() == null
                            ? null
                            : user.getLastLoginAt().toString())

            // OTP security
            .append("failedOtpAttempts",
                    user.getFailedOtpAttempts())

            .append("otpLockedUntil",
                    user.getOtpLockedUntil() == null
                            ? null
                            : user.getOtpLockedUntil().toString())

            .append("otpLockLevel",
                    user.getOtpLockLevel());

    collection.updateOne(
            new Document(
                    "_id",
                    new ObjectId(user.getId())
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

    private User toUser(Document document) {

        User user = new User();

        user.setId(
                document.getObjectId("_id").toString()
        );

        user.setUsername(
                document.getString("username")
        );

        user.setPasswordHash(
                document.getString("passwordHash")
        );

        user.setFullName(
                document.getString("fullName")
        );

        user.setEmail(
                document.getString("email")
        );

        user.setRole(
                document.getString("role")
        );

        user.setStatus(
                document.getString("status")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {
            user.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        Object failedAttempts =
                document.get("failedLoginAttempts");

        if (failedAttempts instanceof Number) {
            user.setFailedLoginAttempts(
                    ((Number) failedAttempts).intValue()
            );
        } else {
            user.setFailedLoginAttempts(0);
        }

        String lockedUntil =
                document.getString("lockedUntil");

        if (lockedUntil != null
                && !lockedUntil.isBlank()) {

            user.setLockedUntil(
                    LocalDateTime.parse(lockedUntil)
            );
        }

        String lastLoginAt =
                document.getString("lastLoginAt");

        if (lastLoginAt != null
                && !lastLoginAt.isBlank()) {

            user.setLastLoginAt(
                    LocalDateTime.parse(lastLoginAt)
            );
        }
        Object failedOtpAttempts =
        document.get("failedOtpAttempts");

if (failedOtpAttempts instanceof Number) {
    user.setFailedOtpAttempts(
            ((Number) failedOtpAttempts).intValue()
    );
} else {
    user.setFailedOtpAttempts(0);
}

String otpLockedUntil =
        document.getString("otpLockedUntil");

if (otpLockedUntil != null
        && !otpLockedUntil.isBlank()) {

    user.setOtpLockedUntil(
            LocalDateTime.parse(otpLockedUntil)
    );
}

Object otpLockLevel =
        document.get("otpLockLevel");

if (otpLockLevel instanceof Number) {
    user.setOtpLockLevel(
            ((Number) otpLockLevel).intValue()
    );
} else {
    user.setOtpLockLevel(0);
}
        return user;
    }
}