package com.retailpos.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;

public class RestoreService {

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

    public RestoreService() {

        database =
                DatabaseConfig.getDatabase();
    }

    public void restoreBackup(
            File backupFile
    ) throws IOException {

        String json =
                Files.readString(
                        backupFile.toPath()
                );

        Document backup =
                Document.parse(json);

        for (String collectionName :
                collections) {

            MongoCollection<Document>
                    collection =
                    database.getCollection(
                            collectionName
                    );

            collection.deleteMany(
                    new Document()
            );

           List<Document> documents =
        backup.getList(
                collectionName,
                Document.class
        );

            if (documents != null &&
                    !documents.isEmpty()) {

                collection.insertMany(
                        documents
                );
            }
        }
    }
}