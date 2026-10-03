package com.retailpos.ui.purchases;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Product;
import com.retailpos.model.Purchase;
import com.retailpos.model.PurchaseItem;
import com.retailpos.model.Supplier;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.PurchaseItemRepository;
import com.retailpos.repository.PurchaseRepository;
import com.retailpos.repository.SupplierRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseItemRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseRepositoryImpl;
import com.retailpos.repository.mongodb.SupplierRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.ProductService;
import com.retailpos.service.PurchaseService;
import com.retailpos.service.SupplierService;

public class PurchaseFrame extends JFrame {

    private final SupplierService supplierService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final AuditLogService auditLogService;

    private JComboBox<String> supplierComboBox;
    private JComboBox<String> paymentMethodComboBox;

    private JTable purchaseTable;
    private DefaultTableModel purchaseModel;

    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    private JTextField discountField;
    private JTextField taxField;
    private final User loggedInUser;
    private List<Supplier> suppliers =
            new ArrayList<>();

   public PurchaseFrame(User loggedInUser) {

    this.loggedInUser = loggedInUser;

        SupplierRepository supplierRepository =
                new SupplierRepositoryImpl();

        ProductRepository productRepository =
                new ProductRepositoryImpl();

        PurchaseRepository purchaseRepository =
                new PurchaseRepositoryImpl();

        PurchaseItemRepository purchaseItemRepository =
                new PurchaseItemRepositoryImpl();

        supplierService =
                new SupplierService(
                        supplierRepository
                );

        productService =
                new ProductService(
                        productRepository
                );

        purchaseService =
                new PurchaseService(
                        purchaseRepository,
                        purchaseItemRepository,
                        productService
                );
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

                auditLogService =
        new AuditLogService(
                auditLogRepository
        );
        setTitle("Retail POS - New Purchase");

        setSize(
                1000,
                650
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

        createPurchaseTable();

        createBottomPanel();

        loadSuppliers();
    }

    private void createTopPanel() {

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                10
                        )
                );

        topPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Purchase Information"
                )
        );

        topPanel.add(
                new JLabel("Supplier:")
        );

        supplierComboBox =
                new JComboBox<>();

        supplierComboBox.setPreferredSize(
                new java.awt.Dimension(
                        220,
                        30
                )
        );

        topPanel.add(
                supplierComboBox
        );

        JButton addProductButton =
                new JButton(
                        "Add Product"
                );

        addProductButton.setFocusPainted(
                false
        );

        addProductButton.addActionListener(
                e -> addProduct()
        );

        topPanel.add(
                addProductButton
        );

        topPanel.add(
                new JLabel("Payment:")
        );

        paymentMethodComboBox =
                new JComboBox<>(
                        new String[]{
                                "Cash",
                                "Card",
                                "UPI",
                                "Bank Transfer",
                                "Credit"
                        }
                );

        topPanel.add(
                paymentMethodComboBox
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );
    }

    private void createPurchaseTable() {

        purchaseModel =
                new DefaultTableModel(
                        new Object[]{
                                "Product",
                                "SKU",
                                "Quantity",
                                "Purchase Price",
                                "Discount",
                                "Total",
                                "Product ID"
                        },
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

        purchaseTable =
                new JTable(
                        purchaseModel
                );

        purchaseTable.setRowHeight(
                28
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        purchaseTable
                );

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Purchase Items"
                )
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );
    }

    private void createBottomPanel() {

        JPanel bottomPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JPanel actionPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        JButton removeButton =
                new JButton(
                        "Remove Item"
                );

        removeButton.setFocusPainted(
                false
        );

        removeButton.addActionListener(
                e -> removeSelectedItem()
        );

        actionPanel.add(
                removeButton
        );

        JButton clearButton =
                new JButton(
                        "Clear"
                );

        clearButton.setFocusPainted(
                false
        );

        clearButton.addActionListener(
                e -> clearPurchase()
        );

        actionPanel.add(
                clearButton
        );

        bottomPanel.add(
                actionPanel,
                BorderLayout.NORTH
        );

        JPanel calculationPanel =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                10,
                                5
                        )
                );

        calculationPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Purchase Summary"
                )
        );

        calculationPanel.add(
                new JLabel("Subtotal:")
        );

        subtotalLabel =
                new JLabel(
                        "₹0.00"
                );

        calculationPanel.add(
                subtotalLabel
        );
       
        calculationPanel.add(
                new JLabel("Discount:")
        );

        discountField =
                new JTextField(
                        "0"
                );

        calculationPanel.add(
                discountField
        );

        calculationPanel.add(
                new JLabel("Tax:")
        );

        taxField =
                new JTextField(
                        "0"
                );

        calculationPanel.add(
                taxField
        );

        calculationPanel.add(
                new JLabel("Total:")
        );

        totalLabel =
                new JLabel(
                        "₹0.00"
                );

        calculationPanel.add(
                totalLabel
        );

        bottomPanel.add(
                calculationPanel,
                BorderLayout.CENTER
        );

        JPanel completePanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton calculateButton =
                new JButton(
                        "Calculate"
                );

        calculateButton.setFocusPainted(
                false
        );

        calculateButton.addActionListener(
                e -> calculateTotals()
        );

        completePanel.add(
                calculateButton
        );

        JButton completeButton =
                new JButton(
                        "Complete Purchase"
                );

        completeButton.setFocusPainted(
                false
        );

        completeButton.addActionListener(
                e -> completePurchase()
        );

        completePanel.add(
                completeButton
        );

        bottomPanel.add(
                completePanel,
                BorderLayout.SOUTH
        );

        add(
                bottomPanel,
                BorderLayout.SOUTH
        );
    }

    private void loadSuppliers() {

        suppliers.clear();

        supplierComboBox.removeAllItems();

        suppliers =
                supplierService.getAllSuppliers();

        for (Supplier supplier : suppliers) {

            supplierComboBox.addItem(
                    supplier.getName()
            );
        }
    }

    private void addProduct() {

        List<Product> products =
                productService.getAllProducts();

        if (products.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No products available.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        String[] productNames =
                new String[
                        products.size()
                ];

        for (int i = 0;
                i < products.size();
                i++) {

            Product product =
                    products.get(i);

            productNames[i] =
                    product.getName()
                            + " | "
                            + product.getSku();
        }

        String selected =
                (String) JOptionPane.showInputDialog(
                        this,
                        "Select Product:",
                        "Add Product",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        productNames,
                        productNames[0]
                );

        if (selected == null) {
            return;
        }

        int selectedIndex = 0;

        for (int i = 0;
                i < productNames.length;
                i++) {

            if (productNames[i]
                    .equals(selected)) {

                selectedIndex = i;
                break;
            }
        }

        Product product =
                products.get(
                        selectedIndex
                );

        String quantityText =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Quantity:",
                        "1"
                );

        if (quantityText == null) {
            return;
        }

        String priceText =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Purchase Price:",
                        String.valueOf(
                                product.getPurchasePrice()
                        )
                );

        if (priceText == null) {
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(
                            quantityText.trim()
                    );

            double purchasePrice =
                    Double.parseDouble(
                            priceText.trim()
                    );

            if (quantity <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero."
                );
            }

            if (purchasePrice < 0) {

                throw new IllegalArgumentException(
                        "Purchase price cannot be negative."
                );
            }

            double total =
                    quantity * purchasePrice;

            purchaseModel.addRow(
                    new Object[]{
                            product.getName(),
                            product.getSku(),
                            quantity,
                            purchasePrice,
                            0.0,
                            total,
                            product.getId()
                    }
            );

            calculateTotals();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void removeSelectedItem() {

        int selectedRow =
                purchaseTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an item first.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        purchaseModel.removeRow(
                selectedRow
        );

        calculateTotals();
    }

    private void clearPurchase() {

        purchaseModel.setRowCount(0);

        discountField.setText("0");

        taxField.setText("0");

        calculateTotals();
    }
private void calculateTotals() {

    double subtotal = 0;

    for (int row = 0;
            row < purchaseModel.getRowCount();
            row++) {

        Object totalValue =
                purchaseModel.getValueAt(
                        row,
                        5
                );

        subtotal +=
                Double.parseDouble(
                        totalValue.toString()
                );
    }

    double discount = 0;
    double taxRate = 0;

    try {

        discount =
                Double.parseDouble(
                        discountField
                                .getText()
                                .trim()
                );

    } catch (NumberFormatException ex) {

        discount = 0;
    }

    try {

        taxRate =
                Double.parseDouble(
                        taxField
                                .getText()
                                .trim()
                );

    } catch (NumberFormatException ex) {

        taxRate = 0;
    }

    if (discount < 0) {
        discount = 0;
    }

    if (taxRate < 0) {
        taxRate = 0;
    }

    double taxableAmount =
            subtotal - discount;

    if (taxableAmount < 0) {
        taxableAmount = 0;
    }

    double taxAmount =
            taxableAmount
                    * taxRate
                    / 100;

    double total =
            taxableAmount
                    + taxAmount;

    subtotalLabel.setText(
            String.format(
                    "₹%.2f",
                    subtotal
            )
    );

    totalLabel.setText(
            String.format(
                    "₹%.2f",
                    total
            )
    );
}
  private void completePurchase() {

    if (supplierComboBox
            .getSelectedItem() == null) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a supplier.",
                "Validation",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    if (purchaseModel.getRowCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "Please add at least one product.",
                "Validation",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    try {

        Supplier selectedSupplier =
                suppliers.get(
                        supplierComboBox
                                .getSelectedIndex()
                );

        double subtotal = 0;

        List<PurchaseItem> items =
                new ArrayList<>();

        for (int row = 0;
                row < purchaseModel.getRowCount();
                row++) {

            String productName =
                    purchaseModel
                            .getValueAt(
                                    row,
                                    0
                            )
                            .toString();

            String sku =
                    purchaseModel
                            .getValueAt(
                                    row,
                                    1
                            )
                            .toString();

            int quantity =
                    Integer.parseInt(
                            purchaseModel
                                    .getValueAt(
                                            row,
                                            2
                                    )
                                    .toString()
                    );

            double unitPrice =
                    Double.parseDouble(
                            purchaseModel
                                    .getValueAt(
                                            row,
                                            3
                                    )
                                    .toString()
                    );

            double discount =
                    Double.parseDouble(
                            purchaseModel
                                    .getValueAt(
                                            row,
                                            4
                                    )
                                    .toString()
                    );

            double itemTotal =
                    Double.parseDouble(
                            purchaseModel
                                    .getValueAt(
                                            row,
                                            5
                                    )
                                    .toString()
                    );

            String productId =
                    purchaseModel
                            .getValueAt(
                                    row,
                                    6
                            )
                            .toString();

            subtotal += itemTotal;

            PurchaseItem item =
                    new PurchaseItem(
                            null,
                            productId,
                            productName,
                            sku,
                            quantity,
                            unitPrice,
                            discount,
                            itemTotal
                    );

            items.add(item);
        }

        // Purchase-level discount
        double discount = 0;

        try {

            discount =
                    Double.parseDouble(
                            discountField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException ex) {

            discount = 0;
        }

        // Tax percentage
        double taxRate = 0;

        try {

            taxRate =
                    Double.parseDouble(
                            taxField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException ex) {

            taxRate = 0;
        }

        if (discount < 0) {

            throw new IllegalArgumentException(
                    "Discount cannot be negative."
            );
        }

        if (taxRate < 0) {

            throw new IllegalArgumentException(
                    "Tax cannot be negative."
            );
        }

        // Amount after discount
        double taxableAmount =
                subtotal - discount;

        if (taxableAmount < 0) {
            taxableAmount = 0;
        }

        // Convert tax percentage into tax amount
        double taxAmount =
                taxableAmount
                        * taxRate
                        / 100;

        // Final total
        double total =
                taxableAmount
                        + taxAmount;

        String invoiceNumber =
                generateInvoiceNumber();

        Purchase purchase =
                new Purchase(
                        invoiceNumber,
                        selectedSupplier.getId(),
                        selectedSupplier.getName(),
                        subtotal,
                        discount,
                        taxAmount,
                        total,
                        paymentMethodComboBox
                                .getSelectedItem()
                                .toString(),
                       "COMPLETED",
                       loggedInUser.getUsername()
                );

        purchaseService.createPurchase(
                purchase,
                items
        );
auditLogService.log(
        "admin",
        "admin",
        "PURCHASE_CREATED",
        "PURCHASES",
        "Purchase created: Invoice "
                + purchase.getInvoiceNumber()
);
        JOptionPane.showMessageDialog(
                this,
                "Purchase completed successfully.\n\n"
                        + "Invoice: "
                        + invoiceNumber
                        + "\n"
                        + "Subtotal: ₹"
                        + String.format(
                                "%.2f",
                                subtotal
                        )
                        + "\n"
                        + "Discount: ₹"
                        + String.format(
                                "%.2f",
                                discount
                        )
                        + "\n"
                        + "Tax: ₹"
                        + String.format(
                                "%.2f",
                                taxAmount
                        )
                        + "\n"
                        + "Total: ₹"
                        + String.format(
                                "%.2f",
                                total
                        ),
                "Purchase Complete",
                JOptionPane.INFORMATION_MESSAGE
        );

        clearPurchase();

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter valid discount and tax values.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Purchase Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

    private String generateInvoiceNumber() {

        return "PUR-"
                + System.currentTimeMillis();
    }
}