package com.retailpos.ui.inventory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

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

import com.retailpos.model.Product;
import com.retailpos.model.StockTransaction;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.StockTransactionRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.StockTransactionRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.InventoryService;

public class InventoryFrame extends JFrame {

    private final ProductRepository productRepository;
    private final StockTransactionRepository transactionRepository;
    private final InventoryService inventoryService;
    private final User loggedInUser;
    private final AuditLogService auditLogService;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;

    public InventoryFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        productRepository =
                new ProductRepositoryImpl();

        transactionRepository =
                new StockTransactionRepositoryImpl();

        inventoryService =
                new InventoryService(
                        productRepository,
                        transactionRepository
                );
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

auditLogService =
        new AuditLogService(
                auditLogRepository
        );
        initializeUI();

        loadProducts();
    }

    private void initializeUI() {

        setTitle("Inventory Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // =========================
        // TOP PANEL
        // =========================

        JPanel topPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        JLabel searchLabel =
                new JLabel("Search Product:");

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

        add(topPanel, BorderLayout.NORTH);

        // =========================
        // TABLE
        // =========================

        String[] columns = {
                "ID",
                "Name",
                "SKU",
                "Category",
                "Stock",
                "Min Stock",
                "Status"
        };

        tableModel =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        productTable =
                new JTable(tableModel);

        productTable.setRowHeight(25);
        productTable.setAutoCreateRowSorter(true);
        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(productTable);

        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // BUTTON PANEL
        // =========================

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 10)
        );

        JButton stockInButton =
                new JButton("Stock In");

        JButton stockOutButton =
                new JButton("Stock Out");

        JButton adjustButton =
                new JButton("Adjust Stock");

        JButton historyButton =
                new JButton("History");

        buttonPanel.add(stockInButton);
        buttonPanel.add(stockOutButton);
        buttonPanel.add(adjustButton);
        buttonPanel.add(historyButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // =========================
        // BUTTON ACTIONS
        // =========================

        searchButton.addActionListener(e ->
                searchProducts()
        );

        refreshButton.addActionListener(e ->
                loadProducts()
        );

        searchField.addActionListener(e ->
                searchProducts()
        );

        stockInButton.addActionListener(e ->
                stockIn()
        );

        stockOutButton.addActionListener(e ->
                stockOut()
        );

        adjustButton.addActionListener(e ->
                adjustStock()
        );

        historyButton.addActionListener(e ->
                showHistory()
        );
    }

    // =========================
    // LOAD PRODUCTS
    // =========================

    private void loadProducts() {

        List<Product> products =
                productRepository.findAll();

        displayProducts(products);
    }

    // =========================
    // SEARCH PRODUCTS
    // =========================

    private void searchProducts() {

        String keyword =
                searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }

        List<Product> products =
                productRepository.findByName(keyword);

        displayProducts(products);
    }

    // =========================
    // DISPLAY PRODUCTS
    // =========================

    private void displayProducts(
            List<Product> products
    ) {

        tableModel.setRowCount(0);

        for (Product product : products) {

            tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    product.getCategory(),
                    product.getStockQuantity(),
                    product.getMinimumStock(),
                    product.getStatus()
            });
        }
    }

    // =========================
    // GET SELECTED PRODUCT
    // =========================

    private Product getSelectedProduct() {

        int selectedRow =
                productTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a product first."
            );

            return null;
        }

        int modelRow =
                productTable.convertRowIndexToModel(
                        selectedRow
                );

        String productId =
                tableModel
                        .getValueAt(modelRow, 0)
                        .toString();

        return productRepository
                .findById(productId)
                .orElse(null);
    }

    // =========================
    // STOCK IN
    // =========================

    private void stockIn() {

        Product product =
                getSelectedProduct();

        if (product == null) {
            return;
        }

        String input =
                JOptionPane.showInputDialog(
                        this,
                        "Enter quantity to add:",
                        "Stock In",
                        JOptionPane.PLAIN_MESSAGE
                );

        if (input == null) {
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(input);

            String reason =
                    JOptionPane.showInputDialog(
                            this,
                            "Enter reason:",
                            "Stock In",
                            JOptionPane.PLAIN_MESSAGE
                    );

            if (reason == null) {
                return;
            }

            inventoryService.stockIn(
                    product.getId(),
                    quantity,
                    reason,
                    loggedInUser.getUsername()
            );
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "STOCK_IN",
        "INVENTORY",
        "Stock added: "
                + product.getName()
                + ", Quantity: "
                + quantity
                + ", Reason: "
                + reason
);
            JOptionPane.showMessageDialog(
                    this,
                    "Stock added successfully."
            );

            loadProducts();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number."
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    // =========================
    // STOCK OUT
    // =========================

    private void stockOut() {

        Product product =
                getSelectedProduct();

        if (product == null) {
            return;
        }

        String input =
                JOptionPane.showInputDialog(
                        this,
                        "Enter quantity to remove:",
                        "Stock Out",
                        JOptionPane.PLAIN_MESSAGE
                );

        if (input == null) {
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(input);

            String reason =
                    JOptionPane.showInputDialog(
                            this,
                            "Enter reason:",
                            "Stock Out",
                            JOptionPane.PLAIN_MESSAGE
                    );

            if (reason == null) {
                return;
            }

            inventoryService.stockOut(
                    product.getId(),
                    quantity,
                    reason,
                    "admin"
            );
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "STOCK_OUT",
        "INVENTORY",
        "Stock removed: "
                + product.getName()
                + ", Quantity: "
                + quantity
                + ", Reason: "
                + reason
);
            JOptionPane.showMessageDialog(
                    this,
                    "Stock removed successfully."
            );

            loadProducts();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number."
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    // =========================
    // ADJUST STOCK
    // =========================

    private void adjustStock() {

        Product product =
                getSelectedProduct();

        if (product == null) {
            return;
        }

        String input =
                JOptionPane.showInputDialog(
                        this,
                        "Current stock: "
                                + product.getStockQuantity()
                                + "\n\nEnter new stock quantity:",
                        "Adjust Stock",
                        JOptionPane.PLAIN_MESSAGE
                );

        if (input == null) {
            return;
        }

        try {

            int newStock =
                    Integer.parseInt(input);

            String reason =
                    JOptionPane.showInputDialog(
                            this,
                            "Enter reason:",
                            "Adjust Stock",
                            JOptionPane.PLAIN_MESSAGE
                    );

            if (reason == null) {
                return;
            }

            inventoryService.adjustStock(
                    product.getId(),
                    newStock,
                    reason,
                   loggedInUser.getUsername()
            );
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "STOCK_ADJUST",
        "INVENTORY",
        "Stock adjusted: "
                + product.getName()
                + ", New Stock: "
                + newStock
                + ", Reason: "
                + reason
);
            JOptionPane.showMessageDialog(
                    this,
                    "Stock adjusted successfully."
            );

            loadProducts();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number."
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    // =========================
    // SHOW HISTORY
    // =========================

    private void showHistory() {

        Product product =
                getSelectedProduct();

        if (product == null) {
            return;
        }

        List<StockTransaction> transactions =
                inventoryService.getProductTransactions(
                        product.getId()
                );

        String[] columns = {
                "Type",
                "Quantity",
                "Previous Stock",
                "New Stock",
                "Reason",
                "Performed By",
                "Date"
        };

        DefaultTableModel historyModel =
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

        for (StockTransaction transaction :
                transactions) {

            historyModel.addRow(
                    new Object[]{
                            transaction.getType(),
                            transaction.getQuantity(),
                            transaction.getPreviousStock(),
                            transaction.getNewStock(),
                            transaction.getReason(),
                            transaction.getPerformedBy(),
                            transaction.getCreatedAt()
                    }
            );
        }

        JTable historyTable =
                new JTable(historyModel);

        historyTable.setAutoCreateRowSorter(true);
        historyTable.setRowHeight(25);

        JScrollPane scrollPane =
                new JScrollPane(historyTable);

        scrollPane.setPreferredSize(
                new Dimension(900, 400)
        );

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Stock History - "
                        + product.getName(),
                JOptionPane.PLAIN_MESSAGE
        );
    }
}