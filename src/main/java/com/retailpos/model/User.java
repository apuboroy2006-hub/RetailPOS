package com.retailpos.model;

import java.time.LocalDateTime;

public class User {

    private String id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private int failedOtpAttempts;
    private LocalDateTime otpLockedUntil;
    private int otpLockLevel;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(
            String username,
            String passwordHash,
            String fullName,
            String role,
            String status
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public int getFailedLoginAttempts() {
    return failedLoginAttempts;
}

public void setFailedLoginAttempts(int failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
}

public LocalDateTime getLockedUntil() {
    return lockedUntil;
}

public void setLockedUntil(LocalDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
}

public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
}

public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
}
public int getFailedOtpAttempts() {
    return failedOtpAttempts;
}

public void setFailedOtpAttempts(int failedOtpAttempts) {
    this.failedOtpAttempts = failedOtpAttempts;
}

public LocalDateTime getOtpLockedUntil() {
    return otpLockedUntil;
}

public void setOtpLockedUntil(LocalDateTime otpLockedUntil) {
    this.otpLockedUntil = otpLockedUntil;
}

public int getOtpLockLevel() {
    return otpLockLevel;
}

public void setOtpLockLevel(int otpLockLevel) {
    this.otpLockLevel = otpLockLevel;
}
}