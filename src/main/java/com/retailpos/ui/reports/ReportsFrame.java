package com.retailpos.ui.reports;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import com.retailpos.repository.SaleItemRepository;
import com.retailpos.repository.mongodb.SaleItemRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.service.ProductService;
import com.retailpos.model.Purchase;
import com.retailpos.model.Sale;
import com.retailpos.report.ReportService;
import com.retailpos.repository.PurchaseRepository;
import com.retailpos.repository.SaleRepository;
import com.retailpos.repository.mongodb.PurchaseRepositoryImpl;
import com.retailpos.repository.mongodb.SaleRepositoryImpl;

public class ReportsFrame extends JFrame {

    private final ReportService reportService;

    private JTable reportTable;
    private DefaultTableModel reportModel;

    private JComboBox<String> reportTypeComboBox;

    private JLabel totalLabel;
    private JTextField fromDateField;
private JTextField toDateField;

    public ReportsFrame() {

        SaleRepository saleRepository =
                new SaleRepositoryImpl();

        PurchaseRepository purchaseRepository =
                new PurchaseRepositoryImpl();

       SaleItemRepository saleItemRepository =
        new SaleItemRepositoryImpl();

ProductService productService =
        new ProductService(
                new ProductRepositoryImpl()
        );

reportService =
        new ReportService(
                saleRepository,
                purchaseRepository,
                saleItemRepository,
                productService
        );

        setTitle(
                "Retail POS - Reports"
        );

        setSize(
                1100,
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

        createTable();

        createBottomPanel();

        loadSalesReport();
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
                        "Report Selection"
                )
        );

        topPanel.add(
                new JLabel(
                        "Report:"
                )
        );

        reportTypeComboBox =
                new JComboBox<>(
                        new String[]{
                                "Sales Report",
                                "Purchase Report"
                        }
                );

        topPanel.add(
                reportTypeComboBox
        );
topPanel.add(
        new JLabel("From:")
);

fromDateField =
        new JTextField(
                10
        );

topPanel.add(
        fromDateField
);

topPanel.add(
        new JLabel("To:")
);

toDateField =
        new JTextField(
                10
);

topPanel.add(
        toDateField
);
        JButton viewButton =
                new JButton(
                        "View Report"
                );

        viewButton.setFocusPainted(
                false
        );

        viewButton.addActionListener(
                e -> loadSelectedReport()
        );

        topPanel.add(
                viewButton
        );
JButton filterButton =
        new JButton(
                "Filter"
        );

filterButton.setFocusPainted(
        false
);

filterButton.addActionListener(
        e -> filterReportsByDate()
);

topPanel.add(
        filterButton
);
        add(
                topPanel,
                BorderLayout.NORTH
        );
    }

    private void createTable() {

        reportModel =
                new DefaultTableModel(
                        new Object[]{
                                "Type",
                                "Invoice",
                                "Party",
                                "Date",
                                "Subtotal",
                                "Discount",
                                "Tax",
                                "Total",
                                "Payment",
                                "Status"
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

        reportTable =
                new JTable(
                        reportModel
                );

        reportTable.setRowHeight(
                28
        );

        reportTable.setAutoCreateRowSorter(
                true
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        reportTable
                );

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Report Data"
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
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                15,
                                10
                        )
                );

        totalLabel =
                new JLabel(
                        "Total: ₹0.00"
                );
JButton profitButton =
        new JButton(
                "Profit & Loss"
        );

profitButton.setFocusPainted(
        false
);

profitButton.addActionListener(
        e -> showProfitAndLoss()
);

bottomPanel.add(
        profitButton
);
JButton exportButton =
        new JButton(
                "Export CSV"
        );

exportButton.setFocusPainted(
        false
);

exportButton.addActionListener(
        e -> exportReportToCSV()
);

bottomPanel.add(
        exportButton
);
        bottomPanel.add(
                totalLabel
        );

        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.addActionListener(
                e -> loadSelectedReport()
        );

        bottomPanel.add(
                refreshButton
        );

        add(
                bottomPanel,
                BorderLayout.SOUTH
        );
    }

    private void loadSelectedReport() {

        String selectedReport =
                reportTypeComboBox
                        .getSelectedItem()
                        .toString();

        if (selectedReport.equals(
                "Sales Report"
        )) {

            loadSalesReport();

        } else {

            loadPurchaseReport();
        }
    }

    private void loadSalesReport() {

        try {

            List<Sale> sales =
                    reportService
                            .getAllSales();

            reportModel.setRowCount(
                    0
            );

            double grandTotal = 0;

            for (Sale sale : sales) {

                reportModel.addRow(
                        new Object[]{
                                "SALE",
                                sale.getInvoiceNumber(),
                                sale.getCustomerName(),
                                sale.getCreatedAt(),
                                String.format(
                                        "₹%.2f",
                                        sale.getSubtotal()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getDiscount()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getTax()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getTotal()
                                ),
                                sale.getPaymentMethod(),
                                sale.getStatus()
                        }
                );

                grandTotal +=
                        sale.getTotal();
            }

            totalLabel.setText(
                    String.format(
                            "Total Sales: ₹%.2f",
                            grandTotal
                    )
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadPurchaseReport() {

        try {

            List<Purchase> purchases =
                    reportService
                            .getAllPurchases();

            reportModel.setRowCount(
                    0
            );

            double grandTotal = 0;

            for (Purchase purchase :
                    purchases) {

                reportModel.addRow(
                        new Object[]{
                                "PURCHASE",
                                purchase.getInvoiceNumber(),
                                purchase.getSupplierName(),
                                purchase.getCreatedAt(),
                                String.format(
                                        "₹%.2f",
                                        purchase.getSubtotal()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getDiscount()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getTax()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getTotal()
                                ),
                                purchase.getPaymentMethod(),
                                purchase.getStatus()
                        }
                );

                grandTotal +=
                        purchase.getTotal();
            }

            totalLabel.setText(
                    String.format(
                            "Total Purchases: ₹%.2f",
                            grandTotal
                    )
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void filterReportsByDate() {

    String fromDate =
            fromDateField
                    .getText()
                    .trim();

    String toDate =
            toDateField
                    .getText()
                    .trim();

    if (fromDate.isEmpty() ||
            toDate.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter both From Date and To Date.\n"
                        + "Format: yyyy-MM-dd",
                "Date Filter",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter
                        .ofPattern("yyyy-MM-dd");

        java.time.LocalDate from =
                java.time.LocalDate.parse(
                        fromDate,
                        formatter
                );

        java.time.LocalDate to =
                java.time.LocalDate.parse(
                        toDate,
                        formatter
                );

        if (from.isAfter(to)) {

            JOptionPane.showMessageDialog(
                    this,
                    "From Date cannot be after To Date.",
                    "Date Filter",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String selectedReport =
                reportTypeComboBox
                        .getSelectedItem()
                        .toString();

        if (selectedReport.equals(
                "Sales Report"
        )) {

            filterSalesByDate(
                    from,
                    to
            );

        } else {

            filterPurchasesByDate(
                    from,
                    to
            );
        }

    } catch (
            java.time.format.DateTimeParseException ex
    ) {

        JOptionPane.showMessageDialog(
                this,
                "Invalid date format.\n"
                        + "Use: yyyy-MM-dd\n"
                        + "Example: 2026-10-03",
                "Date Filter",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
private void filterSalesByDate(
        java.time.LocalDate from,
        java.time.LocalDate to
) {

    try {

        List<Sale> sales =
                reportService
                        .getAllSales();

        reportModel.setRowCount(
                0
        );

        double grandTotal = 0;

        for (Sale sale : sales) {

            java.time.LocalDate saleDate =
                    sale.getCreatedAt()
                            .toLocalDate();

            if (!saleDate.isBefore(from)
                    && !saleDate.isAfter(to)) {

                reportModel.addRow(
                        new Object[]{
                                "SALE",
                                sale.getInvoiceNumber(),
                                sale.getCustomerName(),
                                sale.getCreatedAt(),
                                String.format(
                                        "₹%.2f",
                                        sale.getSubtotal()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getDiscount()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getTax()
                                ),
                                String.format(
                                        "₹%.2f",
                                        sale.getTotal()
                                ),
                                sale.getPaymentMethod(),
                                sale.getStatus()
                        }
                );

                grandTotal +=
                        sale.getTotal();
            }
        }

        totalLabel.setText(
                String.format(
                        "Total Sales: ₹%.2f",
                        grandTotal
                )
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Report Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
private void filterPurchasesByDate(
        java.time.LocalDate from,
        java.time.LocalDate to
) {

    try {

        List<Purchase> purchases =
                reportService
                        .getAllPurchases();

        reportModel.setRowCount(
                0
        );

        double grandTotal = 0;

        for (Purchase purchase :
                purchases) {

            java.time.LocalDate purchaseDate =
                    purchase.getCreatedAt()
                            .toLocalDate();

            if (!purchaseDate.isBefore(from)
                    && !purchaseDate.isAfter(to)) {

                reportModel.addRow(
                        new Object[]{
                                "PURCHASE",
                                purchase.getInvoiceNumber(),
                                purchase.getSupplierName(),
                                purchase.getCreatedAt(),
                                String.format(
                                        "₹%.2f",
                                        purchase.getSubtotal()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getDiscount()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getTax()
                                ),
                                String.format(
                                        "₹%.2f",
                                        purchase.getTotal()
                                ),
                                purchase.getPaymentMethod(),
                                purchase.getStatus()
                        }
                );

                grandTotal +=
                        purchase.getTotal();
            }
        }

        totalLabel.setText(
                String.format(
                        "Total Purchases: ₹%.2f",
                        grandTotal
                )
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Report Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
private void showProfitAndLoss() {

    try {

        double totalSales =
                reportService
                        .calculateTotalSales();

        double totalPurchases =
                reportService
                        .calculateTotalPurchases();

        double grossProfit =
                reportService
                        .calculateGrossProfit();

        String message =
                String.format(
                        "Profit & Loss Report\n\n"
                                + "Total Sales: ₹%.2f\n"
                                + "Total Purchases: ₹%.2f\n"
                                + "--------------------------\n"
                                + "Gross Profit: ₹%.2f",
                        totalSales,
                        totalPurchases,
                        grossProfit
                );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Profit & Loss",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Profit & Loss Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
private void exportReportToCSV() {

    try {

        javax.swing.JFileChooser fileChooser =
                new javax.swing.JFileChooser();

        fileChooser.setDialogTitle(
                "Save Report as CSV"
        );

        fileChooser.setSelectedFile(
                new java.io.File(
                        "report.csv"
                )
        );

        int result =
                fileChooser.showSaveDialog(
                        this
                );

        if (result !=
                javax.swing.JFileChooser.APPROVE_OPTION) {

            return;
        }

        java.io.File file =
                fileChooser.getSelectedFile();

        java.io.PrintWriter writer =
                new java.io.PrintWriter(
                        file
                );

        // Write column headers
        for (int column = 0;
                column < reportModel.getColumnCount();
                column++) {

            writer.print(
                    reportModel
                            .getColumnName(
                                    column
                            )
            );

            if (column <
                    reportModel.getColumnCount() - 1) {

                writer.print(",");
            }
        }

        writer.println();

        // Write table data
        for (int row = 0;
                row < reportModel.getRowCount();
                row++) {

            for (int column = 0;
                    column < reportModel.getColumnCount();
                    column++) {

                Object value =
                        reportModel.getValueAt(
                                row,
                                column
                        );

                String text =
                        value == null
                                ? ""
                                : value.toString();

                text =
                        text.replace(
                                "\"",
                                "\"\""
                        );

                writer.print(
                        "\"" + text + "\""
                );

                if (column <
                        reportModel.getColumnCount() - 1) {

                    writer.print(",");
                }
            }

            writer.println();
        }

        writer.close();

        JOptionPane.showMessageDialog(
                this,
                "Report exported successfully.\n\n"
                        + file.getAbsolutePath(),
                "Export CSV",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
}