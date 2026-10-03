package com.retailpos.ui.login;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.UserRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.UserRepositoryImpl;
import com.retailpos.security.AuthService;
import com.retailpos.service.AuditLogService;
import com.retailpos.ui.dashboard.DashboardFrame;
public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    private final AuthService authService;
    private final AuditLogService auditLogService;
    public LoginFrame() {

        UserRepository userRepository =
                new UserRepositoryImpl();

        authService =
                new AuthService(userRepository);
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

auditLogService =
        new AuditLogService(
                auditLogRepository
        );

        setTitle("Retail POS - Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel =
                new JLabel("RETAIL POS", SwingConstants.CENTER);

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        JLabel usernameLabel =
                new JLabel("Username:");

        gbc.gridx = 0;
        gbc.gridy = 1;

        mainPanel.add(usernameLabel, gbc);

        usernameField =
                new JTextField(20);

        gbc.gridx = 1;
        gbc.gridy = 1;

        mainPanel.add(usernameField, gbc);

        JLabel passwordLabel =
                new JLabel("Password:");

        gbc.gridx = 0;
        gbc.gridy = 2;

        mainPanel.add(passwordLabel, gbc);

        passwordField =
                new JPasswordField(20);

        gbc.gridx = 1;
        gbc.gridy = 2;

        mainPanel.add(passwordField, gbc);

        JButton loginButton =
                new JButton("LOGIN");

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        mainPanel.add(loginButton, gbc);

        messageLabel =
                new JLabel(" ", SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        mainPanel.add(messageLabel, gbc);

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
                new String(passwordField.getPassword());

        if (username.isEmpty() ||
                password.isEmpty()) {

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
              dispose();

DashboardFrame dashboardFrame =
        new DashboardFrame(loggedInUser);

dashboardFrame.setVisible(true);

                // Dashboard will be opened here later.

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