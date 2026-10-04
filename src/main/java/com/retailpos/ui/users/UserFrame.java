package com.retailpos.ui.users;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.UserRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.UserRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.UserService;

public class UserFrame extends JFrame {

    private final UserService userService;
    private final User loggedInUser;
    private final AuditLogService auditLogService;

    private JTable userTable;
    private DefaultTableModel userModel;

    private JTextField searchField;

    public UserFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        UserRepository userRepository =
                new UserRepositoryImpl();

        userService =
                new UserService(
                        userRepository
                );
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

          auditLogService =
        new AuditLogService(
                auditLogRepository
        );
        setTitle(
                "Retail POS - Users"
        );

        setSize(
                1000,
                600
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        createTopPanel();

        createTable();

        createBottomPanel();

        loadUsers();
    }

    private void createTopPanel() {

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                10
                        )
                );

        topPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "User Search"
                )
        );

        topPanel.add(
                new JLabel(
                        "Search:"
                )
        );

        searchField =
                new JTextField(
                        20
                );

        topPanel.add(
                searchField
        );

        JButton searchButton =
                new JButton(
                        "Search"
                );

        searchButton.setFocusPainted(
                false
        );

        searchButton.addActionListener(
                e -> searchUsers()
        );

        topPanel.add(
                searchButton
        );

        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.addActionListener(
                e -> loadUsers()
        );

        topPanel.add(
                refreshButton
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );
    }

    private void createTable() {

        userModel =
                new DefaultTableModel(
                        new Object[]{
                                "Username",
                             "Full Name",
                             "Email",
                             "Role",
                             "Status"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        userTable =
                new JTable(
                        userModel
                );

        userTable.setRowHeight(
                28
        );

        userTable.setAutoCreateRowSorter(
                true
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        userTable
                );

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Users"
                )
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );
    }

    private void createBottomPanel() {

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                10
                        )
                );

        JButton addButton =
                new JButton(
                        "Add User"
                );

        addButton.setFocusPainted(
                false
        );

        addButton.addActionListener(
                e -> addUser()
        );

        bottomPanel.add(
                addButton
        );

        JButton editButton =
                new JButton(
                        "Edit User"
                );

        editButton.setFocusPainted(
                false
        );

        editButton.addActionListener(
                e -> editUser()
        );

        bottomPanel.add(
                editButton
        );

        JButton deleteButton =
                new JButton(
                        "Delete User"
                );

        deleteButton.setFocusPainted(
                false
        );

        deleteButton.addActionListener(
                e -> deleteUser()
        );

        bottomPanel.add(
                deleteButton
        );
JButton changePasswordButton =
        new JButton(
                "Change Password"
        );

changePasswordButton.setFocusPainted(
        false
);

changePasswordButton.addActionListener(
        e -> changePassword()
);

bottomPanel.add(
        changePasswordButton
);
        add(
                bottomPanel,
                BorderLayout.SOUTH
        );
    }

    private void loadUsers() {

        try {

            List<User> users =
                    userService.getAllUsers();

            userModel.setRowCount(
                    0
            );

            for (User user :
                    users) {

                userModel.addRow(
                        new Object[]{
                                user.getUsername(),
        user.getFullName(),
        user.getEmail(),
        user.getRole(),
        user.getStatus()
                        }
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "User Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void searchUsers() {

        String keyword =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        try {

            List<User> users =
                    userService.getAllUsers();

            userModel.setRowCount(
                    0
            );

            for (User user :
                    users) {

                String username =
                        user.getUsername()
                                .toLowerCase();

                String fullName =
                        user.getFullName()
                                .toLowerCase();

                if (username.contains(keyword)
                        || fullName.contains(keyword)) {

                    userModel.addRow(
                            new Object[]{
                                    user.getUsername(),
                                    user.getFullName(),
                                    user.getRole(),
                                    user.getStatus()
                            }
                    );
                }
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Search Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

  private void addUser() {

    JTextField usernameField =
            new JTextField();

    JTextField fullNameField =
            new JTextField();

    JTextField emailField =
            new JTextField();

    JPasswordField passwordField =
            new JPasswordField();

    JComboBox<String> roleComboBox =
            new JComboBox<>(
                    new String[]{
                            "MANAGER",
                            "CASHIER"
                    }
            );

    Object[] fields = {

            "Username:",
            usernameField,

            "Full Name:",
            fullNameField,

            "Email:",
            emailField,

            "Password:",
            passwordField,

            "Role:",
            roleComboBox
    };

    int result =
            JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Add User",
                    JOptionPane.OK_CANCEL_OPTION
            );

    if (result != JOptionPane.OK_OPTION) {

        return;
    }

    String username =
            usernameField
                    .getText()
                    .trim();

    String fullName =
            fullNameField
                    .getText()
                    .trim();

    String email =
            emailField
                    .getText()
                    .trim();

    String password =
            new String(
                    passwordField.getPassword()
            );

    String role =
            roleComboBox
                    .getSelectedItem()
                    .toString();

    if (username.isEmpty()
            || fullName.isEmpty()
            || email.isEmpty()
            || password.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "All fields are required.",
                "Validation",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        userService.createUser(
                username,
                password,
                fullName,
                email,
                role
        );

        auditLogService.log(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                "USER_CREATED",
                "USERS",
                "User created: " + username
        );

        JOptionPane.showMessageDialog(
                this,
                "User created successfully.",
                "Add User",
                JOptionPane.INFORMATION_MESSAGE
        );

        loadUsers();

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Add User Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
   private void editUser() {

    int selectedRow =
            userTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a user first.",
                "Edit User",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int modelRow =
            userTable.convertRowIndexToModel(
                    selectedRow
            );

    String username =
            userModel
                    .getValueAt(
                            modelRow,
                            0
                    )
                    .toString();

    User user =
            userService
                    .findByUsername(
                            username
                    )
                    .orElse(null);

    if (user == null) {

        JOptionPane.showMessageDialog(
                this,
                "User not found.",
                "Edit User",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    JTextField fullNameField =
            new JTextField(
                    user.getFullName()
            );

    JTextField emailField =
            new JTextField(
                    user.getEmail() != null
                            ? user.getEmail()
                            : ""
            );

    JComboBox<String> roleComboBox =
            new JComboBox<>(
                    new String[]{
                            "ADMIN",
                            "MANAGER",
                            "CASHIER"
                    }
            );

    roleComboBox.setSelectedItem(
            user.getRole()
    );

    JComboBox<String> statusComboBox =
            new JComboBox<>(
                    new String[]{
                            "ACTIVE",
                            "INACTIVE"
                    }
            );

    statusComboBox.setSelectedItem(
            user.getStatus()
    );

    Object[] fields = {

            "Username:",
            new JLabel(
                    user.getUsername()
            ),

            "Full Name:",
            fullNameField,

            "Email:",
            emailField,

            "Role:",
            roleComboBox,

            "Status:",
            statusComboBox
    };

    int result =
            JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Edit User",
                    JOptionPane.OK_CANCEL_OPTION
            );

    if (result != JOptionPane.OK_OPTION) {

        return;
    }

    String fullName =
            fullNameField
                    .getText()
                    .trim();

    String email =
            emailField
                    .getText()
                    .trim();

    String role =
            roleComboBox
                    .getSelectedItem()
                    .toString();

    String status =
            statusComboBox
                    .getSelectedItem()
                    .toString();

    if (fullName.isEmpty()
            || email.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Full name and email are required.",
                "Validation",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    if (!email.contains("@")) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter a valid email address.",
                "Validation",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        user.setFullName(
                fullName
        );

        user.setEmail(
                email
        );

        user.setRole(
                role
        );

        user.setStatus(
                status
        );

        userService.updateUser(
                user
        );

        auditLogService.log(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                "USER_UPDATED",
                "USERS",
                "User updated: " + user.getUsername()
        );

        JOptionPane.showMessageDialog(
                this,
                "User updated successfully.",
                "Edit User",
                JOptionPane.INFORMATION_MESSAGE
        );

        loadUsers();

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Edit User Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
   private void deleteUser() {

    int selectedRow =
            userTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a user first.",
                "Delete User",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int modelRow =
            userTable.convertRowIndexToModel(
                    selectedRow
            );

    String username =
            userModel
                    .getValueAt(
                            modelRow,
                            0
                    )
                    .toString();

    if (username.equals("admin")) {

        JOptionPane.showMessageDialog(
                this,
                "The admin user cannot be deleted.",
                "Delete User",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int confirmation =
            JOptionPane.showConfirmDialog(
                    this,
                    "Delete user: "
                            + username
                            + "?",
                    "Delete User",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

    if (confirmation !=
            JOptionPane.YES_OPTION) {

        return;
    }

    try {

        User user =
                userService
                        .findByUsername(
                                username
                        )
                        .orElse(null);

        if (user == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "User not found.",
                    "Delete User",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        userService.deleteUser(
                user.getId()
        );
        auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "USER_DELETED",
        "USERS",
        "User deleted: " + user.getUsername()
);
        JOptionPane.showMessageDialog(
                this,
                "User deleted successfully.",
                "Delete User",
                JOptionPane.INFORMATION_MESSAGE
        );

        loadUsers();

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Delete User Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
private void changePassword() {

    int selectedRow =
            userTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a user first.",
                "Change Password",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int modelRow =
            userTable.convertRowIndexToModel(
                    selectedRow
            );

    String username =
            userModel
                    .getValueAt(
                            modelRow,
                            0
                    )
                    .toString();

    JPasswordField newPasswordField =
            new JPasswordField();

    JPasswordField confirmPasswordField =
            new JPasswordField();

    Object[] fields = {

            "Username:",
            new JLabel(username),

            "New Password:",
            newPasswordField,

            "Confirm Password:",
            confirmPasswordField
    };

    int result =
            JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Change Password",
                    JOptionPane.OK_CANCEL_OPTION
            );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    String newPassword =
            new String(
                    newPasswordField.getPassword()
            );

    String confirmPassword =
            new String(
                    confirmPasswordField.getPassword()
            );

    if (newPassword.isBlank()) {

        JOptionPane.showMessageDialog(
                this,
                "New password is required.",
                "Change Password",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    if (newPassword.length() < 6) {

        JOptionPane.showMessageDialog(
                this,
                "New password must contain at least 6 characters.",
                "Change Password",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    if (!newPassword.equals(confirmPassword)) {

        JOptionPane.showMessageDialog(
                this,
                "Passwords do not match.",
                "Change Password",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    try {

        userService.resetPassword(
                username,
                newPassword
        );

        auditLogService.log(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                "PASSWORD_CHANGED",
                "USERS",
                "Password changed for user: "
                        + username
        );

        JOptionPane.showMessageDialog(
                this,
                "Password changed successfully.",
                "Change Password",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Change Password Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
}