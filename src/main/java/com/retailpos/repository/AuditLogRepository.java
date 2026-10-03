package com.retailpos.repository;

import java.util.List;

import com.retailpos.model.AuditLog;

public interface AuditLogRepository {

    void save(AuditLog auditLog);

    List<AuditLog> findAll();

    List<AuditLog> findByUsername(
            String username
    );
}