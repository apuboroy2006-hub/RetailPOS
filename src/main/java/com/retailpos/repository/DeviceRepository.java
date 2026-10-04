package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Device;

public interface DeviceRepository {

    void save(Device device);

    Optional<Device> findByDeviceId(String deviceId);

    Optional<Device> findByUserIdAndDeviceId(
            String userId,
            String deviceId
    );

    List<Device> findByUserId(String userId);

    List<Device> findAll();

    void update(Device device);

    void deleteById(String id);
}