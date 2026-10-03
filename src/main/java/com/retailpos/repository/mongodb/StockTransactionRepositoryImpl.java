package com.retailpos.repository.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.StockTransaction;
import com.retailpos.repository.StockTransactionRepository;

import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionRepositoryImpl
        implements StockTransactionRepository {

    private final MongoCollection<Document> collection;

    public StockTransactionRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        collection =
                database.getCollection(
                        "stock_transactions"
                );
    }

    @Override
    public void save(StockTransaction transaction) {

        Document document =
                new Document()
                        .append(
                                "productId",
                                transaction.getProductId()
                        )
                        .append(
                                "productName",
                                transaction.getProductName()
                        )
                        .append(
                                "type",
                                transaction.getType()
                        )
                        .append(
                                "quantity",
                                transaction.getQuantity()
                        )
                        .append(
                                "previousStock",
                                transaction.getPreviousStock()
                        )
                        .append(
                                "newStock",
                                transaction.getNewStock()
                        )
                        .append(
                                "reason",
                                transaction.getReason()
                        )
                        .append(
                                "performedBy",
                                transaction.getPerformedBy()
                        )
                        .append(
                                "createdAt",
                                transaction
                                        .getCreatedAt()
                                        .toString()
                        );

        collection.insertOne(document);

        transaction.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );
    }

    @Override
    public List<StockTransaction> findAll() {

        List<StockTransaction> transactions =
                new ArrayList<>();

        for (Document document :
                collection.find()) {

            transactions.add(
                    toTransaction(document)
            );
        }

        return transactions;
    }

    @Override
    public List<StockTransaction> findByProductId(
            String productId
    ) {

        List<StockTransaction> transactions =
                new ArrayList<>();

        Document filter =
                new Document(
                        "productId",
                        productId
                );

        for (Document document :
                collection.find(filter)) {

            transactions.add(
                    toTransaction(document)
            );
        }

        return transactions;
    }

    private StockTransaction toTransaction(
            Document document
    ) {

        StockTransaction transaction =
                new StockTransaction();

        transaction.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );

        transaction.setProductId(
                document.getString("productId")
        );

        transaction.setProductName(
                document.getString("productName")
        );

        transaction.setType(
                document.getString("type")
        );

        transaction.setQuantity(
                document.getInteger("quantity")
        );

        transaction.setPreviousStock(
                document.getInteger(
                        "previousStock"
                )
        );

        transaction.setNewStock(
                document.getInteger("newStock")
        );

        transaction.setReason(
                document.getString("reason")
        );

        transaction.setPerformedBy(
                document.getString("performedBy")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {

            transaction.setCreatedAt(
                    LocalDateTime.parse(createdAt)
            );
        }

        return transaction;
    }
}