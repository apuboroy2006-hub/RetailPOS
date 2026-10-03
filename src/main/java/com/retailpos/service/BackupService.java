package com.retailpos.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;

public class BackupService {

    private final MongoDatabase database;

   private final List<String> collections =
        Arrays.asList(
                "users",
                "settings",
                "products",
                "purchases",
                "purchase_items",
                "sales",
                "sale_items",
                "customers",
                "suppliers",
                "stock_transactions",
                "audit_logs"
        );

    public BackupService() {

        database =
                DatabaseConfig.getDatabase();
    }

    public void createBackup(
            File backupFile
    ) throws IOException {

        Document backup =
                new Document();

        backup.append(
                "database",
                "retail_pos"
        );

        backup.append(
                "createdAt",
                java.time.LocalDateTime
                        .now()
                        .toString()
        );

        for (String collectionName :
                collections) {

            MongoCollection<Document>
                    collection =
                    database.getCollection(
                            collectionName
                    );

            List<Document> documents =
                    collection
                            .find()
                            .into(
                                    new java.util.ArrayList<>()
                            );

            backup.append(
                    collectionName,
                    documents
            );
        }

        try (
                FileWriter writer =
                        new FileWriter(
                                backupFile
                        )
        ) {

            writer.write(
                    backup.toJson()
            );
        }
    }
}