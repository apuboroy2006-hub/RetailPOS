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
import static com.mongodb.client.model.Sorts.descending;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Session;
import com.retailpos.repository.SessionRepository;

public class SessionRepositoryImpl implements SessionRepository {

    private final MongoCollection<Document> collection;

    public SessionRepositoryImpl() {

        MongoDatabase database = DatabaseConfig.getDatabase();

        collection = database.getCollection("sessions");
    }

    @Override
    public void save(Session session) {

        Document document = new Document()
                .append("userId", session.getUserId())
                .append("username", session.getUsername())
                .append("deviceId", session.getDeviceId())
                .append("loginAt",
                        session.getLoginAt() == null
                                ? null
                                : session.getLoginAt().toString())
                .append("lastActivityAt",
                        session.getLastActivityAt() == null
                                ? null
                                : session.getLastActivityAt().toString())
                .append("logoutAt",
                        session.getLogoutAt() == null
                                ? null
                                : session.getLogoutAt().toString())
                .append("status", session.getStatus());

        collection.insertOne(document);

        ObjectId id = document.getObjectId("_id");

        if (id != null) {
            session.setId(id.toHexString());
        }
    }

    @Override
    public Optional<Session> findById(String id) {

        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        try {

            Document document =
                    collection.find(
                            eq("_id", new ObjectId(id))
                    ).first();

            if (document == null) {
                return Optional.empty();
            }

            return Optional.of(toSession(document));

        } catch (IllegalArgumentException e) {

            return Optional.empty();
        }
    }

    @Override
    public List<Session> findActiveSessions() {

        List<Session> sessions = new ArrayList<>();

        for (Document document :
                collection.find(
                        eq("status", "ACTIVE")
                ).sort(descending("loginAt"))) {

            sessions.add(toSession(document));
        }

        return sessions;
    }

    @Override
    public List<Session> findByUsername(String username) {

        List<Session> sessions = new ArrayList<>();

        if (username == null || username.isBlank()) {
            return sessions;
        }

        for (Document document :
                collection.find(
                        eq("username", username)
                ).sort(descending("loginAt"))) {

            sessions.add(toSession(document));
        }

        return sessions;
    }

    @Override
    public void update(Session session) {

        if (session == null
                || session.getId() == null
                || session.getId().isBlank()) {
            return;
        }

        try {

            Document document = new Document()
                    .append("userId", session.getUserId())
                    .append("username", session.getUsername())
                    .append("deviceId", session.getDeviceId())
                    .append("loginAt",
                            session.getLoginAt() == null
                                    ? null
                                    : session.getLoginAt().toString())
                    .append("lastActivityAt",
                            session.getLastActivityAt() == null
                                    ? null
                                    : session.getLastActivityAt().toString())
                    .append("logoutAt",
                            session.getLogoutAt() == null
                                    ? null
                                    : session.getLogoutAt().toString())
                    .append("status", session.getStatus());

            collection.replaceOne(
                    eq("_id", new ObjectId(session.getId())),
                    document
            );

        } catch (IllegalArgumentException e) {

            // Invalid MongoDB ObjectId
        }
    }

    @Override
    public void deleteById(String id) {

        if (id == null || id.isBlank()) {
            return;
        }

        try {

            collection.deleteOne(
                    eq("_id", new ObjectId(id))
            );

        } catch (IllegalArgumentException e) {

            // Invalid MongoDB ObjectId
        }
    }

    private Session toSession(Document document) {

        Session session = new Session();

        ObjectId id = document.getObjectId("_id");

        if (id != null) {
            session.setId(id.toHexString());
        }

        session.setUserId(
                document.getString("userId")
        );

        session.setUsername(
                document.getString("username")
        );

        session.setDeviceId(
                document.getString("deviceId")
        );

        String loginAt =
                document.getString("loginAt");

        if (loginAt != null
                && !loginAt.isBlank()) {

            session.setLoginAt(
                    LocalDateTime.parse(loginAt)
            );
        }

        String lastActivityAt =
                document.getString("lastActivityAt");

        if (lastActivityAt != null
                && !lastActivityAt.isBlank()) {

            session.setLastActivityAt(
                    LocalDateTime.parse(lastActivityAt)
            );
        }

        String logoutAt =
                document.getString("logoutAt");

        if (logoutAt != null
                && !logoutAt.isBlank()) {

            session.setLogoutAt(
                    LocalDateTime.parse(logoutAt)
            );
        }

        session.setStatus(
                document.getString("status")
        );

        return session;
    }
}