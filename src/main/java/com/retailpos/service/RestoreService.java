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

    private final List<String> collections = Arrays.asList(
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
        database = DatabaseConfig.getDatabase();
    }

    public void restoreBackup(File backupFile) throws IOException {

        String json = Files.readString(backupFile.toPath());
        Document backup;

        try {
            backup = Document.parse(json);
        } catch (RuntimeException ex) {
            throw new IOException("Invalid backup JSON file.", ex);
        }

        if (!"retail_pos".equals(backup.getString("database"))) {
            throw new IOException(
                    "Invalid backup: database name does not match Retail POS."
            );
        }

        // Validate every collection before deleting any existing data.
        for (String collectionName : collections) {
            Object value = backup.get(collectionName);

            if (!(value instanceof List<?> documents)) {
                throw new IOException(
                        "Invalid backup: missing or invalid collection: "
                                + collectionName
                );
            }

            for (Object item : documents) {
                if (!(item instanceof Document)) {
                    throw new IOException(
                            "Invalid backup: invalid document in collection: "
                                    + collectionName
                    );
                }
            }
        }

        // All collections passed validation; restoration may now begin.
        for (String collectionName : collections) {
            MongoCollection<Document> collection =
                    database.getCollection(collectionName);

            collection.deleteMany(new Document());

            @SuppressWarnings("unchecked")
            List<Document> documents =
                    (List<Document>) backup.get(collectionName);

            if (!documents.isEmpty()) {
                collection.insertMany(documents);
            }
        }
    }
}
