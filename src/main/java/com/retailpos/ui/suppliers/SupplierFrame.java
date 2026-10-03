package com.retailpos.ui.suppliers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Supplier;
import com.retailpos.repository.SupplierRepository;
import com.retailpos.repository.mongodb.SupplierRepositoryImpl;
import com.retailpos.service.SupplierService;

public class SupplierFrame extends JFrame {

    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;

    private JTable supplierTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JLabel resultLabel;

    public SupplierFrame() {

        supplierRepository = new SupplierRepositoryImpl();
        supplierService = new SupplierService(supplierRepository);

        initializeUI();
        loadSuppliers();
    }

    private void initializeUI() {

        setTitle("Supplier Management");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // ---------------- TOP PANEL ----------------

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Search:");

        searchField = new JTextField(25);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        resultLabel = new JLabel("Showing all suppliers");

        resultLabel.setBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 0)
        );

        searchButton.addActionListener(e -> searchSuppliers());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadSuppliers();
        });

        searchField.addActionListener(e -> searchSuppliers());

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(refreshButton);
        topPanel.add(resultLabel);

        add(topPanel, BorderLayout.NORTH);

        // ---------------- TABLE ----------------

        String[] columns = {
                "ID",
                "Name",
                "Phone",
                "Email",
                "Address",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        supplierTable = new JTable(tableModel);

        supplierTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        supplierTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane =
                new JScrollPane(supplierTable);

        add(scrollPane, BorderLayout.CENTER);

        // ---------------- BUTTON PANEL ----------------

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton addButton = new JButton("Add Supplier");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> addSupplier());

        editButton.addActionListener(e -> editSupplier());

        deleteButton.addActionListener(e -> deleteSupplier());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // ---------------- LOAD SUPPLIERS ----------------

    private void loadSuppliers() {

        List<Supplier> suppliers =
                supplierService.getAllSuppliers();

        displaySuppliers(suppliers);

        resultLabel.setText(
                "Showing " + suppliers.size() + " supplier(s)"
        );
    }

    // ---------------- SEARCH ----------------

    private void searchSuppliers() {

        String keyword =
                searchField.getText().trim();

        if (keyword.isEmpty()) {

            loadSuppliers();
            return;
        }

        List<Supplier> suppliers =
                supplierService.searchByName(keyword);

        displaySuppliers(suppliers);

        if (suppliers.isEmpty()) {

            resultLabel.setText(
                    "No suppliers found for: " + keyword
            );

        } else {

            resultLabel.setText(
                    "Showing "
                            + suppliers.size()
                            + " supplier(s) for: "
                            + keyword
            );
        }
    }

    // ---------------- DISPLAY ----------------

    private void displaySuppliers(
            List<Supplier> suppliers
    ) {

        tableModel.setRowCount(0);

        for (Supplier supplier : suppliers) {

            tableModel.addRow(new Object[]{
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getAddress(),
                    supplier.getStatus()
            });
        }
    }

    // ---------------- ADD ----------------

    private void addSupplier() {

        SupplierDialog dialog =
                new SupplierDialog(
                        this,
                        supplierService,
                        null
                );

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadSuppliers();
        }
    }

    // ---------------- EDIT ----------------

    private void editSupplier() {

        int selectedRow =
                supplierTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a supplier first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int modelRow =
                supplierTable.convertRowIndexToModel(
                        selectedRow
                );

        String supplierId =
                tableModel.getValueAt(
                        modelRow,
                        0
                ).toString();

        Supplier supplier =
                supplierService.findById(supplierId)
                        .orElse(null);

        if (supplier == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Supplier not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        SupplierDialog dialog =
                new SupplierDialog(
                        this,
                        supplierService,
                        supplier
                );

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadSuppliers();
        }
    }

    // ---------------- DELETE ----------------

    private void deleteSupplier() {

        int selectedRow =
                supplierTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a supplier first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int modelRow =
                supplierTable.convertRowIndexToModel(
                        selectedRow
                );

        String supplierId =
                tableModel.getValueAt(
                        modelRow,
                        0
                ).toString();

        String supplierName =
                tableModel.getValueAt(
                        modelRow,
                        1
                ).toString();

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete supplier:\n"
                                + supplierName + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            supplierService.deleteSupplier(
                    supplierId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Supplier deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadSuppliers();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete supplier:\n"
                            + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}