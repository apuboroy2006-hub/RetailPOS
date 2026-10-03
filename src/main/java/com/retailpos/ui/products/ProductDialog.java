package com.retailpos.ui.products;

import com.retailpos.model.Product;
import com.retailpos.repository.ProductRepository;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.service.ProductService;

import javax.swing.*;
import java.awt.*;

public class ProductDialog extends JDialog {

    private final ProductService productService;
    private final Product existingProduct;

    private final JTextField nameField;
    private final JTextField skuField;
    private final JTextField barcodeField;
    private final JTextField categoryField;
    private final JTextField purchasePriceField;
    private final JTextField sellingPriceField;
    private final JTextField stockQuantityField;
    private final JTextField minimumStockField;
    private final JTextField supplierField;

    private boolean saved = false;

    public ProductDialog(
            JFrame parent,
            Product product
    ) {

        super(
                parent,
                product == null
                        ? "Add Product"
                        : "Edit Product",
                true
        );

        existingProduct = product;

        ProductRepository productRepository =
                new ProductRepositoryImpl();

        productService =
                new ProductService(productRepository);

        setSize(500, 550);
        setLocationRelativeTo(parent);
        setResizable(false);

        // =========================
        // FORM PANEL
        // =========================

        JPanel formPanel =
                new JPanel(
                        new GridLayout(
                                9,
                                2,
                                10,
                                10
                        )
                );

        formPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        10,
                        20
                )
        );

        // Product Name
        formPanel.add(
                new JLabel("Product Name:")
        );

        nameField =
                new JTextField();

        formPanel.add(nameField);

        // SKU
        formPanel.add(
                new JLabel("SKU:")
        );

        skuField =
                new JTextField();

        formPanel.add(skuField);

        // Barcode
        formPanel.add(
                new JLabel("Barcode:")
        );

        barcodeField =
                new JTextField();

        formPanel.add(barcodeField);

        // Category
        formPanel.add(
                new JLabel("Category:")
        );

        categoryField =
                new JTextField();

        formPanel.add(categoryField);

        // Purchase Price
        formPanel.add(
                new JLabel("Purchase Price:")
        );

        purchasePriceField =
                new JTextField();

        formPanel.add(
                purchasePriceField
        );

        // Selling Price
        formPanel.add(
                new JLabel("Selling Price:")
        );

        sellingPriceField =
                new JTextField();

        formPanel.add(
                sellingPriceField
        );

        // Stock Quantity
        formPanel.add(
                new JLabel("Stock Quantity:")
        );

        stockQuantityField =
                new JTextField();

        formPanel.add(
                stockQuantityField
        );

        // Minimum Stock
        formPanel.add(
                new JLabel("Minimum Stock:")
        );

        minimumStockField =
                new JTextField();

        formPanel.add(
                minimumStockField
        );

        // Supplier
        formPanel.add(
                new JLabel("Supplier:")
        );

        supplierField =
                new JTextField();

        formPanel.add(
                supplierField
        );

        add(
                formPanel,
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

        JButton saveButton =
                new JButton("Save");

        JButton cancelButton =
                new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        // =========================
        // EDIT MODE
        // =========================

        if (existingProduct != null) {
            loadProductData();
        }

        // =========================
        // ACTIONS
        // =========================

        saveButton.addActionListener(
                e -> saveProduct()
        );

        cancelButton.addActionListener(
                e -> dispose()
        );
    }

    // =========================
    // LOAD PRODUCT
    // =========================

    private void loadProductData() {

        nameField.setText(
                existingProduct.getName()
        );

        skuField.setText(
                existingProduct.getSku()
        );

        barcodeField.setText(
                existingProduct.getBarcode()
        );

        categoryField.setText(
                existingProduct.getCategory()
        );

        purchasePriceField.setText(
                String.valueOf(
                        existingProduct.getPurchasePrice()
                )
        );

        sellingPriceField.setText(
                String.valueOf(
                        existingProduct.getSellingPrice()
                )
        );

        stockQuantityField.setText(
                String.valueOf(
                        existingProduct.getStockQuantity()
                )
        );

        minimumStockField.setText(
                String.valueOf(
                        existingProduct.getMinimumStock()
                )
        );

        supplierField.setText(
                existingProduct.getSupplier()
        );
    }

    // =========================
    // SAVE PRODUCT
    // =========================

    private void saveProduct() {

        try {

            String name =
                    nameField.getText().trim();

            String sku =
                    skuField.getText().trim();

            String barcode =
                    barcodeField.getText().trim();

            String category =
                    categoryField.getText().trim();

            String supplier =
                    supplierField.getText().trim();

            double purchasePrice =
                    Double.parseDouble(
                            purchasePriceField
                                    .getText()
                                    .trim()
                    );

            double sellingPrice =
                    Double.parseDouble(
                            sellingPriceField
                                    .getText()
                                    .trim()
                    );

            int stockQuantity =
                    Integer.parseInt(
                            stockQuantityField
                                    .getText()
                                    .trim()
                    );

            int minimumStock =
                    Integer.parseInt(
                            minimumStockField
                                    .getText()
                                    .trim()
                    );

            // =========================
            // ADD PRODUCT
            // =========================

            if (existingProduct == null) {

                productService.createProduct(
                        name,
                        sku,
                        barcode,
                        category,
                        purchasePrice,
                        sellingPrice,
                        stockQuantity,
                        minimumStock,
                        supplier
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Product added successfully."
                );

            }

            // =========================
            // UPDATE PRODUCT
            // =========================

            else {

                existingProduct.setName(name);
                existingProduct.setSku(sku);
                existingProduct.setBarcode(barcode);
                existingProduct.setCategory(category);
                existingProduct.setPurchasePrice(
                        purchasePrice
                );
                existingProduct.setSellingPrice(
                        sellingPrice
                );
                existingProduct.setStockQuantity(
                        stockQuantity
                );
                existingProduct.setMinimumStock(
                        minimumStock
                );
                existingProduct.setSupplier(
                        supplier
                );

                productService.updateProduct(
                        existingProduct
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Product updated successfully."
                );
            }

            saved = true;

            dispose();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers for price and stock fields.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "An unexpected error occurred:\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}