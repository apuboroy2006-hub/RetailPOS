package com.retailpos.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.retailpos.model.Device;

public class MongoDeviceRepository implements DeviceRepository {

    private final MongoCollection<Document> collection;

    public MongoDeviceRepository(MongoDatabase database) {
        this.collection = database.getCollection("devices");
    }

    @Override
    public void save(Device device) {

        Document document = new Document();

        document.append("deviceId", device.getDeviceId());
        document.append("userId", device.getUserId());
        document.append("username", device.getUsername());
        document.append("deviceName", device.getDeviceName());
        document.append("operatingSystem", device.getOperatingSystem());
        document.append("osVersion", device.getOsVersion());
        document.append("ipAddress", device.getIpAddress());
        document.append("registeredAt", device.getRegisteredAt().toString());
        document.append("lastLoginAt", device.getLastLoginAt().toString());
        document.append("status", device.getStatus());

        collection.insertOne(document);

        ObjectId id = document.getObjectId("_id");

        device.setId(id.toString());
    }

    @Override
    public Optional<Device> findByDeviceId(String deviceId) {

        Document document =
                collection.find(eq("deviceId", deviceId)).first();

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
                collection.find(eq("userId", userId))) {

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

        if (device.getId() == null) {
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
                eq("_id", new ObjectId(device.getId())),
                new Document("$set", update)
        );
    }

    @Override
    public void deleteById(String id) {

        if (id == null) {
            return;
        }

        collection.deleteOne(
                eq("_id", new ObjectId(id))
        );
    }

    private Device toDevice(Document document) {

        Device device = new Device();

        ObjectId objectId = document.getObjectId("_id");

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