package com.retailpos.security;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.retailpos.model.Device;
import com.retailpos.model.User;
import com.retailpos.repository.DeviceRepository;
import com.retailpos.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository;
    private final DeviceService deviceService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    public AuthService(
            UserRepository userRepository,
            DeviceRepository deviceRepository
    ) {
        this.userRepository = userRepository;
        this.deviceService =
                new DeviceService(deviceRepository);
    }

    public Optional<User> login(
            String username,
            String password
    ) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            return Optional.empty();
        }

        Optional<User> optionalUser =
                userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();

        if (!"ACTIVE".equals(user.getStatus())) {
            return Optional.empty();
        }

        // Check temporary account lock
        if (user.getLockedUntil() != null) {

            if (LocalDateTime.now()
                    .isBefore(user.getLockedUntil())) {

                return Optional.empty();
            }

            // Lock period finished
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);

            userRepository.update(user);
        }

        boolean passwordMatches =
                BCrypt.checkpw(
                        password,
                        user.getPasswordHash()
                );

        if (!passwordMatches) {

            int failedAttempts =
                    user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(
                    failedAttempts
            );

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {

                user.setLockedUntil(
                        LocalDateTime.now()
                                .plusMinutes(LOCK_MINUTES)
                );
            }

            userRepository.update(user);

            return Optional.empty();
        }

        // Successful login
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(
                LocalDateTime.now()
        );

        userRepository.update(user);

        return Optional.of(user);
    }

    public boolean isCurrentDeviceKnown(User user) {

        Device currentDevice =
                deviceService.getCurrentDevice(user);

        return deviceService.isKnownDevice(
                user.getId(),
                currentDevice.getDeviceId()
        );
    }

    public Device getCurrentDevice(User user) {

        return deviceService.getCurrentDevice(user);
    }

    public Device registerCurrentDevice(User user) {

        Device device =
                deviceService.getCurrentDevice(user);

        return deviceService.registerDevice(device);
    }

    public void updateDeviceLastLogin(User user) {

        Device currentDevice =
                deviceService.getCurrentDevice(user);

        Optional<Device> existingDevice =
                deviceService.findDevice(
                        user.getId(),
                        currentDevice.getDeviceId()
                );

        if (existingDevice.isPresent()) {

            deviceService.updateLastLogin(
                    existingDevice.get()
            );
        }
    }
}