package com.retailpos.repository.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.SaleItem;
import com.retailpos.repository.SaleItemRepository;

public class SaleItemRepositoryImpl implements SaleItemRepository {

    private final MongoCollection<Document> collection;

    public SaleItemRepositoryImpl() {
        MongoDatabase database = DatabaseConfig.getDatabase();
        collection = database.getCollection("sale_items");
    }

    @Override
    public void save(SaleItem saleItem) {

        Document document = new Document()
                .append("saleId", saleItem.getSaleId())
                .append("productId", saleItem.getProductId())
                .append("productName", saleItem.getProductName())
                .append("sku", saleItem.getSku())
                .append("quantity", saleItem.getQuantity())
                .append("unitPrice", saleItem.getUnitPrice())
                .append("discount", saleItem.getDiscount())
                .append("total", saleItem.getTotal());

        collection.insertOne(document);

        saleItem.setId(
                document.getObjectId("_id").toHexString()
        );
    }

    @Override
    public List<SaleItem> findBySaleId(String saleId) {

        List<SaleItem> items = new ArrayList<>();

        for (Document document : collection.find(
                eq("saleId", saleId)
        )) {
            items.add(toSaleItem(document));
        }

        return items;
    }

    @Override
    public void deleteBySaleId(String saleId) {

        collection.deleteMany(
                eq("saleId", saleId)
        );
    }

    private SaleItem toSaleItem(Document document) {

        SaleItem item = new SaleItem();

        item.setId(
                document.getObjectId("_id").toHexString()
        );

        item.setSaleId(
                document.getString("saleId")
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
                document.getInteger("quantity", 0)
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