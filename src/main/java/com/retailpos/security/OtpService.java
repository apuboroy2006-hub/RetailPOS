package com.retailpos.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import com.retailpos.model.OtpCode;
import com.retailpos.model.User;
import com.retailpos.repository.OtpRepository;
import com.retailpos.repository.UserRepository;

public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;

    private static final int MAX_ATTEMPTS = 5;

    private static final int BASE_LOCK_MINUTES = 15;

    public OtpService(
            OtpRepository otpRepository,
            UserRepository userRepository
    ) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    public OtpCode generateOtp(
            User user,
            String deviceId
    ) {

        String code =
                String.format(
                        "%06d",
                        secureRandom.nextInt(1_000_000)
                );

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime expiresAt =
                now.plusMinutes(
                        OTP_EXPIRY_MINUTES
                );

        OtpCode otpCode =
                new OtpCode(
                        user.getId(),
                        user.getUsername(),
                        deviceId,
                        code,
                        now,
                        expiresAt
                );

        otpRepository.save(otpCode);

        return otpCode;
    }

    public boolean isOtpLocked(User user) {

        if (user.getOtpLockedUntil() == null) {
            return false;
        }

        if (LocalDateTime.now()
                .isBefore(user.getOtpLockedUntil())) {

            return true;
        }

        // Lock period finished
        user.setOtpLockedUntil(null);
        user.setFailedOtpAttempts(0);

        userRepository.update(user);

        return false;
    }

    public boolean verifyOtp(
            User user,
            String deviceId,
            String enteredCode
    ) {

        if (isOtpLocked(user)) {
            return false;
        }

        if (enteredCode == null
                || enteredCode.isBlank()) {

            return false;
        }

        Optional<OtpCode> optionalOtp =
                otpRepository
                        .findLatestByUserIdAndDeviceId(
                                user.getId(),
                                deviceId
                        );

        if (optionalOtp.isEmpty()) {
            return false;
        }

        OtpCode otpCode =
                optionalOtp.get();

        if (otpCode.isVerified()) {
            return false;
        }

        if (otpCode.getAttempts()
                >= MAX_ATTEMPTS) {

            return false;
        }

        if (LocalDateTime.now()
                .isAfter(
                        otpCode.getExpiresAt()
                )) {

            return false;
        }

        otpCode.setAttempts(
                otpCode.getAttempts() + 1
        );

        // Wrong OTP
        if (!otpCode.getCode()
                .equals(enteredCode.trim())) {

            otpRepository.update(otpCode);

            registerFailedOtp(user);

            return false;
        }

        // Correct OTP
        otpCode.setVerified(true);

        otpRepository.update(otpCode);

        user.setFailedOtpAttempts(0);
        user.setOtpLockedUntil(null);

        userRepository.update(user);

        return true;
    }

    private void registerFailedOtp(User user) {

        int failedAttempts =
                user.getFailedOtpAttempts() + 1;

        user.setFailedOtpAttempts(
                failedAttempts
        );

        if (failedAttempts >= MAX_ATTEMPTS) {

            int lockLevel =
                    user.getOtpLockLevel() + 1;

            user.setOtpLockLevel(
                    lockLevel
            );

            int lockMinutes =
                    BASE_LOCK_MINUTES
                    * (int) Math.pow(2, lockLevel - 1);

            user.setOtpLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(lockMinutes)
            );

            user.setFailedOtpAttempts(0);
        }

        userRepository.update(user);
    }
}