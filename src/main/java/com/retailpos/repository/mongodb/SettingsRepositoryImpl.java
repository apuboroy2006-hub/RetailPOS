package com.retailpos.repository.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Settings;
import com.retailpos.repository.SettingsRepository;

public class SettingsRepositoryImpl
        implements SettingsRepository {

    private final MongoCollection<Document> collection;

    public SettingsRepositoryImpl() {

        collection =
                DatabaseConfig
                        .getDatabase()
                        .getCollection("settings");
    }

    @Override
    public Settings getSettings() {

        Document document =
                collection.find().first();

        if (document == null) {
            return null;
        }

        String invoicePrefix =
                document.getString(
                        "invoicePrefix"
                );

        Boolean showTaxOnInvoice =
                document.getBoolean(
                        "showTaxOnInvoice"
                );

        if (invoicePrefix == null ||
                invoicePrefix.isBlank()) {

            invoicePrefix = "INV-";
        }

        if (showTaxOnInvoice == null) {

            showTaxOnInvoice = true;
        }

        return new Settings(
                document.getString("storeName"),
                document.getString("storeAddress"),
                document.getString("phone"),
                document.getString("email"),
                document.getString("currency"),
                document.getString("taxNumber"),
                invoicePrefix,
                showTaxOnInvoice
        );
    }

    @Override
    public void save(Settings settings) {

        Document document =
                new Document(
                        "storeName",
                        settings.getStoreName()
                )
                .append(
                        "storeAddress",
                        settings.getStoreAddress()
                )
                .append(
                        "phone",
                        settings.getPhone()
                )
                .append(
                        "email",
                        settings.getEmail()
                )
                .append(
                        "currency",
                        settings.getCurrency()
                )
                .append(
                        "taxNumber",
                        settings.getTaxNumber()
                )
                .append(
                        "invoicePrefix",
                        settings.getInvoicePrefix()
                )
                .append(
                        "showTaxOnInvoice",
                        settings.isShowTaxOnInvoice()
                );

        collection.deleteMany(
                new Document()
        );

        collection.insertOne(
                document
        );
    }
}