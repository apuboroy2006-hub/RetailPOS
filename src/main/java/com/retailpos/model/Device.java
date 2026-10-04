package com.retailpos.model;

import java.time.LocalDateTime;

public class Device {

    private String id;
    private String deviceId;
    private String userId;
    private String username;
    private String deviceName;
    private String operatingSystem;
    private String osVersion;
    private String ipAddress;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private String status;

    public Device() {
    }

    public Device(
            String deviceId,
            String userId,
            String username,
            String deviceName,
            String operatingSystem,
            String osVersion,
            String ipAddress
    ) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.username = username;
        this.deviceName = deviceName;
        this.operatingSystem = operatingSystem;
        this.osVersion = osVersion;
        this.ipAddress = ipAddress;
        this.registeredAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}