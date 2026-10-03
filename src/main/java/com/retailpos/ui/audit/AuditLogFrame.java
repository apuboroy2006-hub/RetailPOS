package com.retailpos.ui.audit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.AuditLog;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.service.AuditLogService;

public class AuditLogFrame extends JFrame {

    private final AuditLogService auditLogService;

    private JTable table;

    private DefaultTableModel tableModel;

    private JTextField usernameField;

    public AuditLogFrame() {

        AuditLogRepository repository =
                new AuditLogRepositoryImpl();

        auditLogService =
                new AuditLogService(
                        repository
                );

        setTitle(
                "Retail POS - Audit Logs"
        );

        setSize(
                1100,
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

        loadLogs();
    }

    private void createTopPanel() {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        0,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "Audit Log / Activity Log"
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        panel.add(titleLabel);

        panel.add(
                new JLabel(
                        "Username:"
                )
        );

        usernameField =
                new JTextField(
                        15
                );

        panel.add(usernameField);

        JButton searchButton =
                new JButton(
                        "Search"
                );

        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        searchButton.setFocusPainted(false);
        refreshButton.setFocusPainted(false);

        searchButton.addActionListener(
                e -> searchLogs()
        );

        refreshButton.addActionListener(
                e -> {
                    usernameField.setText("");
                    loadLogs();
                }
        );

        panel.add(searchButton);
        panel.add(refreshButton);

        add(
                panel,
                BorderLayout.NORTH
        );
    }

    private void createTable() {

        String[] columns = {
                "Username",
                "Action",
                "Module",
                "Description",
                "Created At"
        };

        tableModel =
                new DefaultTableModel(
                        columns,
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

        table =
                new JTable(
                        tableModel
                );

        table.setRowHeight(28);

        table.getTableHeader()
                .setReorderingAllowed(false);

        JScrollPane scrollPane =
                new JScrollPane(
                        table
                );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        10,
                        0,
                        10
                )
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );
    }

    private void createBottomPanel() {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton closeButton =
                new JButton(
                        "Close"
                );

        closeButton.setFocusPainted(false);

        closeButton.addActionListener(
                e -> dispose()
        );

        panel.add(closeButton);

        add(
                panel,
                BorderLayout.SOUTH
        );
    }

    private void loadLogs() {

        try {

            List<AuditLog> logs =
                    auditLogService.getAllLogs();

            displayLogs(logs);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load audit logs.\n\n"
                            + ex.getMessage(),
                    "Audit Log Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void searchLogs() {

        String username =
                usernameField
                        .getText()
                        .trim();

        if (username.isEmpty()) {

            loadLogs();

            return;
        }

        try {

            List<AuditLog> logs =
                    auditLogService
                            .getLogsByUsername(
                                    username
                            );

            displayLogs(logs);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Search failed.\n\n"
                            + ex.getMessage(),
                    "Audit Log Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayLogs(
            List<AuditLog> logs
    ) {

        tableModel.setRowCount(0);

        for (AuditLog log : logs) {

            tableModel.addRow(
                    new Object[] {
                            log.getUsername(),
                            log.getAction(),
                            log.getModule(),
                            log.getDescription(),
                            log.getCreatedAt()
                    }
            );
        }
    }
}