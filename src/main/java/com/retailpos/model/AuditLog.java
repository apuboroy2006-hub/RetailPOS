package com.retailpos.model;

import java.time.LocalDateTime;

public class AuditLog {

    private String id;
    private String userId;
    private String username;
    private String action;
    private String module;
    private String description;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(
            String userId,
            String username,
            String action,
            String module,
            String description
    ) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.module = module;
        this.description = description;
        this.createdAt = LocalDateTime.now();
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}