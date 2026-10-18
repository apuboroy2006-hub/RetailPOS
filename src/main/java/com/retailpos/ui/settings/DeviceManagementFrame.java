
package com.retailpos.ui.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import com.retailpos.model.Device;
import com.retailpos.repository.DeviceRepository;
import com.retailpos.repository.mongodb.DeviceRepositoryImpl;
import com.retailpos.model.Session;
import com.retailpos.repository.mongodb.SessionRepositoryImpl;
import com.retailpos.security.SessionService;

public class DeviceManagementFrame extends JFrame {

    private final DeviceRepository deviceRepository;
    private final JTable deviceTable;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final Timer refreshTimer;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DeviceManagementFrame() {

        deviceRepository = new DeviceRepositoryImpl();

        setTitle("Retail POS - Device Management");
        setSize(1100, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        refreshTimer.stop();
    }
});
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titleLabel = new JLabel(
                "DEVICE MANAGEMENT",
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "ID",
                "Username",
                "Device Name",
                "Operating System",
                "OS Version",
                "IP Address",
                "Registered At",
                "Last Login",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        deviceTable = new JTable(tableModel);
        deviceTable.setRowHeight(25);
        deviceTable.setAutoCreateRowSorter(true);

        // Internal ID is needed for updates and deletion.
        // Keep it hidden from the visible table.
        deviceTable.removeColumn(
                deviceTable.getColumnModel().getColumn(0)
        );

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        searchField = new JTextField(22);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchDevices());


JButton refreshButton = new JButton("Refresh");
refreshButton.addActionListener(e -> {
    searchField.setText("");
    deviceTable.setRowSorter(null);
    loadDevices();
});


        JButton statusButton = new JButton("Block / Activate");
        statusButton.addActionListener(e -> toggleDeviceStatus());

        JButton removeButton = new JButton("Remove Device");
        removeButton.addActionListener(e -> removeDevice());

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(searchField);
        bottomPanel.add(searchButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(statusButton);
        bottomPanel.add(removeButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

loadDevices();

refreshTimer = new Timer(5000, e -> loadDevices());
refreshTimer.start();
    }

    private void loadDevices() {

        tableModel.setRowCount(0);

        List<Device> devices = deviceRepository.findAll();

        for (Device device : devices) {

            String registeredAt =
                    device.getRegisteredAt() != null
                            ? device.getRegisteredAt().format(DATE_FORMAT)
                            : "";

            String lastLogin =
                    device.getLastLoginAt() != null
                            ? device.getLastLoginAt().format(DATE_FORMAT)
                            : "";

            tableModel.addRow(new Object[] {
                    device.getId(),
                    device.getUsername(),
                    device.getDeviceName(),
                    device.getOperatingSystem(),
                    device.getOsVersion(),
                    device.getIpAddress(),
                    registeredAt,
                    lastLogin,
                    device.getStatus()
            });
        }
    }

    private Device getSelectedDevice() {

        int selectedRow = deviceTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a device first.",
                    "No Device Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        int modelRow = deviceTable.convertRowIndexToModel(selectedRow);

        String id = String.valueOf(
                tableModel.getValueAt(modelRow, 0)
        );

        for (Device device : deviceRepository.findAll()) {
            if (id.equals(device.getId())) {
                return device;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "Selected device was not found. Please refresh.",
                "Device Not Found",
                JOptionPane.WARNING_MESSAGE
        );

        loadDevices();
        return null;
    }

    private void toggleDeviceStatus() {

        Device device = getSelectedDevice();

        if (device == null) {
            return;
        }

        String currentStatus = device.getStatus();

        if (!"ACTIVE".equalsIgnoreCase(currentStatus)
                && !"BLOCKED".equalsIgnoreCase(currentStatus)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unknown device status: " + currentStatus,
                    "Cannot Change Status",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean isActive =
                "ACTIVE".equalsIgnoreCase(currentStatus);

        String newStatus = isActive ? "BLOCKED" : "ACTIVE";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Change device status for "
                        + device.getUsername()
                        + " from "
                        + currentStatus
                        + " to "
                        + newStatus
                        + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        device.setStatus(newStatus);
        deviceRepository.update(device);

        if ("BLOCKED".equalsIgnoreCase(device.getStatus())) {
            SessionService sessionService = new SessionService(
                    new SessionRepositoryImpl()
            );

            for (Session activeSession : sessionService.getActiveSessions()) {
                if (device.getUserId() != null
                        && device.getDeviceId() != null
                        && device.getUserId().equals(activeSession.getUserId())
                        && device.getDeviceId().equals(activeSession.getDeviceId())) {
                    sessionService.forceLogout(activeSession.getId());
                }
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "Device status updated to " + newStatus + "."
        );

        loadDevices();
    }

    private void removeDevice() {

        Device device = getSelectedDevice();

        if (device == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Permanently remove this registered device?\n\n"
                        + "Username: " + device.getUsername()
                        + "\nDevice: " + device.getDeviceName()
                        + "\n\nThe device record will be deleted.",
                "Confirm Device Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        deviceRepository.deleteById(device.getId());

        JOptionPane.showMessageDialog(
                this,
                "Device record removed."
        );

        loadDevices();
    }

    private void searchDevices() {

        String query = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        deviceTable.setRowSorter(sorter);

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(query)
                    )
            );
        }
    }
}
