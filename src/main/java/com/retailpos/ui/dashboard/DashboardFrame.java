package com.retailpos.ui.dashboard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.retailpos.model.User;
import com.retailpos.repository.AuditLogRepository;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.SaleItemRepositoryImpl;
import com.retailpos.repository.mongodb.SaleRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.ProductService;
import com.retailpos.service.SaleService;
import com.retailpos.ui.customers.CustomerFrame;
import com.retailpos.ui.inventory.InventoryFrame;
import com.retailpos.ui.pos.POSFrame;
import com.retailpos.ui.products.ProductFrame;
import com.retailpos.ui.purchases.PurchaseFrame;
import com.retailpos.ui.purchases.PurchaseHistoryFrame;
import com.retailpos.ui.reports.ReportsFrame;
import com.retailpos.ui.sales.SalesFrame;
import com.retailpos.ui.settings.DeviceManagementFrame;
import com.retailpos.ui.settings.SettingsFrame;
import com.retailpos.ui.suppliers.SupplierFrame;
import com.retailpos.ui.users.UserFrame;
public class DashboardFrame extends JFrame {

    private final User loggedInUser;
    private final AuditLogService auditLogService;
    private final ProductService productService;
private final SaleService saleService;
private JLabel productsValueLabel;
private JLabel todaysSalesValueLabel;
private JLabel stockValueLabel;
private JLabel lowStockValueLabel;
private Timer dashboardTimer;

    public DashboardFrame(User loggedInUser) {
ProductRepositoryImpl productRepository =
        new ProductRepositoryImpl();

productService =
        new ProductService(
                productRepository
        );
        SaleRepositoryImpl saleRepository =
        new SaleRepositoryImpl();

SaleItemRepositoryImpl saleItemRepository =
        new SaleItemRepositoryImpl();

saleService =
        new SaleService(
                saleRepository,
                saleItemRepository
        );
        this.loggedInUser = loggedInUser;
        AuditLogRepository auditLogRepository =
        new AuditLogRepositoryImpl();

auditLogService =
        new AuditLogService(
                auditLogRepository
        );

        setTitle("Retail POS - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
        addWindowListener(
        new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(
                    java.awt.event.WindowEvent e
            ) {

                if (dashboardTimer != null) {
                    dashboardTimer.stop();
                }
            }
        }
);
    }

    private void createUI() {

        setLayout(new BorderLayout());

        // =========================
        // TOP BAR
        // =========================

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 15, 10, 15
                )
        );

        JLabel titleLabel =
                new JLabel("RETAIL POS");

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        topPanel.add(
                titleLabel,
                BorderLayout.WEST
        );

        JLabel userLabel =
                new JLabel(
                        "Welcome, "
                                + loggedInUser.getFullName()
                                + " | Role: "
                                + loggedInUser.getRole()
                );

        userLabel.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        topPanel.add(
                userLabel,
                BorderLayout.EAST
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );


        // =========================
        // SIDEBAR
        // =========================

        JPanel sidebar =
                new JPanel();

       sidebar.setLayout(
        new GridLayout(
                0,
                1,
                5,
                5
        )
);

        sidebar.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

       String[] menuItems;

String role =
        loggedInUser.getRole();

if ("ADMIN".equalsIgnoreCase(role)) {

    menuItems = new String[] {
            "Dashboard",
            "POS",
            "Products",
            "Inventory",
            "Sales",
            "Purchases",
            "Customers",
            "Suppliers",
            "Users",
            "Reports",
            "Device Management",
            "Settings"
    };

} else if ("MANAGER".equalsIgnoreCase(role)) {

    menuItems = new String[] {
            "Dashboard",
            "POS",
            "Products",
            "Inventory",
            "Sales",
            "Purchases",
            "Customers",
            "Suppliers",
            "Reports"
    };

} else if ("CASHIER".equalsIgnoreCase(role)) {

    menuItems = new String[] {
            "Dashboard",
            "POS",
            "Sales",
            "Customers"
    };

} else {

    menuItems = new String[] {
            "Dashboard"
    };
}
      for (String item : menuItems) {

    JButton button =
            new JButton(item);

    button.setFocusPainted(false);

    if (item.equals("Products")) {

        button.addActionListener(e -> {

            ProductFrame productFrame =
                    new ProductFrame(loggedInUser);

            productFrame.setVisible(true);
        });
    }
    if (item.equals("POS")) {
    button.addActionListener(e -> {
       POSFrame posFrame =
        new POSFrame(loggedInUser);
       posFrame.setVisible(true);
    });
}
    if (item.equals("Inventory")) {

    button.addActionListener(e -> {

        InventoryFrame inventoryFrame =
               new InventoryFrame(loggedInUser);

        inventoryFrame.setVisible(true);
    });
}
if (item.equals("Customers")) {

    button.addActionListener(e -> {

        CustomerFrame customerFrame =
                new CustomerFrame();

        customerFrame.setVisible(true);
    });
}
if (item.equals("Suppliers")) {
    button.addActionListener(e -> {
        SupplierFrame supplierFrame = new SupplierFrame();
        supplierFrame.setVisible(true);
    });
}
if (item.equals("Sales")) {

    button.addActionListener(e -> {

        SalesFrame salesFrame =
                new SalesFrame();

        salesFrame.setVisible(true);
    });
}
if (item.equals("Purchases")) {

    button.addActionListener(e -> {

        String[] options = {
                "New Purchase",
                "Purchase History"
        };

        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "Select Purchase Option",
                        "Purchases",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );

        if (choice == 0) {

           PurchaseFrame purchaseFrame =
        new PurchaseFrame(loggedInUser);

         purchaseFrame.setVisible(true);

        } else if (choice == 1) {

            PurchaseHistoryFrame historyFrame =
                    new PurchaseHistoryFrame();

            historyFrame.setVisible(true);
        }
    });
}
if (item.equals("Reports")) {

    button.addActionListener(e -> {

        ReportsFrame reportsFrame =
                new ReportsFrame();

        reportsFrame.setVisible(true);
    });
}
if (item.equals("Users")) {

    button.addActionListener(e -> {

        UserFrame userFrame =
        new UserFrame(loggedInUser);

userFrame.setVisible(true);
    });
}
if (item.equals("Device Management")) {

    button.addActionListener(e -> {

        DeviceManagementFrame deviceManagementFrame =
                new DeviceManagementFrame();

        deviceManagementFrame.setVisible(true);
    });
}

if (item.equals("Settings")) {

    button.addActionListener(e -> {

        SettingsFrame settingsFrame =
                new SettingsFrame(loggedInUser);

        settingsFrame.setVisible(true);
    });
}
if (item.equals("Settings")) {

    button.addActionListener(e -> {

        SettingsFrame settingsFrame =
        new SettingsFrame(loggedInUser);

        settingsFrame.setVisible(true);
    });
}
    sidebar.add(button);
}

        JButton logoutButton =
                new JButton("Logout");

        logoutButton.setFocusPainted(false);

        sidebar.add(logoutButton);

        add(
                sidebar,
                BorderLayout.WEST
        );


        // =========================
        // MAIN CONTENT
        // =========================

        JPanel contentPanel =
                new JPanel(
                        new BorderLayout()
                );
JButton refreshButton =
        new JButton("Refresh Dashboard");

refreshButton.setFocusPainted(false);

refreshButton.addActionListener(
        e -> refreshDashboard()
);

    JLabel dashboardTitle =
        new JLabel(
                "Dashboard",
                SwingConstants.CENTER
        );

dashboardTitle.setFont(
        new Font(
                "Arial",
                Font.BOLD,
                28
        )
);

JPanel dashboardHeader =
        new JPanel(
                new BorderLayout()
        );

dashboardHeader.add(
        dashboardTitle,
        BorderLayout.CENTER
);

dashboardHeader.add(
        refreshButton,
        BorderLayout.EAST
);

contentPanel.add(
        dashboardHeader,
        BorderLayout.NORTH
);
double todaysSales = calculateTodaysSales();

double stockValue = calculateStockValue();

int lowStockItems = calculateLowStockItems();

for (com.retailpos.model.Product product :
        productService.getAllProducts()) {

    if (product.getStockQuantity()
            <= product.getMinimumStock()) {

        lowStockItems++;
    }
}
        // =========================
        // STAT CARDS
        // =========================

        JPanel cardsPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                15,
                                15
                        )
                );

        cardsPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        30,
                        30,
                        30,
                        30
                )
        );

       cardsPanel.add(
        createCard(
                "Products",
                String.valueOf(
                        productService
                                .getAllProducts()
                                .size()
                )
        )
);

      cardsPanel.add(
        createCard(
                "Today's Sales",
                String.format(
                        "₹%.2f",
                        todaysSales
                )
        )
);
      cardsPanel.add(
        createCard(
                "Stock Value",
                String.format(
                        "₹%.2f",
                        stockValue
                )
        )
);

        cardsPanel.add(
        createCard(
                "Low Stock Items",
                String.valueOf(
                        lowStockItems
                )
        )
);
        contentPanel.add(
                cardsPanel,
                BorderLayout.CENTER
        );

        add(
                contentPanel,
                BorderLayout.CENTER
        );

dashboardTimer =
        new Timer(
                2000,
                e -> refreshDashboard()
        );

dashboardTimer.start();
        // =========================
        // LOGOUT
        // =========================

        logoutButton.addActionListener(e -> logout());
    }


    private JPanel createCard(
        String title,
        String value
) {

    JPanel card =
            new JPanel(
                    new BorderLayout()
            );

    card.setBorder(
            BorderFactory.createLineBorder(
                    Color.GRAY
            )
    );

    JLabel titleLabel =
            new JLabel(
                    title,
                    SwingConstants.CENTER
            );

    titleLabel.setFont(
            new Font(
                    "Arial",
                    Font.BOLD,
                    18
            )
    );

    JLabel valueLabel =
            new JLabel(
                    value,
                    SwingConstants.CENTER
            );

    valueLabel.setFont(
            new Font(
                    "Arial",
                    Font.BOLD,
                    30
            )
    );

    if (title.equals("Products")) {
        productsValueLabel = valueLabel;
    }

    if (title.equals("Today's Sales")) {
        todaysSalesValueLabel = valueLabel;
    }

    if (title.equals("Stock Value")) {
        stockValueLabel = valueLabel;
    }

    if (title.equals("Low Stock Items")) {
        lowStockValueLabel = valueLabel;
    }

    card.add(
            titleLabel,
            BorderLayout.NORTH
    );

    card.add(
            valueLabel,
            BorderLayout.CENTER
    );

    return card;
}
  private double calculateTodaysSales() {

    double todaysSales = 0.0;

    java.time.LocalDate today =
            java.time.LocalDate.now();

    for (com.retailpos.model.Sale sale :
            saleService.getAllSales()) {

        if (sale.getCreatedAt() != null &&
                sale.getCreatedAt()
                        .toLocalDate()
                        .equals(today) &&
                "COMPLETED".equalsIgnoreCase(
                        sale.getStatus()
                )) {

            todaysSales += sale.getTotal();
        }
    }

    return todaysSales;
}
private double calculateStockValue() {

    double stockValue = 0.0;

    for (com.retailpos.model.Product product :
            productService.getAllProducts()) {

        stockValue +=
                product.getStockQuantity()
                        * product.getPurchasePrice();
    }

    return stockValue;
}
private int calculateLowStockItems() {

    int lowStockItems = 0;

    for (com.retailpos.model.Product product :
            productService.getAllProducts()) {

        if (product.getStockQuantity()
                <= product.getMinimumStock()) {

            lowStockItems++;
        }
    }

    return lowStockItems;
}
private void refreshDashboard() {

    double todaysSales =
            calculateTodaysSales();

    double stockValue =
            calculateStockValue();

    int lowStockItems =
            calculateLowStockItems();

    int products =
            productService
                    .getAllProducts()
                    .size();

    productsValueLabel.setText(
            String.valueOf(products)
    );

    todaysSalesValueLabel.setText(
            String.format(
                    "₹%.2f",
                    todaysSales
            )
    );

    stockValueLabel.setText(
            String.format(
                    "₹%.2f",
                    stockValue
            )
    );

    lowStockValueLabel.setText(
            String.valueOf(
                    lowStockItems
            )
    );
}
    private void logout() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION
                );

       if (choice ==
        JOptionPane.YES_OPTION) {

    auditLogService.log(
            loggedInUser.getId(),
            loggedInUser.getUsername(),
            "LOGOUT",
            "AUTHENTICATION",
            "User logged out successfully"
    );

    dispose();

    // Login screen will be opened here later.
}
    }
}