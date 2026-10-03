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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.retailpos.model.Purchase;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.PurchaseItemRepository;
import com.retailpos.repository.PurchaseRepository;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseItemRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseRepositoryImpl;
import com.retailpos.service.ProductService;
import com.retailpos.service.PurchaseService;

public class PurchaseHistoryFrame extends JFrame {

    private final PurchaseService purchaseService;

    private JTable purchaseTable;
    private DefaultTableModel purchaseModel;
private JButton detailsButton;
    private JTextField searchField;

    public PurchaseHistoryFrame() {

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
                "Retail POS - Purchase History"
        );

        setSize(
                1100,
                600
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

        loadPurchases();
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
                        "Search Purchases"
                )
        );

        topPanel.add(
                new JLabel("Search:")
        );

        searchField =
                new JTextField(
                        25
                );

        topPanel.add(
                searchField
        );

        JButton searchButton =
                new JButton(
                        "Search"
                );

        searchButton.setFocusPainted(
                false
        );

        searchButton.addActionListener(
                e -> searchPurchases()
        );

        topPanel.add(
                searchButton
        );

        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.addActionListener(
                e -> loadPurchases()
        );

        topPanel.add(
                refreshButton
        );
        detailsButton =
        new JButton(
                "View Details"
        );

detailsButton.setFocusPainted(
        false
);

detailsButton.addActionListener(
        e -> viewPurchaseDetails()
);

topPanel.add(
        detailsButton
);
        add(
                topPanel,
                BorderLayout.NORTH
        );
    }

    private void createTable() {

        purchaseModel =
                new DefaultTableModel(
                        new Object[]{
                                "Invoice",
                                "Supplier",
                                "Date",
                                "Subtotal",
                                "Discount",
                                "Tax",
                                "Total",
                                "Payment",
                                "Status",
                                "Purchased By"
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

        purchaseTable.setAutoCreateRowSorter(
                true
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        purchaseTable
                );

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Purchase History"
                )
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );
    }

    private void loadPurchases() {

        try {

            List<Purchase> purchases =
                    purchaseService
                            .getAllPurchases();

            displayPurchases(
                    purchases
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

    private void searchPurchases() {

        String searchText =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        if (searchText.isEmpty()) {

            loadPurchases();

            return;
        }

        try {

            List<Purchase> purchases =
                    purchaseService
                            .getAllPurchases();

            purchases.removeIf(
                    purchase ->
                            !purchase
                                    .getInvoiceNumber()
                                    .toLowerCase()
                                    .contains(searchText)
                            &&
                            !purchase
                                    .getSupplierName()
                                    .toLowerCase()
                                    .contains(searchText)
            );

            displayPurchases(
                    purchases
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Search Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayPurchases(
            List<Purchase> purchases
    ) {

        purchaseModel.setRowCount(
                0
        );

        for (Purchase purchase :
                purchases) {

            purchaseModel.addRow(
                    new Object[]{
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
                            purchase.getStatus(),
                            purchase.getPurchasedBy()
                    }
            );
        }
    }
   private void viewPurchaseDetails() {

    int selectedRow =
            purchaseTable.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a purchase first.",
                "Purchase Details",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int modelRow =
            purchaseTable.convertRowIndexToModel(
                    selectedRow
            );

    String invoiceNumber =
            purchaseModel
                    .getValueAt(
                            modelRow,
                            0
                    )
                    .toString();

    PurchaseDetailsFrame detailsFrame =
            new PurchaseDetailsFrame(
                    invoiceNumber
            );

    detailsFrame.setVisible(true);
}
}