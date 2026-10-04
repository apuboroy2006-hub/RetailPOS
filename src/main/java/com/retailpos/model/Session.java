package com.retailpos.model;

import java.time.LocalDateTime;

public class Session {

    private String id;
    private String userId;
    private String username;
    private String deviceId;

    private LocalDateTime loginAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime logoutAt;

    private String status;

    public Session() {
    }

    public Session(
            String userId,
            String username,
            String deviceId,
            LocalDateTime loginAt,
            LocalDateTime lastActivityAt,
            String status
    ) {
        this.userId = userId;
        this.username = username;
        this.deviceId = deviceId;
        this.loginAt = loginAt;
        this.lastActivityAt = lastActivityAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(LocalDateTime logoutAt) {
        this.logoutAt = logoutAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}