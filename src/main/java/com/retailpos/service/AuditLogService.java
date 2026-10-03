package com.retailpos.service;

import java.util.List;

import com.retailpos.model.AuditLog;
import com.retailpos.repository.AuditLogRepository;

public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository =
                auditLogRepository;
    }

    public void log(
            String userId,
            String username,
            String action,
            String module,
            String description
    ) {

        AuditLog auditLog =
                new AuditLog(
                        userId,
                        username,
                        action,
                        module,
                        description
                );

        auditLogRepository.save(
                auditLog
        );
    }

    public List<AuditLog> getAllLogs() {

        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByUsername(
            String username
    ) {

        return auditLogRepository
                .findByUsername(
                        username
                );
    }
}