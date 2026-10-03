package com.retailpos.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConfig {

    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "retail_pos";

    private static final MongoClient mongoClient =
            MongoClients.create(MONGO_URI);

    private DatabaseConfig() {
    }

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static void close() {
        mongoClient.close();
    }
}