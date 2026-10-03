package com.retailpos.ui.purchases;

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
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Purchase;
import com.retailpos.model.PurchaseItem;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.PurchaseItemRepository;
import com.retailpos.repository.PurchaseRepository;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseItemRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseRepositoryImpl;
import com.retailpos.service.ProductService;
import com.retailpos.service.PurchaseService;

public class PurchaseDetailsFrame extends JFrame {

    private final PurchaseService purchaseService;

    private JTable itemTable;
    private DefaultTableModel itemModel;

    private JLabel invoiceLabel;
    private JLabel supplierLabel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JLabel paymentLabel;
    private JLabel statusLabel;
    private JLabel purchasedByLabel;

    public PurchaseDetailsFrame(
            String invoiceNumber
    ) {

        PurchaseRepository purchaseRepository =
                new PurchaseRepositoryImpl();

        PurchaseItemRepository purchaseItemRepository =
                new PurchaseItemRepositoryImpl();

        ProductRepository productRepository =
                new ProductRepositoryImpl();

        ProductService productService =
                new ProductService(
                        productRepository
                );

        purchaseService =
                new PurchaseService(
                        purchaseRepository,
                        purchaseItemRepository,
                        productService
                );

        setTitle(
                "Retail POS - Purchase Details"
        );

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

        createTable();

        createBottomPanel();

        loadPurchase(
                invoiceNumber
        );
    }

    private void createTopPanel() {

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                15,
                                10
                        )
                );

        topPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Purchase Information"
                )
        );

        invoiceLabel =
                new JLabel(
                        "Invoice: "
                );

        supplierLabel =
                new JLabel(
                        "Supplier: "
                );

        topPanel.add(
                invoiceLabel
        );

        topPanel.add(
                supplierLabel
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );
    }

    private void createTable() {

        itemModel =
                new DefaultTableModel(
                        new Object[]{
                                "Product",
                                "SKU",
                                "Quantity",
                                "Purchase Price",
                                "Discount",
                                "Total"
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

        itemTable =
                new JTable(
                        itemModel
                );

        itemTable.setRowHeight(
                28
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        itemTable
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

        JPanel summaryPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                15,
                                10
                        )
                );

        subtotalLabel =
                new JLabel(
                        "Subtotal: ₹0.00"
                );

        discountLabel =
                new JLabel(
                        "Discount: ₹0.00"
                );

        taxLabel =
                new JLabel(
                        "Tax: ₹0.00"
                );

        totalLabel =
                new JLabel(
                        "Total: ₹0.00"
                );

        paymentLabel =
                new JLabel(
                        "Payment: "
                );

        statusLabel =
                new JLabel(
                        "Status: "
                );

        purchasedByLabel =
                new JLabel(
                        "Purchased By: "
                );

        summaryPanel.add(
                subtotalLabel
        );

        summaryPanel.add(
                discountLabel
        );

        summaryPanel.add(
                taxLabel
        );

        summaryPanel.add(
                totalLabel
        );

        summaryPanel.add(
                paymentLabel
        );

        summaryPanel.add(
                statusLabel
        );

        summaryPanel.add(
                purchasedByLabel
        );

        bottomPanel.add(
                summaryPanel,
                BorderLayout.CENTER
        );

        JButton closeButton =
                new JButton(
                        "Close"
                );

        closeButton.setFocusPainted(
                false
        );

        closeButton.addActionListener(
                e -> dispose()
        );

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        buttonPanel.add(
                closeButton
        );

        bottomPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(
                bottomPanel,
                BorderLayout.SOUTH
        );
    }

    private void loadPurchase(
            String invoiceNumber
    ) {

        try {

            Purchase purchase =
                    purchaseService
                            .findByInvoiceNumber(
                                    invoiceNumber
                            )
                            .orElse(null);

            if (purchase == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Purchase not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                dispose();

                return;
            }

            invoiceLabel.setText(
                    "Invoice: "
                            + purchase
                                    .getInvoiceNumber()
            );

            supplierLabel.setText(
                    "Supplier: "
                            + purchase
                                    .getSupplierName()
            );

            subtotalLabel.setText(
                    String.format(
                            "Subtotal: ₹%.2f",
                            purchase.getSubtotal()
                    )
            );

            discountLabel.setText(
                    String.format(
                            "Discount: ₹%.2f",
                            purchase.getDiscount()
                    )
            );

            taxLabel.setText(
                    String.format(
                            "Tax: ₹%.2f",
                            purchase.getTax()
                    )
            );

            totalLabel.setText(
                    String.format(
                            "Total: ₹%.2f",
                            purchase.getTotal()
                    )
            );

            paymentLabel.setText(
                    "Payment: "
                            + purchase
                                    .getPaymentMethod()
            );

            statusLabel.setText(
                    "Status: "
                            + purchase
                                    .getStatus()
            );

            purchasedByLabel.setText(
                    "Purchased By: "
                            + purchase
                                    .getPurchasedBy()
            );

            loadItems(
                    purchase.getId()
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadItems(
            String purchaseId
    ) {

        List<PurchaseItem> items =
                purchaseService
                        .getPurchaseItems(
                                purchaseId
                        );

        itemModel.setRowCount(
                0
        );

        for (PurchaseItem item :
                items) {

            itemModel.addRow(
                    new Object[]{
                            item.getProductName(),
                            item.getSku(),
                            item.getQuantity(),
                            String.format(
                                    "₹%.2f",
                                    item.getUnitPrice()
                            ),
                            String.format(
                                    "₹%.2f",
                                    item.getDiscount()
                            ),
                            String.format(
                                    "₹%.2f",
                                    item.getTotal()
                            )
                    }
            );
        }
    }
}