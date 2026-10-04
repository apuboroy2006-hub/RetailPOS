package com.retailpos.repository;

import java.util.Optional;

import com.retailpos.model.OtpCode;

public interface OtpRepository {

    void save(OtpCode otpCode);

    Optional<OtpCode> findLatestByUserIdAndDeviceId(
            String userId,
            String deviceId
    );

    void update(OtpCode otpCode);

    void deleteById(String id);
}