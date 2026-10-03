package com.retailpos.ui.products;

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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Product;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.ProductService;
public class ProductFrame extends JFrame {

    private final ProductService productService;
    private final User loggedInUser;
    private final JTextField searchField;
    private final JTable productTable;
    private final DefaultTableModel tableModel;
    private final AuditLogService auditLogService;
   public ProductFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        ProductRepository productRepository =
                new ProductRepositoryImpl();

        productService =
                new ProductService(productRepository);
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

        auditLogService =
        new AuditLogService(
                auditLogRepository
        );

        setTitle("Retail POS - Products");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // =========================
        // MAIN PANEL
        // =========================

        JPanel mainPanel =
                new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

        // =========================
        // TOP PANEL
        // =========================

        JPanel topPanel =
                new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel =
                new JLabel("Product Management");

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        topPanel.add(
                titleLabel,
                BorderLayout.WEST
        );

        JPanel searchPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        searchField =
                new JTextField(20);

        JButton searchButton =
                new JButton("Search");

        JButton refreshButton =
                new JButton("Refresh");

        searchPanel.add(
                new JLabel("Search:")
        );

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        topPanel.add(
                searchPanel,
                BorderLayout.EAST
        );

        mainPanel.add(
                topPanel,
                BorderLayout.NORTH
        );

        // =========================
        // TABLE
        // =========================

        String[] columns = {
                "ID",
                "Name",
                "SKU",
                "Barcode",
                "Category",
                "Purchase Price",
                "Selling Price",
                "Stock",
                "Min Stock",
                "Supplier",
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

        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        productTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane =
                new JScrollPane(productTable);

        mainPanel.add(
                scrollPane,
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

        JButton addButton =
                new JButton("Add Product");

        JButton editButton =
                new JButton("Edit Product");

        JButton deleteButton =
                new JButton("Delete Product");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        // =========================
        // ACTIONS
        // =========================

        searchButton.addActionListener(
                e -> searchProducts()
        );

        refreshButton.addActionListener(
                e -> loadProducts()
        );

        searchField.addActionListener(
                e -> searchProducts()
        );

        addButton.addActionListener(
                e -> showAddProductDialog()
        );

        editButton.addActionListener(
                e -> showEditProductDialog()
        );

        deleteButton.addActionListener(
                e -> deleteSelectedProduct()
        );

        // Load products when window opens
        loadProducts();
    }

    // =========================
    // LOAD PRODUCTS
    // =========================

    private void loadProducts() {

        List<Product> products =
                productService.getAllProducts();

        displayProducts(products);
    }

    // =========================
    // SEARCH PRODUCTS
    // =========================

    private void searchProducts() {

        String searchText =
                searchField.getText().trim();

        List<Product> products =
                productService.searchProducts(
                        searchText
                );

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

            tableModel.addRow(
                    new Object[]{
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getBarcode(),
                            product.getCategory(),
                            product.getPurchasePrice(),
                            product.getSellingPrice(),
                            product.getStockQuantity(),
                            product.getMinimumStock(),
                            product.getSupplier(),
                            product.getStatus()
                    }
            );
        }
    }

    // =========================
    // ADD PRODUCT
    // =========================

  private void showAddProductDialog() {

    ProductDialog dialog =
            new ProductDialog(
                    this,
                    null
            );

    dialog.setVisible(true);

    if (dialog.isSaved()) {

        auditLogService.log(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                "PRODUCT_CREATED",
                "PRODUCTS",
                "Product created successfully"
        );

        loadProducts();
    }
}

    // =========================
    // EDIT PRODUCT
    // =========================

   private void showEditProductDialog() {

    int selectedRow =
            productTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a product first."
        );

        return;
    }

    int modelRow =
            productTable.convertRowIndexToModel(
                    selectedRow
            );

    String productId =
            tableModel
                    .getValueAt(
                            modelRow,
                            0
                    )
                    .toString();

    Product product =
            productService
                    .findById(productId)
                    .orElse(null);

    if (product == null) {

        JOptionPane.showMessageDialog(
                this,
                "Product not found."
        );

        return;
    }

    ProductDialog dialog =
            new ProductDialog(
                    this,
                    product
            );

    dialog.setVisible(true);

    if (dialog.isSaved()) {

        auditLogService.log(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                "PRODUCT_UPDATED",
                "PRODUCTS",
                "Product updated: " + product.getName()
        );

        loadProducts();
    }
}

    // =========================
    // DELETE PRODUCT
    // =========================

    private void deleteSelectedProduct() {

        int selectedRow =
                productTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a product first."
            );

            return;
        }

        int modelRow =
                productTable.convertRowIndexToModel(
                        selectedRow
                );

        String productId =
                tableModel
                        .getValueAt(
                                modelRow,
                                0
                        )
                        .toString();

        String productName =
                tableModel
                        .getValueAt(
                                modelRow,
                                1
                        )
                        .toString();

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete product: "
                                + productName
                                + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            productService.deleteProduct(
                    productId
            );
            auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "PRODUCT_DELETED",
        "PRODUCTS",
        "Product deleted: " + productName
);

            JOptionPane.showMessageDialog(
                    this,
                    "Product deleted successfully."
            );

            loadProducts();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}