package com.retailpos.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.retailpos.model.Settings;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.SettingsRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.SettingsRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.BackupService;
import com.retailpos.service.RestoreService;
import com.retailpos.service.SettingsService;

public class SettingsFrame extends JFrame {

    private JTextField storeNameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> currencyComboBox;
    private JTextField taxNumberField;

    private JTextField invoicePrefixField;
    private JCheckBox showTaxOnInvoiceCheckBox;

    private final SettingsService settingsService;
    private final AuditLogService auditLogService;
    private final BackupService backupService;
    private final RestoreService restoreService;
    private final User loggedInUser;

    public SettingsFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        SettingsRepository settingsRepository =
                new SettingsRepositoryImpl();

        settingsService =
                new SettingsService(
                        settingsRepository
                );
backupService =
        new BackupService();
AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

auditLogService =
        new AuditLogService(
                auditLogRepository
        );
restoreService =
        new RestoreService();
        initializeUI();

        loadSettings();
    }

    private void initializeUI() {

        setTitle("Retail POS - Settings");

        setSize(600, 600);

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

        add(
                createFormPanel(),
                BorderLayout.CENTER
        );

        add(
                createBottomPanel(),
                BorderLayout.SOUTH
        );
    }

    private JPanel createFormPanel() {

        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        30,
                        20,
                        30
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        8,
                        8,
                        8,
                        8
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1;

        storeNameField =
                new JTextField();

        addressField =
                new JTextField();

        phoneField =
                new JTextField();

        emailField =
                new JTextField();

        currencyComboBox =
                new JComboBox<>(
                        new String[]{
                                "INR",
                                "USD",
                                "EUR",
                                "GBP"
                        }
                );

        taxNumberField =
                new JTextField();

        invoicePrefixField =
                new JTextField();

        showTaxOnInvoiceCheckBox =
                new JCheckBox(
                        "Show tax on invoice"
                );

        addField(
                panel,
                gbc,
                0,
                "Store Name:",
                storeNameField
        );

        addField(
                panel,
                gbc,
                1,
                "Store Address:",
                addressField
        );

        addField(
                panel,
                gbc,
                2,
                "Phone:",
                phoneField
        );

        addField(
                panel,
                gbc,
                3,
                "Email:",
                emailField
        );

        addField(
                panel,
                gbc,
                4,
                "Currency:",
                currencyComboBox
        );

        addField(
                panel,
                gbc,
                5,
                "Tax Number:",
                taxNumberField
        );

        addField(
                panel,
                gbc,
                6,
                "Invoice Prefix:",
                invoicePrefixField
        );

        addField(
                panel,
                gbc,
                7,
                "Tax:",
                showTaxOnInvoiceCheckBox
        );

        return panel;
    }
private void backupDatabase() {

    JFileChooser fileChooser =
            new JFileChooser();

    fileChooser.setDialogTitle(
            "Save Retail POS Backup"
    );

    fileChooser.setSelectedFile(
            new File(
                    "RetailPOS_Backup.json"
            )
    );

    int result =
            fileChooser.showSaveDialog(
                    this
            );

    if (result !=
            JFileChooser.APPROVE_OPTION) {

        return;
    }

    File backupFile =
            fileChooser.getSelectedFile();

    try {

        backupService.createBackup(
                backupFile
        );
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "BACKUP_CREATED",
        "BACKUP",
        "Database backup created: "
                + backupFile.getAbsolutePath()
);
        JOptionPane.showMessageDialog(
                this,
                "Backup created successfully.\n\n"
                        + backupFile.getAbsolutePath(),
                "Backup Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "Backup failed.\n\n"
                        + ex.getMessage(),
                "Backup Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component component
    ) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        panel.add(
                new JLabel(label),
                gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        panel.add(
                component,
                gbc
        );
    }
    private void restoreDatabase() {

    int confirmation =
            JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to restore?\n\n"
                            + "All current data will be replaced.",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

    if (confirmation !=
            JOptionPane.YES_OPTION) {

        return;
    }

    JFileChooser fileChooser =
            new JFileChooser();

    fileChooser.setDialogTitle(
            "Select Retail POS Backup"
    );

    int result =
            fileChooser.showOpenDialog(
                    this
            );

    if (result !=
            JFileChooser.APPROVE_OPTION) {

        return;
    }

    File backupFile =
            fileChooser.getSelectedFile();

    try {

        restoreService.restoreBackup(
                backupFile
        );
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "RESTORE_COMPLETED",
        "BACKUP",
        "Database restored from backup: "
                + backupFile.getAbsolutePath()
);
        JOptionPane.showMessageDialog(
                this,
                "Database restored successfully.\n\n"
                        + "Please restart the application.",
                "Restore Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "Restore failed.\n\n"
                        + ex.getMessage(),
                "Restore Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
  private JPanel createBottomPanel() {

    JPanel panel =
            new JPanel(
                    new FlowLayout(
                            FlowLayout.RIGHT
                    )
            );

    JButton backupButton =
            new JButton("Backup Database");

    JButton restoreButton =
            new JButton("Restore Database");
            JButton auditLogButton =
        new JButton("Audit Logs");

    JButton saveButton =
            new JButton("Save");

    JButton closeButton =
            new JButton("Close");

    backupButton.setFocusPainted(false);

    restoreButton.setFocusPainted(false);
    auditLogButton.setFocusPainted(false);

auditLogButton.addActionListener(
        e -> {
          com.retailpos.ui.audit.AuditLogFrame frame =
        new com.retailpos.ui.audit.AuditLogFrame();

            frame.setVisible(true);
        }
);

    saveButton.setFocusPainted(false);

    closeButton.setFocusPainted(false);

    backupButton.addActionListener(
            e -> backupDatabase()
    );

    restoreButton.addActionListener(
            e -> restoreDatabase()
    );

    saveButton.addActionListener(
            e -> saveSettings()
    );

    closeButton.addActionListener(
            e -> dispose()
    );

    panel.add(
            backupButton
    );

    panel.add(
            restoreButton
    );
    panel.add(auditLogButton);
    panel.add(
            saveButton
    );

    panel.add(
            closeButton
    );

    return panel;
}
    private void loadSettings() {

        Settings settings =
                settingsService.getSettings();

        if (settings == null) {

            invoicePrefixField.setText(
                    "INV-"
            );

            showTaxOnInvoiceCheckBox.setSelected(
                    true
            );

            return;
        }

        storeNameField.setText(
                settings.getStoreName()
        );

        addressField.setText(
                settings.getStoreAddress()
        );

        phoneField.setText(
                settings.getPhone()
        );

        emailField.setText(
                settings.getEmail()
        );

        currencyComboBox.setSelectedItem(
                settings.getCurrency()
        );

        taxNumberField.setText(
                settings.getTaxNumber()
        );

        String invoicePrefix =
                settings.getInvoicePrefix();

        if (invoicePrefix == null ||
                invoicePrefix.isBlank()) {

            invoicePrefix = "INV-";
        }

        invoicePrefixField.setText(
                invoicePrefix
        );

        showTaxOnInvoiceCheckBox.setSelected(
                settings.isShowTaxOnInvoice()
        );
    }

    private void saveSettings() {

        try {

            String invoicePrefix =
                    invoicePrefixField
                            .getText()
                            .trim();

            if (invoicePrefix.isBlank()) {

                invoicePrefix = "INV-";
            }

            Settings settings =
                    new Settings(
                            storeNameField
                                    .getText()
                                    .trim(),

                            addressField
                                    .getText()
                                    .trim(),

                            phoneField
                                    .getText()
                                    .trim(),

                            emailField
                                    .getText()
                                    .trim(),

                            currencyComboBox
                                    .getSelectedItem()
                                    .toString(),

                            taxNumberField
                                    .getText()
                                    .trim(),

                            invoicePrefix,

                            showTaxOnInvoiceCheckBox
                                    .isSelected()
                    );

            settingsService.saveSettings(
                    settings
            );
           auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "SETTINGS_UPDATED",
        "SETTINGS",
        "Store settings updated"
);
            JOptionPane.showMessageDialog(
                    this,
                    "Settings saved successfully.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Settings Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}