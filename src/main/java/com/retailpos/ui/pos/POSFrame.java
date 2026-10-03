package com.retailpos.ui.pos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Customer;
import com.retailpos.model.Product;
import com.retailpos.model.Sale;
import com.retailpos.model.SaleItem;
import com.retailpos.model.Settings;
import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.CustomerRepository;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.SaleItemRepository;
import com.retailpos.repository.SaleRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.CustomerRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.SaleItemRepositoryImpl;
import com.retailpos.repository.mongodb.SaleRepositoryImpl;
import com.retailpos.repository.mongodb.SettingsRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.CustomerService;
import com.retailpos.service.ProductService;
import com.retailpos.service.SaleService;

public class POSFrame extends JFrame {
private CustomerService customerService;
private Customer selectedCustomer;
    private final ProductService productService;

    private JTextField searchField;

    private JTable cartTable;

    private DefaultTableModel cartModel;

    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    private JComboBox<String> paymentMethodCombo;
    private JComboBox<String> customerComboBox;
private double taxRate = 0.0;
private final SaleService saleService;
private final AuditLogService auditLogService;
private final User loggedInUser;

    public POSFrame(User loggedInUser) {

    this.loggedInUser = loggedInUser;

        ProductRepository productRepository =
                new ProductRepositoryImpl();

        productService =
                new ProductService(productRepository);
                CustomerRepository customerRepository =
        new CustomerRepositoryImpl();

customerService =
        new CustomerService(customerRepository);
        SaleRepository saleRepository =
        new SaleRepositoryImpl();

SaleItemRepository saleItemRepository =
        new SaleItemRepositoryImpl();
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

auditLogService =
        new AuditLogService(
                auditLogRepository
        );

saleService =
        new SaleService(
                saleRepository,
                saleItemRepository
        );

        initializeUI();
    }
private void selectCustomer() {

    List<Customer> customers =
            customerService.getAllCustomers();

    if (customers.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "No customers found.",
                "Customer",
                JOptionPane.INFORMATION_MESSAGE
        );

        return;
    }

    JList<Customer> customerList =
            new JList<>(
                    customers.toArray(
                            new Customer[0]
                    )
            );

    customerList.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    customerList.setSelectedIndex(0);

    JScrollPane scrollPane =
            new JScrollPane(customerList);

    scrollPane.setPreferredSize(
            new java.awt.Dimension(450, 250)
    );

    int result =
            JOptionPane.showConfirmDialog(
                    this,
                    scrollPane,
                    "Select Customer",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

    if (result == JOptionPane.OK_OPTION) {

        Customer customer =
                customerList.getSelectedValue();

        if (customer != null) {

            selectedCustomer = customer;

            customerComboBox.removeAllItems();

            customerComboBox.addItem(
                    customer.getName()
                            + " - "
                            + customer.getPhone()
            );

            customerComboBox.setSelectedIndex(0);
        }
    }
}
    private void initializeUI() {

        setTitle("Point of Sale");

        setSize(1200, 700);

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

        JLabel titleLabel =
                new JLabel("POINT OF SALE");

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        JLabel searchLabel =
                new JLabel("Product Search:");

        searchField =
                new JTextField(25);

        JButton searchButton =
                new JButton("Search");

        searchButton.addActionListener(
                e -> searchProducts()
        );

        topPanel.add(titleLabel);

        topPanel.add(
                Box.createHorizontalStrut(30)
        );

        topPanel.add(searchLabel);

        topPanel.add(searchField);

        topPanel.add(searchButton);

        add(
                topPanel,
                BorderLayout.NORTH
        );

        // =========================
        // CENTER - CART TABLE
        // =========================

       String[] columns = {
        "Product",
        "SKU",
        "Quantity",
        "Unit Price",
        "Discount",
        "Total",
        "Product ID"
};

        cartModel =
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

        cartTable =
                new JTable(cartModel);

        cartTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane cartScrollPane =
                new JScrollPane(cartTable);

        add(
                cartScrollPane,
                BorderLayout.CENTER
        );

        // =========================
        // RIGHT PANEL
        // =========================

        JPanel rightPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        rightPanel.setPreferredSize(
                new Dimension(300, 0)
        );

        // =========================
        // CUSTOMER SECTION
        // =========================

        JPanel customerPanel =
                new JPanel(
                        new GridLayout(
                                3,
                                1,
                                5,
                                5
                        )
                );

        customerPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Customer"
                )
        );

        customerPanel.add(
                new JLabel("Customer")
        );

        customerComboBox =
        new JComboBox<>();

        customerComboBox.addItem(
        "Walk-in Customer"
);

       customerPanel.add(
        customerComboBox
);

        JButton selectCustomerButton =
                new JButton(
                        "Select Customer"
                );
                selectCustomerButton.addActionListener(
        e -> selectCustomer()
);

        customerPanel.add(
                selectCustomerButton
        );

        rightPanel.add(
                customerPanel,
                BorderLayout.NORTH
        );

        // =========================
        // TOTALS
        // =========================

        JPanel totalsPanel =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                5,
                                8
                        )
                );

        totalsPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Bill Summary"
                )
        );

        subtotalLabel =
                new JLabel("₹0.00");

        discountLabel =
                new JLabel("₹0.00");

        taxLabel =
                new JLabel("₹0.00");

        totalLabel =
                new JLabel("₹0.00");

        totalLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        totalsPanel.add(
                new JLabel("Subtotal:")
        );

        totalsPanel.add(
                subtotalLabel
        );

        totalsPanel.add(
                new JLabel("Discount:")
        );

        totalsPanel.add(
                discountLabel
        );

        totalsPanel.add(
                new JLabel("Tax:")
        );

        totalsPanel.add(
                taxLabel
        );

        totalsPanel.add(
                new JLabel("Total:")
        );

        totalsPanel.add(
                totalLabel
        );

        rightPanel.add(
                totalsPanel,
                BorderLayout.CENTER
        );

        // =========================
        // PAYMENT
        // =========================

        JPanel paymentPanel =
                new JPanel(
                        new GridLayout(
                                3,
                                1,
                                5,
                                8
                        )
                );

        paymentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Payment"
                )
        );

        paymentPanel.add(
                new JLabel(
                        "Payment Method:"
                )
        );

        paymentMethodCombo =
                new JComboBox<>(
                        new String[]{
                                "Cash",
                                "Card",
                                "UPI",
                                "Bank Transfer"
                        }
                );

        paymentPanel.add(
                paymentMethodCombo
        );

        JButton completeSaleButton =
                new JButton(
                        "Complete Sale"
                );
                completeSaleButton.addActionListener(
        e -> completeSale()
);

        paymentPanel.add(
                completeSaleButton
        );

        rightPanel.add(
                paymentPanel,
                BorderLayout.SOUTH
        );

        add(
                rightPanel,
                BorderLayout.EAST
        );

        // =========================
        // BOTTOM BUTTONS
        // =========================

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        JButton removeButton =
                new JButton(
                        "Remove Item"
                );

        JButton clearButton =
                new JButton(
                        "Clear Cart"
                );
JButton taxButton =
        new JButton(
                "Apply Tax"
        );
        taxButton.addActionListener(
        e -> applyTax()
);

JButton discountButton =
        new JButton(
                "Apply Discount"
        );


        removeButton.addActionListener(
                e -> removeSelectedItem()
        );

        clearButton.addActionListener(
                e -> clearCart()
        );
discountButton.addActionListener(
        e -> applyDiscount()
);
        bottomPanel.add(
                removeButton
        );

        bottomPanel.add(
                clearButton
        );
bottomPanel.add(
        removeButton
);

bottomPanel.add(
        discountButton
);
bottomPanel.add(
        taxButton
);
bottomPanel.add(
        clearButton
);
        add(
                bottomPanel,
                BorderLayout.SOUTH
        );
    }

    // =========================
    // SEARCH PRODUCTS
    // =========================

    private void searchProducts() {

        String keyword =
                searchField.getText().trim();

        if (keyword.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a product name.",
                    "Search",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        List<Product> products =
                productService.searchProducts(
                        keyword
                );

        if (products.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No products found for: "
                            + keyword,
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JList<Product> productList =
                new JList<>(
                        products.toArray(
                                new Product[0]
                        )
                );

        productList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        productList.setSelectedIndex(0);

        JScrollPane scrollPane =
                new JScrollPane(
                        productList
                );

        scrollPane.setPreferredSize(
                new Dimension(
                        450,
                        200
                )
        );

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        scrollPane,
                        "Select Product",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (
                result ==
                JOptionPane.OK_OPTION
        ) {

            Product selectedProduct =
                    productList.getSelectedValue();

            if (selectedProduct != null) {

                addProductToCart(
                        selectedProduct
                );
            }
        }
    }

    // =========================
    // ADD PRODUCT TO CART
    // =========================

    private void addProductToCart(
            Product product
    ) {

        for (
                int row = 0;
                row < cartModel.getRowCount();
                row++
        ) {

            String productSku =
                    (String) cartModel.getValueAt(
                            row,
                            1
                    );

            if (
                    productSku.equals(
                            product.getSku()
                    )
            ) {

                int quantity =
                        ((Number)
                                cartModel.getValueAt(
                                        row,
                                        2
                                )
                        ).intValue();

                quantity++;

                double unitPrice =
                        product.getSellingPrice();

                double discount = 0.0;

                double total =
                        quantity * unitPrice
                                - discount;

                cartModel.setValueAt(
                        quantity,
                        row,
                        2
                );

                cartModel.setValueAt(
                        unitPrice,
                        row,
                        3
                );

                cartModel.setValueAt(
                        discount,
                        row,
                        4
                );

                cartModel.setValueAt(
                        total,
                        row,
                        5
                );

                updateCartTotals();

                return;
            }
        }

        int quantity = 1;

        double unitPrice =
                product.getSellingPrice();

        double discount = 0.0;

        double total =
                quantity * unitPrice
                        - discount;

       cartModel.addRow(
        new Object[]{
                product.getName(),
                product.getSku(),
                quantity,
                unitPrice,
                discount,
                total,
                product.getId()
        }
);
        updateCartTotals();
    }

    // =========================
    // REMOVE SELECTED ITEM
    // =========================

    private void removeSelectedItem() {

        int selectedRow =
                cartTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an item to remove.",
                    "Remove Item",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        cartModel.removeRow(
                selectedRow
        );

        updateCartTotals();
    }
// =========================
// APPLY DISCOUNT
// =========================

private void applyDiscount() {

    int selectedRow =
            cartTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select an item first.",
                "Discount",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int quantity =
            ((Number)
                    cartModel.getValueAt(
                            selectedRow,
                            2
                    )
            ).intValue();

    double unitPrice =
            ((Number)
                    cartModel.getValueAt(
                            selectedRow,
                            3
                    )
            ).doubleValue();

    double maxDiscount =
            quantity * unitPrice;

    String input =
            JOptionPane.showInputDialog(
                    this,
                    "Enter discount amount:",
                    "Apply Discount",
                    JOptionPane.PLAIN_MESSAGE
            );

    if (input == null) {
        return;
    }

    try {

        double discount =
                Double.parseDouble(
                        input.trim()
                );

        if (discount < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Discount cannot be negative.",
                    "Invalid Discount",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (discount > maxDiscount) {

            JOptionPane.showMessageDialog(
                    this,
                    "Discount cannot be greater than "
                            + String.format(
                                    "₹%.2f",
                                    maxDiscount
                            ),
                    "Invalid Discount",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        double total =
                maxDiscount - discount;

        cartModel.setValueAt(
                discount,
                selectedRow,
                4
        );

        cartModel.setValueAt(
                total,
                selectedRow,
                5
        );

        updateCartTotals();

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter a valid number.",
                "Invalid Discount",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

// =========================
// APPLY TAX
// =========================

private void applyTax() {

    if (cartModel.getRowCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "Cart is empty.",
                "Tax",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String input =
            JOptionPane.showInputDialog(
                    this,
                    "Enter tax percentage:",
                    "Apply Tax",
                    JOptionPane.PLAIN_MESSAGE
            );

    if (input == null) {
        return;
    }

    try {

        double rate =
                Double.parseDouble(
                        input.trim()
                );

        if (rate < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Tax percentage cannot be negative.",
                    "Invalid Tax",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (rate > 100) {

            JOptionPane.showMessageDialog(
                    this,
                    "Tax percentage cannot be greater than 100%.",
                    "Invalid Tax",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        taxRate = rate;

        updateCartTotals();

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter a valid percentage.",
                "Invalid Tax",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
// =========================
// COMPLETE SALE
// =========================
// =========================
// CLEAR CART
// =========================

private void clearCart() {

    if (cartModel.getRowCount() == 0) {

        return;
    }

    int result =
            JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to clear the cart?",
                    "Clear Cart",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

    if (result == JOptionPane.YES_OPTION) {

        cartModel.setRowCount(0);

        updateCartTotals();
    }
}
// =========================
// COMPLETE SALE
// =========================

private void completeSale() {

    if (cartModel.getRowCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "Cart is empty.",
                "Complete Sale",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String invoiceNumber =
            generateInvoiceNumber();

    String paymentMethod =
            (String)
                    paymentMethodCombo.getSelectedItem();

    if (paymentMethod == null ||
            paymentMethod.isBlank()) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a payment method.",
                "Complete Sale",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

  double subtotal = 0.0;
double discount = 0.0;

for (
        int row = 0;
        row < cartModel.getRowCount();
        row++
) {

    double rowTotal =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            5
                    )
            ).doubleValue();

    double rowDiscount =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            4
                    )
            ).doubleValue();

    subtotal +=
            rowTotal + rowDiscount;

    discount +=
            rowDiscount;
}

double discountedSubtotal =
        subtotal - discount;

double tax =
        discountedSubtotal
                * taxRate
                / 100.0;

double total =
        discountedSubtotal + tax;
        String customerId = null;
String customerName = "Walk-in Customer";

if (selectedCustomer != null) {

    customerId =
            selectedCustomer.getId();

    customerName =
            selectedCustomer.getName();
}
Sale sale =
        new Sale(
                invoiceNumber,
                customerId,
                customerName,
                subtotal,
                discount,
                tax,
                total,
                paymentMethod,
                "COMPLETED",
                loggedInUser.getUsername()
        );
        List<SaleItem> saleItems =
        new java.util.ArrayList<>();
        for (
        int row = 0;
        row < cartModel.getRowCount();
        row++
) {

    String productId =
            (String)
                    cartModel.getValueAt(
                            row,
                            6
                    );

    String productName =
            (String)
                    cartModel.getValueAt(
                            row,
                            0
                    );

    String sku =
            (String)
                    cartModel.getValueAt(
                            row,
                            1
                    );

    int quantity =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            2
                    )
            ).intValue();

    double unitPrice =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            3
                    )
            ).doubleValue();

    double itemDiscount =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            4
                    )
            ).doubleValue();

    double itemTotal =
            ((Number)
                    cartModel.getValueAt(
                            row,
                            5
                    )
            ).doubleValue();

    SaleItem saleItem =
            new SaleItem(
                    sale.getId(),
                    productId,
                    productName,
                    sku,
                    quantity,
                    unitPrice,
                    itemDiscount,
                    itemTotal
            );

    saleItems.add(saleItem);
}
saleService.createSale(
        sale,
        saleItems
);
auditLogService.log(
        loggedInUser.getId(),
        loggedInUser.getUsername(),
        "SALE_CREATED",
        "SALES",
        "Sale completed: Invoice "
                + invoiceNumber
);
for (SaleItem item : saleItems) {

    productService.reduceStock(
            item.getProductId(),
            item.getQuantity()
    );
}
JOptionPane.showMessageDialog(
        this,
        "Sale completed successfully.\nInvoice: "
                + invoiceNumber,
        "Sale Completed",
        JOptionPane.INFORMATION_MESSAGE
);

clearCart();
selectedCustomer = null;
customerComboBox.removeAllItems();
taxRate = 0.0;
updateCartTotals();
}
// =========================
// GENERATE INVOICE NUMBER
// =========================
// =========================
// GENERATE INVOICE NUMBER
// =========================

private String generateInvoiceNumber() {

    Settings settings =
            new SettingsRepositoryImpl()
                    .getSettings();

    String prefix = "INV-";

    if (settings != null &&
            settings.getInvoicePrefix() != null &&
            !settings.getInvoicePrefix().isBlank()) {

        prefix =
                settings
                        .getInvoicePrefix()
                        .trim();
    }

    return prefix
            + System.currentTimeMillis();
}

// =========================
// UPDATE CART TOTALS
// =========================

private void updateCartTotals() {

    double subtotal = 0.0;
    double discount = 0.0;

    for (
            int row = 0;
            row < cartModel.getRowCount();
            row++
    ) {

        double rowTotal =
                ((Number)
                        cartModel.getValueAt(
                                row,
                                5
                        )
                ).doubleValue();

        double rowDiscount =
                ((Number)
                        cartModel.getValueAt(
                                row,
                                4
                        )
                ).doubleValue();

        subtotal +=
                rowTotal + rowDiscount;

        discount +=
                rowDiscount;
    }

    double discountedSubtotal =
            subtotal - discount;

    double tax =
            discountedSubtotal
                    * taxRate
                    / 100.0;

    double total =
            discountedSubtotal + tax;

    subtotalLabel.setText(
            String.format(
                    "₹%.2f",
                    subtotal
            )
    );

    discountLabel.setText(
            String.format(
                    "₹%.2f",
                    discount
            )
    );

    taxLabel.setText(
            String.format(
                    "₹%.2f",
                    tax
            )
    );

    totalLabel.setText(
            String.format(
                    "₹%.2f",
                    total
            )
    );
}

}
