package com.retailpos.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {

    private static final String DATABASE_NAME = "retail_pos";

    private static final String MONGO_USER =
            "apuboroy2006_db_user";

    private static final String MONGO_PASSWORD =
            System.getenv("RETAILPOS_MONGO_PASSWORD");

    @Bean
    public MongoClient mongoClient() {

        if (MONGO_PASSWORD == null || MONGO_PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "RETAILPOS_MONGO_PASSWORD is not set."
            );
        }

        MongoCredential credential =
              MongoCredential.createScramSha1Credential(
                        MONGO_USER,
                        "admin",
                        MONGO_PASSWORD.toCharArray()
                );

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(
                                        "mongodb+srv://cluster0.wnoucsp.mongodb.net/"
                                                + DATABASE_NAME
                                                + "?appName=Cluster0"
                                )
                        )
                        .credential(credential)
                        .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(
            MongoClient mongoClient) {

        return new SimpleMongoClientDatabaseFactory(
                mongoClient,
                DATABASE_NAME
        );
    }

    @Bean
    public MongoTemplate mongoTemplate(
            MongoDatabaseFactory mongoDatabaseFactory) {

        return new MongoTemplate(mongoDatabaseFactory);
    }
}