package com.retailpos.repository.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.PurchaseItem;
import com.retailpos.repository.PurchaseItemRepository;

public class PurchaseItemRepositoryImpl
        implements PurchaseItemRepository {

    private final MongoCollection<Document> collection;

    public PurchaseItemRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        collection =
                database.getCollection("purchase_items");
    }

    @Override
    public void save(
            PurchaseItem purchaseItem
    ) {

        Document document =
                new Document();

        document.append(
                "purchaseId",
                purchaseItem.getPurchaseId()
        );

        document.append(
                "productId",
                purchaseItem.getProductId()
        );

        document.append(
                "productName",
                purchaseItem.getProductName()
        );

        document.append(
                "sku",
                purchaseItem.getSku()
        );

        document.append(
                "quantity",
                purchaseItem.getQuantity()
        );

        document.append(
                "unitPrice",
                purchaseItem.getUnitPrice()
        );

        document.append(
                "discount",
                purchaseItem.getDiscount()
        );

        document.append(
                "total",
                purchaseItem.getTotal()
        );

        collection.insertOne(document);

        purchaseItem.setId(
                document.getObjectId("_id")
                        .toHexString()
        );
    }

    @Override
    public List<PurchaseItem> findByPurchaseId(
            String purchaseId
    ) {

        List<PurchaseItem> items =
                new ArrayList<>();

        for (
                Document document :
                collection.find(
                        new Document(
                                "purchaseId",
                                purchaseId
                        )
                )
        ) {

            items.add(
                    mapDocumentToPurchaseItem(
                            document
                    )
            );
        }

        return items;
    }

    @Override
    public void deleteByPurchaseId(
            String purchaseId
    ) {

        collection.deleteMany(
                new Document(
                        "purchaseId",
                        purchaseId
                )
        );
    }

    private PurchaseItem mapDocumentToPurchaseItem(
            Document document
    ) {

        PurchaseItem item =
                new PurchaseItem();

        ObjectId objectId =
                document.getObjectId("_id");

        if (objectId != null) {

            item.setId(
                    objectId.toHexString()
            );
        }

        item.setPurchaseId(
                document.getString("purchaseId")
        );

        item.setProductId(
                document.getString("productId")
        );

        item.setProductName(
                document.getString("productName")
        );

        item.setSku(
                document.getString("sku")
        );

        item.setQuantity(
                document.getInteger("quantity")
        );

        item.setUnitPrice(
                document.getDouble("unitPrice")
        );

        item.setDiscount(
                document.getDouble("discount")
        );

        item.setTotal(
                document.getDouble("total")
        );

        return item;
    }
}