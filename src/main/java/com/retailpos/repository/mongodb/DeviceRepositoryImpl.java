package com.retailpos.repository.mongodb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.Device;
import com.retailpos.repository.DeviceRepository;

public class DeviceRepositoryImpl implements DeviceRepository {

    private final MongoCollection<Document> collection;

    public DeviceRepositoryImpl() {
        MongoDatabase database = DatabaseConfig.getDatabase();
        this.collection = database.getCollection("devices");
    }

    @Override
    public void save(Device device) {

        Document document = new Document()
                .append("deviceId", device.getDeviceId())
                .append("userId", device.getUserId())
                .append("username", device.getUsername())
                .append("deviceName", device.getDeviceName())
                .append("operatingSystem", device.getOperatingSystem())
                .append("osVersion", device.getOsVersion())
                .append("ipAddress", device.getIpAddress())
                .append("registeredAt", device.getRegisteredAt().toString())
                .append("lastLoginAt", device.getLastLoginAt().toString())
                .append("status", device.getStatus());

        collection.insertOne(document);

        device.setId(
                document.getObjectId("_id").toString()
        );
    }

    @Override
    public Optional<Device> findByDeviceId(String deviceId) {

        Document document = collection.find(
                new Document("deviceId", deviceId)
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toDevice(document));
    }

    @Override
    public Optional<Device> findByUserIdAndDeviceId(
            String userId,
            String deviceId
    ) {

        Document document = collection.find(
                new Document("userId", userId)
                        .append("deviceId", deviceId)
        ).first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(toDevice(document));
    }

    @Override
    public List<Device> findByUserId(String userId) {

        List<Device> devices = new ArrayList<>();

        for (Document document :
                collection.find(new Document("userId", userId))) {

            devices.add(toDevice(document));
        }

        return devices;
    }

    @Override
    public List<Device> findAll() {

        List<Device> devices = new ArrayList<>();

        for (Document document : collection.find()) {

            devices.add(toDevice(document));
        }

        return devices;
    }

    @Override
    public void update(Device device) {

        if (!ObjectId.isValid(device.getId())) {
            return;
        }

        Document update = new Document()
                .append("deviceId", device.getDeviceId())
                .append("userId", device.getUserId())
                .append("username", device.getUsername())
                .append("deviceName", device.getDeviceName())
                .append("operatingSystem", device.getOperatingSystem())
                .append("osVersion", device.getOsVersion())
                .append("ipAddress", device.getIpAddress())
                .append("registeredAt", device.getRegisteredAt().toString())
                .append("lastLoginAt", device.getLastLoginAt().toString())
                .append("status", device.getStatus());

        collection.updateOne(
                new Document("_id", new ObjectId(device.getId())),
                new Document("$set", update)
        );
    }

    @Override
    public void deleteById(String id) {

        if (!ObjectId.isValid(id)) {
            return;
        }

        collection.deleteOne(
                new Document("_id", new ObjectId(id))
        );
    }

    private Device toDevice(Document document) {

        Device device = new Device();

        ObjectId objectId =
                document.getObjectId("_id");

        if (objectId != null) {
            device.setId(objectId.toString());
        }

        device.setDeviceId(
                document.getString("deviceId")
        );

        device.setUserId(
                document.getString("userId")
        );

        device.setUsername(
                document.getString("username")
        );

        device.setDeviceName(
                document.getString("deviceName")
        );

        device.setOperatingSystem(
                document.getString("operatingSystem")
        );

        device.setOsVersion(
                document.getString("osVersion")
        );

        device.setIpAddress(
                document.getString("ipAddress")
        );

        String registeredAt =
                document.getString("registeredAt");

        if (registeredAt != null) {
            device.setRegisteredAt(
                    LocalDateTime.parse(registeredAt)
            );
        }

        String lastLoginAt =
                document.getString("lastLoginAt");

        if (lastLoginAt != null) {
            device.setLastLoginAt(
                    LocalDateTime.parse(lastLoginAt)
            );
        }

        device.setStatus(
                document.getString("status")
        );

        return device;
    }
}