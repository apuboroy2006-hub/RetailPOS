package com.retailpos.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConfig {

    private static final String DATABASE_NAME = "retail_pos";

    private static final String MONGO_USER =
        System.getenv("RETAILPOS_MONGO_USER");

    private static final String MONGO_PASSWORD =
            System.getenv("RETAILPOS_MONGO_PASSWORD");

    private static final MongoCredential CREDENTIAL =
            MongoCredential.createCredential(
                    MONGO_USER,
                    "admin",
                    MONGO_PASSWORD.toCharArray()
            );

       private static final MongoClient mongoClient =
            MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString(
                                    new ConnectionString(
                                            System.getenv("RETAILPOS_MONGO_URI")
                                    )
                            )
                            .credential(CREDENTIAL)
                            .build()
            );

    private DatabaseConfig() {
    }

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static void close() {
        mongoClient.close();
    }
}