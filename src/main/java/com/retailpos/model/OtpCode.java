package com.retailpos.model;

import java.time.LocalDateTime;

public class OtpCode {

    private String id;
    private String userId;
    private String username;
    private String deviceId;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean verified;
    private int attempts;

    public OtpCode() {
    }

    public OtpCode(
            String userId,
            String username,
            String deviceId,
            String code,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this.userId = userId;
        this.username = username;
        this.deviceId = deviceId;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.attempts = 0;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}