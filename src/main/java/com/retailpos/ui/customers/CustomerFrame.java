package com.retailpos.ui.customers;

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

import com.retailpos.model.Customer;
import com.retailpos.repository.CustomerRepository;
import com.retailpos.repository.mongodb.CustomerRepositoryImpl;
import com.retailpos.service.CustomerService;

public class CustomerFrame extends JFrame {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
private JLabel resultLabel;
    public CustomerFrame() {

        customerRepository =
                new CustomerRepositoryImpl();

        customerService =
                new CustomerService(
                        customerRepository
                );

        initializeUI();

        loadCustomers();
    }

    private void initializeUI() {

        setTitle("Customer Management");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setLayout(
                new BorderLayout(10, 10)
        );

        // =========================
        // TOP PANEL
        // =========================

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        JLabel searchLabel =
                new JLabel("Search Customer:");

        searchField =
                new JTextField(25);

        JButton searchButton =
                new JButton("Search");

        JButton refreshButton =
                new JButton("Refresh");

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(refreshButton);
        resultLabel =
        new JLabel("Showing all customers");

resultLabel.setBorder(
        BorderFactory.createEmptyBorder(
                0, 10, 0, 0
        )
);

topPanel.add(resultLabel);

        add(
                topPanel,
                BorderLayout.NORTH
        );

        // =========================
        // TABLE
        // =========================

        String[] columns = {
                "ID",
                "Name",
                "Phone",
                "Email",
                "Address",
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

        customerTable =
                new JTable(tableModel);

        customerTable.setRowHeight(25);

        customerTable.setAutoCreateRowSorter(
                true
        );

        customerTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(customerTable);

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        // =========================
        // BUTTON PANEL
        // =========================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                10
                        )
                );

        JButton addButton =
                new JButton("Add Customer");

        JButton editButton =
                new JButton("Edit");

        JButton deleteButton =
                new JButton("Delete");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        // =========================
        // BUTTON ACTIONS
        // =========================

        searchButton.addActionListener(
                e -> searchCustomers()
        );

        refreshButton.addActionListener(
                e -> loadCustomers()
        );

        searchField.addActionListener(
                e -> searchCustomers()
        );

        addButton.addActionListener(
                e -> addCustomer()
        );

        editButton.addActionListener(
                e -> editCustomer()
        );

        deleteButton.addActionListener(
                e -> deleteCustomer()
        );
    }

    // =========================
    // LOAD CUSTOMERS
    // =========================
private void loadCustomers() {

    List<Customer> customers =
            customerService.getAllCustomers();

    displayCustomers(customers);

    resultLabel.setText(
            "Showing "
                    + customers.size()
                    + " customer(s)"
    );
}
    // =========================
    // SEARCH CUSTOMERS
    // =========================

   private void searchCustomers() {

    String keyword =
            searchField.getText().trim();

    List<Customer> customers =
            customerService.searchByName(
                    keyword
            );

    displayCustomers(customers);

    if (keyword.isEmpty()) {

        resultLabel.setText(
                "Showing "
                        + customers.size()
                        + " customer(s)"
        );

    } else if (customers.isEmpty()) {

        resultLabel.setText(
                "No customers found for: "
                        + keyword
        );

    } else {

        resultLabel.setText(
                "Showing "
                        + customers.size()
                        + " customer(s) for: "
                        + keyword
        );
    }
}

    // =========================
    // DISPLAY CUSTOMERS
    // =========================

    private void displayCustomers(
            List<Customer> customers
    ) {

        tableModel.setRowCount(0);

        for (Customer customer :
                customers) {

            tableModel.addRow(
                    new Object[]{
                            customer.getId(),
                            customer.getName(),
                            customer.getPhone(),
                            customer.getEmail(),
                            customer.getAddress(),
                            customer.getStatus()
                    }
            );
        }
    }

    // =========================
    // ADD CUSTOMER
    // =========================

    private void addCustomer() {

        CustomerDialog dialog =
                new CustomerDialog(
                        this,
                        null,
                        customerService
                );

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadCustomers();
        }
    }

    // =========================
    // EDIT CUSTOMER
    // =========================

    private void editCustomer() {

        Customer customer =
                getSelectedCustomer();

        if (customer == null) {
            return;
        }

        CustomerDialog dialog =
                new CustomerDialog(
                        this,
                        customer,
                        customerService
                );

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadCustomers();
        }
    }

    // =========================
    // DELETE CUSTOMER
    // =========================

    private void deleteCustomer() {

        Customer customer =
                getSelectedCustomer();

        if (customer == null) {
            return;
        }

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete "
                                + customer.getName()
                                + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            customerService.deleteCustomer(
                    customer.getId()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Customer deleted successfully."
            );

            loadCustomers();

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // GET SELECTED CUSTOMER
    // =========================

    private Customer getSelectedCustomer() {

        int selectedRow =
                customerTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a customer first."
            );

            return null;
        }

        int modelRow =
                customerTable.convertRowIndexToModel(
                        selectedRow
                );

        String customerId =
                tableModel
                        .getValueAt(
                                modelRow,
                                0
                        )
                        .toString();

        return customerRepository
                .findById(customerId)
                .orElse(null);
    }
}