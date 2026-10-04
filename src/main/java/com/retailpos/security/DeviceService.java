package com.retailpos.security;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Optional;

import com.retailpos.model.Device;
import com.retailpos.model.User;
import com.retailpos.repository.DeviceRepository;

public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device getCurrentDevice(User user) {

        String deviceId = generateDeviceId();

        String deviceName = getDeviceName();

        String operatingSystem =
                System.getProperty("os.name", "Unknown");

        String osVersion =
                System.getProperty("os.version", "Unknown");

        String ipAddress =
                getLocalIpAddress();

        return new Device(
                deviceId,
                user.getId(),
                user.getUsername(),
                deviceName,
                operatingSystem,
                osVersion,
                ipAddress
        );
    }

    public boolean isKnownDevice(
            String userId,
            String deviceId
    ) {

        Optional<Device> device =
                deviceRepository.findByUserIdAndDeviceId(
                        userId,
                        deviceId
                );

        return device.isPresent()
                && "ACTIVE".equals(device.get().getStatus());
    }

    public Device registerDevice(Device device) {

        deviceRepository.save(device);

        return device;
    }

    public void updateLastLogin(Device device) {

        device.setLastLoginAt(LocalDateTime.now());

        deviceRepository.update(device);
    }

    public Optional<Device> findDevice(
            String userId,
            String deviceId
    ) {

        return deviceRepository.findByUserIdAndDeviceId(
                userId,
                deviceId
        );
    }

    private String generateDeviceId() {

        try {

            String rawData =
                    getMacAddress()
                    + "|"
                    + getDeviceName()
                    + "|"
                    + System.getProperty("os.name")
                    + "|"
                    + System.getProperty("os.version");

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawData.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to generate device ID",
                    e
            );
        }
    }

    private String getDeviceName() {

        try {

            return InetAddress
                    .getLocalHost()
                    .getHostName();

        } catch (Exception e) {

            return "UNKNOWN-DEVICE";
        }
    }

    private String getLocalIpAddress() {

        try {

            InetAddress localHost =
                    InetAddress.getLocalHost();

            if (!localHost.isLoopbackAddress()) {
                return localHost.getHostAddress();
            }

            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                NetworkInterface networkInterface =
                        interfaces.nextElement();

                if (!networkInterface.isUp()
                        || networkInterface.isLoopback()
                        || networkInterface.isVirtual()) {

                    continue;
                }

                Enumeration<InetAddress> addresses =
                        networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {

                    InetAddress address =
                            addresses.nextElement();

                    if (!address.isLoopbackAddress()
                            && address instanceof java.net.Inet4Address) {

                        return address.getHostAddress();
                    }
                }
            }

        } catch (Exception e) {

            return "UNKNOWN-IP";
        }

        return "UNKNOWN-IP";
    }

    private String getMacAddress() {

        try {

            InetAddress localHost =
                    InetAddress.getLocalHost();

            NetworkInterface networkInterface =
                    NetworkInterface.getByInetAddress(localHost);

            if (networkInterface != null) {

                byte[] mac =
                        networkInterface.getHardwareAddress();

                if (mac != null) {

                    StringBuilder result =
                            new StringBuilder();

                    for (byte b : mac) {

                        result.append(
                                String.format("%02X", b)
                        );
                    }

                    return result.toString();
                }
            }

        } catch (Exception ignored) {
        }

        return "UNKNOWN-MAC";
    }
}