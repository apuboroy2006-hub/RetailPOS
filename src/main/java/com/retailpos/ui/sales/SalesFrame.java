package com.retailpos.ui.sales;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Sale;
import com.retailpos.model.SaleItem;
import com.retailpos.repository.SaleItemRepository;
import com.retailpos.repository.SaleRepository;
import com.retailpos.repository.mongodb.SaleItemRepositoryImpl;
import com.retailpos.repository.mongodb.SaleRepositoryImpl;
import com.retailpos.service.SaleService;

public class SalesFrame extends JFrame {

    private JTable salesTable;
    private JButton detailsButton;
    private JButton refreshButton;
    private JButton printButton;
    private DefaultTableModel salesModel;
    private SaleService saleService;
    private JTextField searchField;

    public SalesFrame() {


        SaleRepository saleRepository =
                new SaleRepositoryImpl();

        SaleItemRepository saleItemRepository =
                new SaleItemRepositoryImpl();

        saleService =
                new SaleService(
                        saleRepository,
                        saleItemRepository
                );

        setTitle("Sales History");

        setSize(1000, 600);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        String[] columns = {
                "Select",
                "Invoice",
                "Customer",
                "Date",
                "Subtotal",
                "Discount",
                "Tax",
                "Total",
                "Payment",
                "Status",
                "Sold By"
        };

        salesModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return column == 0;
                    }

                    @Override
                    public Class<?> getColumnClass(int column) {
                        return column == 0 ? Boolean.class : Object.class;
                    }
                };

        salesTable =
                new JTable(
                        salesModel
                );

        JScrollPane scrollPane =
                new JScrollPane(
                        salesTable
                );

        searchField =
                new JTextField();

        searchField.setToolTipText(
                "Search by invoice or customer"
        );

       JPanel searchPanel =
        new JPanel(
                new java.awt.BorderLayout()
        );

searchPanel.setBorder(
        javax.swing.BorderFactory.createEmptyBorder(
                10, 10, 10, 10
        )
);

searchPanel.add(
        searchField,
        java.awt.BorderLayout.CENTER
);

JButton searchButton = new JButton("Search");
searchButton.addActionListener(e -> searchSales());
searchPanel.add(searchButton, java.awt.BorderLayout.EAST);

add(
        searchPanel,
        java.awt.BorderLayout.NORTH
);

add(
        scrollPane,
        java.awt.BorderLayout.CENTER
);
detailsButton =
        new JButton("View Details");

refreshButton =
        new JButton("Refresh");

JPanel bottomPanel =
        new JPanel();
javax.swing.JCheckBox selectAllCheckBox = new javax.swing.JCheckBox("Select All");
selectAllCheckBox.addActionListener(e -> {
    boolean checked = selectAllCheckBox.isSelected();
    for (int row = 0; row < salesModel.getRowCount(); row++) {
        salesModel.setValueAt(checked, row, 0);
    }
});
bottomPanel.add(selectAllCheckBox);


bottomPanel.add(
        detailsButton
);

bottomPanel.add(
        refreshButton
);
printButton =
        new JButton("Print");

bottomPanel.add(
        printButton
);
add(
        bottomPanel,
        java.awt.BorderLayout.SOUTH
);

refreshButton.addActionListener(e -> {
    searchField.setText("");
    loadSales();
});
printButton.addActionListener(e -> printSelectedInvoices());

        detailsButton.addActionListener(e -> {

            int selectedRow =
                    salesTable.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a sale first.",
                        "Sale Details",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            String invoiceNumber =
                    salesTable.getValueAt(selectedRow, 1).toString();

            java.util.Optional<Sale> saleOptional =
                    saleService.findByInvoiceNumber(
                            invoiceNumber
                    );

            if (saleOptional.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Sale not found.",
                        "Sale Details",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Sale sale =
                    saleOptional.get();

            java.util.List<SaleItem> items =
                    saleService.getSaleItems(
                            sale.getId()
                    );

            StringBuilder details =
                    new StringBuilder();

            details.append("Invoice: ")
                    .append(sale.getInvoiceNumber())
                    .append("\n");

            details.append("Customer: ")
                    .append(sale.getCustomerName())
                    .append("\n");

            details.append("Payment: ")
                    .append(sale.getPaymentMethod())
                    .append("\n");

            details.append("Status: ")
                    .append(sale.getStatus())
                    .append("\n\n");

            for (SaleItem item : items) {

                details.append("Product: ")
                        .append(item.getProductName())
                        .append("\n");

                details.append("SKU: ")
                        .append(item.getSku())
                        .append("\n");

                details.append("Quantity: ")
                        .append(item.getQuantity())
                        .append("\n");

                details.append("Unit Price: ")
                        .append(item.getUnitPrice())
                        .append("\n");

                details.append("Discount: ")
                        .append(item.getDiscount())
                        .append("\n");

                details.append("Total: ")
                        .append(item.getTotal())
                        .append("\n\n");
            }

            details.append("Subtotal: ")
                    .append(sale.getSubtotal())
                    .append("\n");

            details.append("Discount: ")
                    .append(sale.getDiscount())
                    .append("\n");

            details.append("Tax: ")
                    .append(sale.getTax())
                    .append("\n");

            details.append("Total: ")
                    .append(sale.getTotal());

            JOptionPane.showMessageDialog(
                    this,
                    details.toString(),
                    "Sale Details",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        searchField.addActionListener(e -> {
            searchSales();
        });

        loadSales();
    }

    private void printSelectedInvoices() {
        if (salesTable.isEditing()) {
            salesTable.getCellEditor().stopCellEditing();
        }

        int checkedCount = 0;
        for (int row = 0; row < salesModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(salesModel.getValueAt(row, 0))) {
                checkedCount++;
            }
        }

        if (checkedCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "No invoices are checked. Tick the checkbox in the Select column first.",
                    "Print Invoices", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        java.util.List<String> invoiceTexts = new java.util.ArrayList<>();

        for (int row = 0; row < salesModel.getRowCount(); row++) {
            if (!Boolean.TRUE.equals(salesModel.getValueAt(row, 0))) {
                continue;
            }

            String invoiceNumber = String.valueOf(salesModel.getValueAt(row, 1));
            java.util.Optional<Sale> found =
                    saleService.findByInvoiceNumber(invoiceNumber);

            if (found.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Sale not found: " + invoiceNumber,
                        "Print Invoices", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Sale sale = found.get();
            invoiceTexts.add(createInvoiceText(
                    sale, saleService.getSaleItems(sale.getId())));
        }

        if (invoiceTexts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one invoice checkbox.",
                    "Print Invoices", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(
        this,
        "Checked invoices: " + checkedCount
                + "\nInvoices prepared for printing: " + invoiceTexts.size(),
        "Print Diagnostic",
        JOptionPane.INFORMATION_MESSAGE
);
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        java.awt.print.PageFormat format = job.defaultPage();

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex < 0 || pageIndex >= invoiceTexts.size()) {
                return java.awt.print.Printable.NO_SUCH_PAGE;
            }

            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            try {
                g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                String[] lines = invoiceTexts.get(pageIndex).split("\\R", -1);
                java.awt.Font font = new java.awt.Font(
                        java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 10);
                g.setFont(font);
                java.awt.FontMetrics metrics = g.getFontMetrics();

                double maxWidth = 1;
                for (String line : lines) {
                    maxWidth = Math.max(maxWidth, metrics.stringWidth(line));
                }

                double scale = Math.min(1.0, Math.min(
                        pageFormat.getImageableWidth() / maxWidth,
                        pageFormat.getImageableHeight()
                                / Math.max(1, lines.length * metrics.getHeight() + 10)));

                g.scale(scale, scale);
                int y = metrics.getAscent();
                for (String line : lines) {
                    g.drawString(line, 0, y);
                    y += metrics.getHeight();
                }

                return java.awt.print.Printable.PAGE_EXISTS;
            } finally {
                g.dispose();
            }
        }, format);

        if (!job.printDialog()) {
            return;
        }

        try {
            job.print();
            JOptionPane.showMessageDialog(this,
                    invoiceTexts.size() + " invoice(s) sent to print.",
                    "Print Invoices", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Printing failed: " + ex.getMessage(),
                    "Print Invoices", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadSales() {

        salesModel.setRowCount(0);

        java.util.List<Sale> sales =
                saleService.getAllSales();

        for (Sale sale : sales) {

            salesModel.addRow(
                    new Object[]{
                            Boolean.FALSE,
                            sale.getInvoiceNumber(),
                            sale.getCustomerName(),
                            sale.getCreatedAt(),
                            sale.getSubtotal(),
                            sale.getDiscount(),
                            sale.getTax(),
                            sale.getTotal(),
                            sale.getPaymentMethod(),
                            sale.getStatus(),
                            sale.getSoldBy()
                    }
            );
        }
    }

   private void searchSales() {

    String searchText =
            searchField.getText()
                    .trim()
                    .toLowerCase();

    salesModel.setRowCount(0);

    java.util.List<Sale> sales =
            saleService.getAllSales();

    for (Sale sale : sales) {

        String invoice =
                sale.getInvoiceNumber() == null
                        ? ""
                        : sale.getInvoiceNumber()
                                .toLowerCase();

        String customer =
                sale.getCustomerName() == null
                        ? ""
                        : sale.getCustomerName()
                                .toLowerCase();

        if (searchText.isEmpty()
                || invoice.contains(searchText)
                || customer.contains(searchText)) {

            salesModel.addRow(
                    new Object[]{
                            Boolean.FALSE,
                            sale.getInvoiceNumber(),
                            sale.getCustomerName(),
                            sale.getCreatedAt(),
                            sale.getSubtotal(),
                            sale.getDiscount(),
                            sale.getTax(),
                            sale.getTotal(),
                            sale.getPaymentMethod(),
                            sale.getStatus(),
                            sale.getSoldBy()
                    }
            );
        }
    }
}
private String createInvoiceText(
        Sale sale,
        java.util.List<SaleItem> items
) {

    StringBuilder invoice =
            new StringBuilder();

    invoice.append("==============================\n");
    invoice.append("          RETAIL POS\n");
    invoice.append("         SALES INVOICE\n");
    invoice.append("==============================\n\n");

    invoice.append("Invoice: ")
            .append(sale.getInvoiceNumber())
            .append("\n");

    invoice.append("Customer: ")
            .append(sale.getCustomerName())
            .append("\n");

    invoice.append("Date: ")
            .append(sale.getCreatedAt())
            .append("\n");

    invoice.append("Payment: ")
            .append(sale.getPaymentMethod())
            .append("\n");

    invoice.append("Sold By: ")
            .append(sale.getSoldBy())
            .append("\n");

    invoice.append("------------------------------\n");

    for (SaleItem item : items) {

        invoice.append("Product: ")
                .append(item.getProductName())
                .append("\n");

        invoice.append("SKU: ")
                .append(item.getSku())
                .append("\n");

        invoice.append("Quantity: ")
                .append(item.getQuantity())
                .append("\n");

        invoice.append("Unit Price: ")
                .append(item.getUnitPrice())
                .append("\n");

        invoice.append("Discount: ")
                .append(item.getDiscount())
                .append("\n");

        invoice.append("Item Total: ")
                .append(item.getTotal())
                .append("\n");

        invoice.append("------------------------------\n");
    }

    invoice.append("Subtotal: ")
            .append(sale.getSubtotal())
            .append("\n");

    invoice.append("Discount: ")
            .append(sale.getDiscount())
            .append("\n");

    invoice.append("Tax: ")
            .append(sale.getTax())
            .append("\n");

    invoice.append("TOTAL: ")
            .append(sale.getTotal())
            .append("\n");

    invoice.append("==============================\n");
    invoice.append("        Thank You!\n");
    invoice.append("==============================");

    return invoice.toString();
}
}
