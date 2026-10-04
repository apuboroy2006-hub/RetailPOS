package com.retailpos.ui.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Device;
import com.retailpos.repository.DeviceRepository;
import com.retailpos.repository.mongodb.DeviceRepositoryImpl;

public class DeviceManagementFrame extends JFrame {

    private final DeviceRepository deviceRepository;

    private final JTable deviceTable;

    private final DefaultTableModel tableModel;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    public DeviceManagementFrame() {

        deviceRepository =
                new DeviceRepositoryImpl();

        setTitle("Retail POS - Device Management");
        setSize(1000, 500);
        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );
        setLocationRelativeTo(null);

        JPanel mainPanel =
                new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "DEVICE MANAGEMENT",
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        mainPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );

        String[] columns = {
                "Username",
                "Device Name",
                "Operating System",
                "OS Version",
                "IP Address",
                "Registered At",
                "Last Login",
                "Status"
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

        deviceTable =
                new JTable(tableModel);

        deviceTable.setRowHeight(25);
        deviceTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane =
                new JScrollPane(deviceTable);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        JButton refreshButton =
                new JButton("Refresh");

        refreshButton.addActionListener(
                e -> loadDevices()
        );

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        bottomPanel.add(refreshButton);

        mainPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        loadDevices();
    }

    private void loadDevices() {

        tableModel.setRowCount(0);

        List<Device> devices =
                deviceRepository.findAll();

        for (Device device : devices) {

            String registeredAt =
                    device.getRegisteredAt() != null
                            ? device.getRegisteredAt()
                                    .format(DATE_FORMAT)
                            : "";

            String lastLogin =
                    device.getLastLoginAt() != null
                            ? device.getLastLoginAt()
                                    .format(DATE_FORMAT)
                            : "";

            tableModel.addRow(
                    new Object[] {
                            device.getUsername(),
                            device.getDeviceName(),
                            device.getOperatingSystem(),
                            device.getOsVersion(),
                            device.getIpAddress(),
                            registeredAt,
                            lastLogin,
                            device.getStatus()
                    }
            );
        }
    }
}