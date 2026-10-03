package com.retailpos.repository.mongodb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.AuditLog;
import com.retailpos.repository.AuditLogRepository;

public class AuditLogRepositoryImpl
        implements AuditLogRepository {

    private final MongoCollection<Document> collection;

    public AuditLogRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        collection =
                database.getCollection(
                        "audit_logs"
                );
    }

    @Override
    public void save(AuditLog auditLog) {

        Document document =
                new Document()
                        .append(
                                "userId",
                                auditLog.getUserId()
                        )
                        .append(
                                "username",
                                auditLog.getUsername()
                        )
                        .append(
                                "action",
                                auditLog.getAction()
                        )
                        .append(
                                "module",
                                auditLog.getModule()
                        )
                        .append(
                                "description",
                                auditLog.getDescription()
                        )
                        .append(
                                "createdAt",
                                auditLog.getCreatedAt()
                                        .toString()
                        );

        collection.insertOne(document);

        auditLog.setId(
                document
                        .getObjectId("_id")
                        .toHexString()
        );
    }

    @Override
    public List<AuditLog> findAll() {

        List<AuditLog> logs =
                new ArrayList<>();

        for (Document document :
                collection.find()) {

            logs.add(
                    toAuditLog(document)
            );
        }

        return logs;
    }

    @Override
    public List<AuditLog> findByUsername(
            String username
    ) {

        List<AuditLog> logs =
                new ArrayList<>();

        for (Document document :
                collection.find(
                        eq("username", username)
                )) {

            logs.add(
                    toAuditLog(document)
            );
        }

        return logs;
    }

    private AuditLog toAuditLog(
            Document document
    ) {

        AuditLog auditLog =
                new AuditLog();

        ObjectId objectId =
                document.getObjectId("_id");

        if (objectId != null) {

            auditLog.setId(
                    objectId.toHexString()
            );
        }

        auditLog.setUserId(
                document.getString("userId")
        );

        auditLog.setUsername(
                document.getString("username")
        );

        auditLog.setAction(
                document.getString("action")
        );

        auditLog.setModule(
                document.getString("module")
        );

        auditLog.setDescription(
                document.getString("description")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {

            auditLog.setCreatedAt(
                    LocalDateTime.parse(
                            createdAt
                    )
            );
        }

        return auditLog;
    }
}