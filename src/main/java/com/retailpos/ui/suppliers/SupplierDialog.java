package com.retailpos.ui.suppliers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.retailpos.model.Supplier;
import com.retailpos.service.SupplierService;

public class SupplierDialog extends JDialog {

    private final SupplierService supplierService;
    private final Supplier supplier;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;

    private boolean saved = false;

    public SupplierDialog(
            JFrame parent,
            SupplierService supplierService,
            Supplier supplier
    ) {
        super(
                parent,
                supplier == null ? "Add Supplier" : "Edit Supplier",
                true
        );

        this.supplierService = supplierService;
        this.supplier = supplier;

        initializeUI();

        if (supplier != null) {
            loadSupplierData();
        }
    }

    private void initializeUI() {

        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(
                new GridLayout(4, 2, 10, 10)
        );

        formPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 10, 15)
        );

        nameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveSupplier());

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSupplierData() {

        nameField.setText(supplier.getName());
        phoneField.setText(supplier.getPhone());
        emailField.setText(supplier.getEmail());
        addressField.setText(supplier.getAddress());
    }

    private void saveSupplier() {

        try {

            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String address = addressField.getText();

            if (supplier == null) {

                supplierService.createSupplier(
                        name,
                        phone,
                        email,
                        address
                );

            } else {

                supplier.setName(name);
                supplier.setPhone(phone);
                supplier.setEmail(email);
                supplier.setAddress(address);

                supplierService.updateSupplier(supplier);
            }

            saved = true;

            JOptionPane.showMessageDialog(
                    this,
                    supplier == null
                            ? "Supplier added successfully."
                            : "Supplier updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save supplier:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isSaved() {
        return saved;
    }
}