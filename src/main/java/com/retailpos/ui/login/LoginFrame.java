package com.retailpos.ui.login;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.retailpos.model.OtpCode;
import com.retailpos.model.Session;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.DeviceRepository;
import com.retailpos.repository.OtpRepository;
import com.retailpos.repository.SessionRepository;
import com.retailpos.repository.UserRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.DeviceRepositoryImpl;
import com.retailpos.repository.mongodb.OtpRepositoryImpl;
import com.retailpos.repository.mongodb.SessionRepositoryImpl;
import com.retailpos.repository.mongodb.UserRepositoryImpl;
import com.retailpos.security.AuthService;
import com.retailpos.security.EmailService;
import com.retailpos.security.OtpService;
import com.retailpos.security.SessionService;
import com.retailpos.service.AuditLogService;
import com.retailpos.ui.dashboard.DashboardFrame;
public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private EmailService emailService;
    private SessionService sessionService;
    public LoginFrame() {

        UserRepository userRepository =
                new UserRepositoryImpl();

        DeviceRepository deviceRepository =
                new DeviceRepositoryImpl();

        authService =
                new AuthService(
                        userRepository,
                        deviceRepository
                );
        SessionRepository sessionRepository =
        new SessionRepositoryImpl();

sessionService =
        new SessionService(sessionRepository);
        AuditLogRepository auditLogRepository =
                new AuditLogRepositoryImpl();

        auditLogService =
                new AuditLogService(
                        auditLogRepository
                );
        OtpRepository otpRepository =
        new OtpRepositoryImpl();

       otpService =
        new OtpService(
                otpRepository,
                userRepository
        );
        emailService =
        new EmailService();
        setTitle("Retail POS - Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel =
                new JPanel(new GridBagLayout());

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(8, 8, 8, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel titleLabel =
                new JLabel(
                        "RETAIL POS",
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        mainPanel.add(
                titleLabel,
                gbc
        );

        gbc.gridwidth = 1;

        JLabel usernameLabel =
                new JLabel("Username:");

        gbc.gridx = 0;
        gbc.gridy = 1;

        mainPanel.add(
                usernameLabel,
                gbc
        );

        usernameField =
                new JTextField(20);

        gbc.gridx = 1;
        gbc.gridy = 1;

        mainPanel.add(
                usernameField,
                gbc
        );

        JLabel passwordLabel =
                new JLabel("Password:");

        gbc.gridx = 0;
        gbc.gridy = 2;

        mainPanel.add(
                passwordLabel,
                gbc
        );

        passwordField =
                new JPasswordField(20);

        gbc.gridx = 1;
        gbc.gridy = 2;

        mainPanel.add(
                passwordField,
                gbc
        );

        JButton loginButton =
                new JButton("LOGIN");

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        mainPanel.add(
                loginButton,
                gbc
        );

        messageLabel =
                new JLabel(
                        " ",
                        SwingConstants.CENTER
                );

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        mainPanel.add(
                messageLabel,
                gbc
        );

        loginButton.addActionListener(
                e -> login()
        );

        passwordField.addActionListener(
                e -> login()
        );

        add(mainPanel);
    }

    private void login() {

        String username =
                usernameField.getText().trim();

        String password =
                new String(
                        passwordField.getPassword()
                );

        if (username.isEmpty()
                || password.isEmpty()) {

            messageLabel.setText(
                    "Please enter username and password."
            );

            return;
        }

        try {

            Optional<User> user =
                    authService.login(
                            username,
                            password
                    );

            if (user.isPresent()) {

                User loggedInUser =
                        user.get();

                boolean knownDevice =
                        authService.isCurrentDeviceKnown(
                                loggedInUser
                        );
                var currentDevice =
        authService.getCurrentDevice(loggedInUser);
                if (!knownDevice) {

    if (loggedInUser.getEmail() == null
            || loggedInUser.getEmail().isBlank()) {

        JOptionPane.showMessageDialog(
                this,
                "No email address is configured for this user.\n"
                        + "Please contact the administrator.",
                "OTP Verification",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }
    if (otpService.isOtpLocked(loggedInUser)) {

    LocalDateTime lockedUntil =
            loggedInUser.getOtpLockedUntil();

    long remainingMinutes =
            java.time.Duration.between(
                    LocalDateTime.now(),
                    lockedUntil
            ).toMinutes() + 1;

    JOptionPane.showMessageDialog(
            this,
            "OTP verification is temporarily locked.\n"
                    + "Please try again after "
                    + remainingMinutes
                    + " minute(s).",
            "OTP Account Locked",
            JOptionPane.ERROR_MESSAGE
    );

    auditLogService.log(
            loggedInUser.getId(),
            loggedInUser.getUsername(),
            "OTP_ACCOUNT_LOCKED",
            "SECURITY",
            "Login blocked because OTP verification is temporarily locked."
    );

    return;
}

    

    OtpCode otpCode =
            otpService.generateOtp(
                    loggedInUser,
                    currentDevice.getDeviceId()
            );

    try {

        emailService.sendOtpEmail(
                loggedInUser.getEmail(),
                loggedInUser.getFullName(),
                otpCode.getCode()
        );
       auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "NEW_DEVICE_OTP_SENT",
        "SECURITY",
        "OTP sent for new device verification."
);
    } catch (RuntimeException e) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to send OTP email.\n"
                        + "Please try again later.",
                "OTP Error",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    String enteredOtp =
            JOptionPane.showInputDialog(
                    this,
                    "A 6-digit OTP has been sent to:\n"
                            + loggedInUser.getEmail()
                            + "\n\nEnter OTP:",
                    "Device Verification",
                    JOptionPane.PLAIN_MESSAGE
            );

   boolean verified =
        otpService.verifyOtp(
                loggedInUser,
                currentDevice.getDeviceId(),
                enteredOtp
        );

    if (!verified) {

        JOptionPane.showMessageDialog(
                this,
                "Invalid or expired OTP.\n"
                        + "Login denied.",
                "Verification Failed",
                JOptionPane.ERROR_MESSAGE
        );
        auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "NEW_DEVICE_OTP_FAILED",
        "SECURITY",
        "New device OTP verification failed."
);
        return;
    }
    auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "NEW_DEVICE_OTP_VERIFIED",
        "SECURITY",
        "New device OTP verification successful."
);

    authService.registerCurrentDevice(
            loggedInUser
    );
    auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "NEW_DEVICE_REGISTERED",
        "SECURITY",
        "New device registered successfully after OTP verification."
);
    JOptionPane.showMessageDialog(
            this,
            "OTP verified successfully.\n"
                    + "New device has been registered.",
            "Device Verified",
            JOptionPane.INFORMATION_MESSAGE
    );

} else {

    authService.updateDeviceLastLogin(
            loggedInUser
    );

    JOptionPane.showMessageDialog(
            this,
            "Known device detected.\n"
                    + "lastLoginAt updated.",
            "Known Device",
            JOptionPane.INFORMATION_MESSAGE
    );
}

                messageLabel.setText(
                        "Login successful!"
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Welcome, "
                                + loggedInUser.getFullName()
                                + "\nRole: "
                                + loggedInUser.getRole(),
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                auditLogService.log(
                        loggedInUser.getId(),
                        loggedInUser.getUsername(),
                        "LOGIN",
                        "AUTHENTICATION",
                        "User logged in successfully"
                );
                Session session =
        sessionService.createSession(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                currentDevice.getDeviceId()
        );
                dispose();

               DashboardFrame dashboardFrame =
        new DashboardFrame(loggedInUser, session);

                dashboardFrame.setVisible(true);

            } else {

                messageLabel.setText(
                        "Invalid username or password."
                );

                passwordField.setText("");
            }

        } catch (Exception e) {

            messageLabel.setText(
                    "Login error."
            );

            e.printStackTrace();
        }
    }
}