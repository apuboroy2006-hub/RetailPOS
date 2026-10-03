package com.retailpos.ui.customers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.retailpos.model.Customer;
import com.retailpos.service.CustomerService;

public class CustomerDialog extends JDialog {

    private final CustomerService customerService;
    private final Customer existingCustomer;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;

    private boolean saved = false;

    public CustomerDialog(
            JFrame parent,
            Customer customer,
            CustomerService customerService
    ) {

        super(
                parent,
                customer == null
                        ? "Add Customer"
                        : "Edit Customer",
                true
        );

        this.existingCustomer = customer;
        this.customerService = customerService;

        initializeUI();

        if (customer != null) {
            loadCustomerData();
        }
    }

    private void initializeUI() {

        setSize(500, 450);
        setLocationRelativeTo(getParent());

        setLayout(new BorderLayout(10, 10));

        // =========================
        // FORM PANEL
        // =========================

        JPanel formPanel =
                new JPanel(
                        new GridBagLayout()
                );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(8, 8, 8, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.anchor =
                GridBagConstraints.WEST;

        // =========================
        // NAME
        // =========================

        JLabel nameLabel =
                new JLabel("Name:");

        nameField =
                new JTextField(25);

        addField(
                formPanel,
                gbc,
                nameLabel,
                nameField,
                0
        );

        // =========================
        // PHONE
        // =========================

        JLabel phoneLabel =
                new JLabel("Phone:");

        phoneField =
                new JTextField(25);

        addField(
                formPanel,
                gbc,
                phoneLabel,
                phoneField,
                1
        );

        // =========================
        // EMAIL
        // =========================

        JLabel emailLabel =
                new JLabel("Email:");

        emailField =
                new JTextField(25);

        addField(
                formPanel,
                gbc,
                emailLabel,
                emailField,
                2
        );

        // =========================
        // ADDRESS
        // =========================

        JLabel addressLabel =
                new JLabel("Address:");

        addressArea =
                new JTextArea(5, 25);

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        JScrollPane addressScrollPane =
                new JScrollPane(addressArea);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;

        formPanel.add(
                addressLabel,
                gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        formPanel.add(
                addressScrollPane,
                gbc
        );

        add(
                formPanel,
                BorderLayout.CENTER
        );

        // =========================
        // BUTTON PANEL
        // =========================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton saveButton =
                new JButton("Save");

        JButton cancelButton =
                new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        // =========================
        // ACTIONS
        // =========================

        saveButton.addActionListener(
                e -> saveCustomer()
        );

        cancelButton.addActionListener(
                e -> dispose()
        );
    }

    // =========================
    // ADD FORM FIELD
    // =========================

    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            JLabel label,
            JTextField field,
            int row
    ) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    // =========================
    // LOAD EXISTING CUSTOMER
    // =========================

    private void loadCustomerData() {

        nameField.setText(
                existingCustomer.getName()
        );

        phoneField.setText(
                existingCustomer.getPhone()
        );

        emailField.setText(
                existingCustomer.getEmail()
        );

        addressArea.setText(
                existingCustomer.getAddress()
        );
    }

    // =========================
    // SAVE CUSTOMER
    // =========================

    private void saveCustomer() {

        String name =
                nameField.getText().trim();

        String phone =
                phoneField.getText().trim();

        String email =
                emailField.getText().trim();

        String address =
                addressArea.getText().trim();

        try {

            if (existingCustomer == null) {

                // ADD CUSTOMER

                customerService.createCustomer(
                        name,
                        phone,
                        email,
                        address
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Customer added successfully."
                );

            } else {

                // EDIT CUSTOMER

                existingCustomer.setName(name);
                existingCustomer.setPhone(phone);
                existingCustomer.setEmail(email);
                existingCustomer.setAddress(address);

                customerService.updateCustomer(
                        existingCustomer
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Customer updated successfully."
                );
            }

            saved = true;

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
                    "An unexpected error occurred:\n"
                            + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // CHECK SAVED
    // =========================

    public boolean isSaved() {
        return saved;
    }
}