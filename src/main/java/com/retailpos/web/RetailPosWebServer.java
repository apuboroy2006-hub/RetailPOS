package com.retailpos.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.retailpos.model.AuditLog;
import com.retailpos.repository.mongodb.AuditLogRepositoryImpl;
import com.retailpos.service.AuditLogService;
import com.retailpos.service.BackupService;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.retailpos.model.Customer;
import com.retailpos.model.Device;
import com.retailpos.model.Product;
import com.retailpos.model.Purchase;
import com.retailpos.model.Sale;
import com.retailpos.model.SaleItem;
import com.retailpos.model.Session;
import com.retailpos.model.Settings;
import com.retailpos.model.StockTransaction;
import com.retailpos.model.User;
import com.retailpos.repository.DeviceRepository;
import com.retailpos.repository.mongodb.CustomerRepositoryImpl;
import com.retailpos.repository.mongodb.DeviceRepositoryImpl;
import com.retailpos.repository.mongodb.OtpRepositoryImpl;
import com.retailpos.repository.mongodb.ProductRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseItemRepositoryImpl;
import com.retailpos.repository.mongodb.PurchaseRepositoryImpl;
import com.retailpos.repository.mongodb.SaleItemRepositoryImpl;
import com.retailpos.repository.mongodb.SaleRepositoryImpl;
import com.retailpos.repository.mongodb.SessionRepositoryImpl;
import com.retailpos.repository.mongodb.SettingsRepositoryImpl;
import com.retailpos.repository.mongodb.StockTransactionRepositoryImpl;
import com.retailpos.repository.mongodb.UserRepositoryImpl;
import com.retailpos.security.AuthService;
import com.retailpos.security.EmailService;
import com.retailpos.security.OtpService;
import com.retailpos.security.SessionService;
import com.retailpos.service.CustomerService;
import com.retailpos.service.InventoryService;
import com.retailpos.service.ProductService;
import com.retailpos.service.PurchaseService;
import com.retailpos.service.SaleService;
import com.retailpos.service.SettingsService;
import com.retailpos.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
public class RetailPosWebServer {

    private HttpServer server;

    private final ProductService productService;
    private final SaleService saleService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;
    private final PurchaseService purchaseService;
    private final UserService userService;
    private final SessionService sessionService;
    private final AuthService authService;
    private final OtpService otpService;
    private final DeviceRepository deviceRepository;
    private final SettingsService settingsService;
    private final Map<String, PendingWebLogin> pendingWebLogins =
        new ConcurrentHashMap<>();
    public RetailPosWebServer() {

        ProductRepositoryImpl productRepository =
                new ProductRepositoryImpl();

        SaleRepositoryImpl saleRepository =
                new SaleRepositoryImpl();

SaleItemRepositoryImpl saleItemRepository =
        new SaleItemRepositoryImpl();
        productService =
                new ProductService(productRepository);

        saleService =
        new SaleService(
                saleRepository,
                saleItemRepository
        );

            CustomerRepositoryImpl customerRepository =
                new CustomerRepositoryImpl();

           customerService =
                new CustomerService(customerRepository);

        StockTransactionRepositoryImpl transactionRepository =
                new StockTransactionRepositoryImpl();

        inventoryService =
                new InventoryService(
                        productRepository,
                        transactionRepository
                );
                PurchaseRepositoryImpl purchaseRepository =
        new PurchaseRepositoryImpl();

PurchaseItemRepositoryImpl purchaseItemRepository =
        new PurchaseItemRepositoryImpl();

purchaseService = new PurchaseService(
        purchaseRepository,
        purchaseItemRepository,
        productService
);
       UserRepositoryImpl userRepository =
        new UserRepositoryImpl();

userService = new UserService(userRepository);

SessionRepositoryImpl sessionRepository =
        new SessionRepositoryImpl();

sessionService =
        new SessionService(sessionRepository);
       deviceRepository = new DeviceRepositoryImpl();
       SettingsRepositoryImpl settingsRepository =
        new SettingsRepositoryImpl();

settingsService =
        new SettingsService(settingsRepository);

authService =
        new AuthService(
                userRepository,
                deviceRepository
        );
        OtpRepositoryImpl otpRepository =
        new OtpRepositoryImpl();

otpService =
        new OtpService(
                otpRepository,
                userRepository
        );
    }

    public void start() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080),
                0
        );
        server.createContext(
        "/verify-otp",
        this::handleVerifyOtp
);
        server.createContext(
        "/login",
        this::handleLogin
);
        server.createContext(
        "/logout",
        this::handleLogout
);
        server.createContext(
        "/inventory",
        this::handleInventory
);
        server.createContext(
        "/customers",
        this::handleCustomers
);
        server.createContext(
                "/",
                this::handleHome
        );
        server.createContext(
        "/products",
        this::handleProducts
);
server.createContext(
        "/pos",
        this::handlePos
);
server.createContext(
        "/barcode-lookup",
        this::handleBarcodeLookup
);
server.createContext(
        "/complete-sale",
        this::handleCompleteSale
);
server.createContext(
        "/sales",
        this::handleSales
);
server.createContext(
        "/sale-details",
        this::handleSaleDetails
);
        server.createContext(
        "/purchases",
        this::handlePurchases
);
        server.createContext(
        "/users",
        this::handleUsers
);
       server.createContext(
        "/settings",
        this::handleSettings
);
server.createContext("/audit-logs", this::handleAuditLogs);
server.createContext("/devices", this::handleDevices);
server.createContext("/device-status-data", this::handleDeviceStatusData);
server.createContext("/session-heartbeat", this::handleSessionHeartbeat);
server.createContext("/backup", this::handleDatabaseBackup);
server.createContext(
        "/reports",
        this::handleReports
);
        server.setExecutor(null);

        server.start();

        System.out.println("====================================");
        System.out.println("Retail POS Web Server Started");
        System.out.println("PC:      http://localhost:8080");
        System.out.println("Network: http://10.190.235.63:8080");
        System.out.println("====================================");
    }

    private void handleHome(
            HttpExchange exchange

    ) throws IOException {

        Session session = requireAuth(exchange);

        if (session == null) {
            return;
        }

        try {

            List<Product> products =

                    productService.getAllProducts();

            List<Sale> sales =
                    saleService.getAllSales();

            int productCount =
                    products.size();

            double todaySales =
                    calculateTodaysSales(sales);

            double stockValue =
                    calculateStockValue(products);

            int lowStockCount =
                    calculateLowStock(products);

           User currentUser = getCurrentUser(session);

String userRole = currentUser == null
        ? ""
        : currentUser.getRole();
 String username = currentUser == null
        ? ""
        : currentUser.getUsername();
String html =
       createDashboardHtml(
        productCount,
        todaySales,
        stockValue,
        lowStockCount,
        userRole,
        username
);

            byte[] bytes =
                    withSessionPolling(exchange, html).getBytes(
                            StandardCharsets.UTF_8
                    );

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            try (
                    OutputStream output =
                            exchange.getResponseBody()
            ) {

                output.write(bytes);
            }

        } catch (Exception e) {

            e.printStackTrace();

            String error =
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                        <title>Retail POS Error</title>
                    </head>
                    <body>
                        <h2>Retail POS Error</h2>
                        <p>%s</p>
                    </body>
                    </html>
                    """.formatted(
                            escapeHtml(
                                    e.getMessage()
                            )
                    );

            byte[] bytes =
                    error.getBytes(
                            StandardCharsets.UTF_8
                    );

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    500,
                    bytes.length
            );

            try (
                    OutputStream output =
                            exchange.getResponseBody()
            ) {

                output.write(bytes);
            }
        }
    }

    private double calculateTodaysSales(
            List<Sale> sales
    ) {

        LocalDate today =
                LocalDate.now();

        double total = 0.0;

        for (Sale sale : sales) {

            if (sale == null) {
                continue;
            }

            if (!"COMPLETED".equalsIgnoreCase(
                    sale.getStatus()
            )) {
                continue;
            }

            if (sale.getCreatedAt() == null) {
                continue;
            }

            if (sale.getCreatedAt()
                    .toLocalDate()
                    .equals(today)) {

                total += sale.getTotal();
            }
        }

        return total;
    }

    private double calculateStockValue(
            List<Product> products
    ) {

        double total = 0.0;

        for (Product product : products) {

            if (product == null) {
                continue;
            }

            total +=
                    product.getStockQuantity()
                    * product.getPurchasePrice();
        }

        return total;
    }

    private int calculateLowStock(
            List<Product> products
    ) {

        int count = 0;

        for (Product product : products) {

            if (product == null) {
                continue;
            }

            if (product.getStockQuantity()
                    <= product.getMinimumStock()) {

                count++;
            }
        }

        return count;
    }

  private String createDashboardHtml(
        int productCount,
        double todaySales,
        double stockValue,
        int lowStockCount,
        String userRole,
        String username
) {
String posButton = """
        <button class="button"
            onclick="window.location.href='/pos'">
            POS
        </button>
        """;

String productsButton = """
        <button class="button"
            onclick="window.location.href='/products'">
            Products
        </button>
        """;

String inventoryButton = """
        <button class="button"
            onclick="window.location.href='/inventory'">
            Inventory
        </button>
        """;

String salesButton = """
        <button class="button"
            onclick="window.location.href='/sales'">
            Sales
        </button>
        """;

String purchasesButton = """
        <button class="button"
            onclick="window.location.href='/purchases'">
            Purchases
        </button>
        """;

String customersButton = """
        <button class="button"
            onclick="window.location.href='/customers'">
            Customers
        </button>
        """;

String suppliersButton = """
        <button class="button"
            onclick="alert('Suppliers web page is not implemented yet.')">
            Suppliers
        </button>
        """;

String reportsButton = """
        <button class="button"
            onclick="window.location.href='/reports'">
            Reports
        </button>
        """;

String usersButton = """
        <button class="button"
            onclick="window.location.href='/users'">
            Users
        </button>
        """;

boolean isAdmin = "ADMIN".equals(userRole);
boolean isManager = "MANAGER".equals(userRole);

String menuPos = posButton;
String menuProducts = productsButton;
String menuInventory = inventoryButton;
String menuSales = salesButton;
String menuCustomers = customersButton;
String menuSuppliers = suppliersButton;

String menuPurchases = (isAdmin || isManager)
        ? purchasesButton : "";

String menuReports = (isAdmin || isManager)
        ? reportsButton : "";

String menuUsers = isAdmin ? usersButton : "";

String settingsButton = """
        <button class="button"
            onclick="window.location.href='/settings'">
            Settings
        </button>
        """;

String devicesButton = """
        <button class="button"
            onclick="window.location.href='/devices'">
            Device Management
        </button>
        """;

String menuSettings = isAdmin ? settingsButton : "";
String menuDevices = isAdmin ? devicesButton : "";
        return """
                <!DOCTYPE html>
                <html>
                <head>

                    <meta charset="UTF-8">

                    <meta
                        name="viewport"
                        content="width=device-width,
                        initial-scale=1.0"
                    >

                    <title>Retail POS Dashboard</title>

                    <style>

                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            font-family:
                                Arial, sans-serif;
                            background: #f4f6f8;
                            color: #222;
                        }

                        .header {
                            background: #1f2937;
                            color: white;
                            padding: 18px;
                            text-align: center;
                        }

                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }

                        .container {
                            max-width: 1100px;
                            margin: auto;
                            padding: 20px;
                        }

                        .title {
                            margin-bottom: 20px;
                        }

                        .title h2 {
                            margin: 0;
                            font-size: 26px;
                        }

                        .title p {
                            color: #666;
                        }

                        .cards {
                            display: grid;
                            grid-template-columns:
                                repeat(
                                    4,
                                    1fr
                                );
                            gap: 15px;
                        }

                        .card {
                            background: white;
                            padding: 20px;
                            border-radius: 12px;
                            box-shadow:
                                0 2px 8px
                                rgba(
                                    0,
                                    0,
                                    0,
                                    0.08
                                );
                        }

                        .card-title {
                            color: #666;
                            font-size: 14px;
                            margin-bottom: 10px;
                        }

                        .card-value {
                            font-size: 25px;
                            font-weight: bold;
                        }

                        .quick {
                            margin-top: 25px;
                        }

                        .quick h3 {
                            margin-bottom: 15px;
                        }

                        .buttons {
                            display: grid;
                            grid-template-columns:
                                repeat(
                                    4,
                                    1fr
                                );
                            gap: 12px;
                        }

                        .button {
                            background: white;
                            border: none;
                            padding: 16px;
                            border-radius: 10px;
                            font-size: 15px;
                            cursor: pointer;
                            box-shadow:
                                0 2px 6px
                                rgba(
                                    0,
                                    0,
                                    0,
                                    0.08
                                );
                        }

                        .button:hover {
                            background: #eeeeee;
                        }

                        .refresh {
                            margin-top: 20px;
                            background: #2563eb;
                            color: white;
                            border: none;
                            padding: 13px 20px;
                            border-radius: 8px;
                            font-size: 15px;
                            cursor: pointer;
                        }

                        .footer {
                            text-align: center;
                            color: #777;
                            margin-top: 35px;
                            padding-bottom: 20px;
                        }

                        @media (
                            max-width: 800px
                        ) {

                            .cards {
                                grid-template-columns:
                                    repeat(
                                        2,
                                        1fr
                                    );
                            }

                            .buttons {
                                grid-template-columns:
                                    repeat(
                                        2,
                                        1fr
                                    );
                            }
                        }

                        @media (
                            max-width: 500px
                        ) {

                            .cards {
                                grid-template-columns:
                                    1fr;
                            }

                            .buttons {
                                grid-template-columns:
                                    1fr;
                            }

                            .container {
                                padding: 15px;
                            }
                        }

                    </style>

                </head>

                <body>


<div class="header">
    <h1>Retail POS</h1>
    <div class="user-info">
    <span>Username: %s</span>
    <span>Role: %s</span>
</div>
    <form action="/logout" method="post"
          style="display: inline;">
        <button type="submit"
                class="button">
            Logout
        </button>
    </form>
</div>


                    <div class="container">

                        <div class="title">
                            <h2>Dashboard</h2>
                            <p>
                                Live data from Retail POS database
                            </p>
                        </div>

                        <div class="cards">

                            <div class="card">

                                <div class="card-title">
                                    Products
                                </div>

                                <div class="card-value">
                                    %d
                                </div>

                            </div>

                            <div class="card">

                                <div class="card-title">
                                    Today's Sales
                                </div>

                                <div class="card-value">
                                    &#8377;%.2f
                                </div>

                            </div>

                            <div class="card">

                                <div class="card-title">
                                    Stock Value
                                </div>

                                <div class="card-value">
                                    &#8377;%.2f
                                </div>

                            </div>

                            <div class="card">

                                <div class="card-title">
                                    Low Stock
                                </div>

                                <div class="card-value">
                                    %d
                                </div>

                            </div>

                        </div>

                        <div class="quick">

                            <h3>
                                Quick Access
                            </h3>


                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            %s

                            </div>

                        </div>

                        <button
                            class="refresh"
                            onclick="location.reload()"
                        >
                            Refresh Dashboard
                        </button>

                        <div class="footer">
                           Retail POS &copy; 2026
                        </div>

                    </div>

                </body>
                </html>
         """.formatted(
        escapeHtml(username),
        escapeHtml(userRole),
        productCount,
        todaySales,
        stockValue,
        lowStockCount,
        menuPos,
        menuProducts,
        menuInventory,
        menuSales,
        menuPurchases,
        menuCustomers,
        menuSuppliers,
        menuReports,
        menuUsers,
        menuSettings,
        menuDevices
);
    }

    private String escapeHtml(
            String text
    ) {

        if (text == null) {
            return "Unknown error";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

private void handleCustomers(HttpExchange exchange)
        throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    try {
        List<Customer> customers =

                customerService.getAllCustomers();

        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1">
                <title>Retail POS - Customers</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 30px;
                        background: #f4f6f9;
                    }
                    h1 { color: #243447; }
                    a {
                        display: inline-block;
                        margin-bottom: 20px;
                        color: #1769aa;
                    }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        background: white;
                    }
                    th, td {
                        padding: 12px;
                        border: 1px solid #ddd;
                        text-align: left;
                    }
                    th {
                        background: #243447;
                        color: white;
                    }
                    tr:nth-child(even) {
                        background: #f2f2f2;
                    }
                </style>
            </head>
            <body>
                <h1>Customer Management</h1>
                <a href="/">Back to Dashboard</a>
            """);

        html.append("<p>Total Customers: ")
            .append(customers.size())
            .append("</p>");

        html.append("""
            <table>
                <tr>
                    <th>Name</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Address</th>
                    <th>Status</th>
                </tr>
            """);

        for (Customer customer : customers) {
            html.append("<tr>")
                .append("<td>")
                .append(escapeHtml(customer.getName()))
                .append("</td><td>")
                .append(escapeHtml(customer.getPhone()))
                .append("</td><td>")
                .append(escapeHtml(customer.getEmail()))
                .append("</td><td>")
                .append(escapeHtml(customer.getAddress()))
                .append("</td><td>")
                .append(escapeHtml(customer.getStatus()))
                .append("</td></tr>");
        }

        html.append("</table></body></html>");

        byte[] bytes = withSessionPolling(exchange, html.toString()).getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }

    } catch (Exception e) {
        String error = "Unable to load customers: "
                + escapeHtml(e.getMessage());

        byte[] bytes = error.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(500, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}

private Session requireAuth(HttpExchange exchange)
        throws IOException {

    String cookieHeader =
            exchange.getRequestHeaders()
                    .getFirst("Cookie");

    String sessionId = null;

    if (cookieHeader != null) {
        for (String cookie : cookieHeader.split(";")) {
            String[] parts =
                    cookie.trim().split("=", 2);

            if (parts.length == 2
                    && "RETAILPOS_SESSION".equals(parts[0])) {
                sessionId = parts[1];
                break;
            }
        }
    }

    if (sessionId == null || sessionId.isBlank()) {
        exchange.getResponseHeaders()
                .set("Location", "/login");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return null;
    }

    Session session = sessionService
            .findById(sessionId)
            .orElse(null);

    if (session == null
            || !"ACTIVE".equals(session.getStatus())) {

        exchange.getResponseHeaders().set(
                "Set-Cookie",
                "RETAILPOS_SESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"
        );

        exchange.getResponseHeaders()
                .set("Location", "/login");

        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return null;
    }

    Device authenticatedDevice = deviceRepository
            .findByUserIdAndDeviceId(
                    session.getUserId(),
                    session.getDeviceId()
            )
            .orElse(null);

    if (authenticatedDevice != null
            && "BLOCKED".equalsIgnoreCase(authenticatedDevice.getStatus())) {

        sessionService.forceLogout(session.getId());

        exchange.getResponseHeaders().set(
                "Set-Cookie",
                "RETAILPOS_SESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"
        );
        exchange.getResponseHeaders().set("Location", "/login");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return null;
    }

    sessionService.updateActivity(session);

    return session;
}
private User getCurrentUser(Session session) {
    if (session == null) {
        return null;
    }

    return userService
            .findByUsername(session.getUsername())
            .orElse(null);
}

private boolean hasAnyRole(User user, String... allowedRoles) {
    if (user == null || user.getRole() == null) {
        return false;
    }

    for (String role : allowedRoles) {
        if (role.equals(user.getRole())) {
            return true;
        }
    }

    return false;
}
private void handleProducts(
        HttpExchange exchange
) throws IOException {
        Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }
    List<Product> products =
            productService.getAllProducts();

    StringBuilder rows =
            new StringBuilder();

    for (Product product : products) {

        String stockClass =
                product.getStockQuantity()
                        <= product.getMinimumStock()
                ? "low-stock"
                : "";

        rows.append("""
            <tr>

                <td>%s</td>

                <td>%s</td>

                <td>%s</td>

                <td>&#8377;%.2f</td>

                <td>&#8377;%.2f</td>

                <td class="%s">
                    %d
                </td>

                <td>%d</td>

                <td>%s</td>

            </tr>
            """.formatted(
                escapeHtml(product.getName()),
                escapeHtml(product.getSku()),
                escapeHtml(product.getCategory()),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                stockClass,
                product.getStockQuantity(),
                product.getMinimumStock(),
                escapeHtml(product.getSupplier())
            ));
    }

    String html = """
        <!DOCTYPE html>

        <html>

        <head>

            <meta charset="UTF-8">

            <meta
                name="viewport"
                content="width=device-width, initial-scale=1.0"
            >

            <title>Retail POS - Products</title>

            <style>

                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    font-family: Arial, sans-serif;
                    background: #f3f4f6;
                }

                .header {
                    background: #111827;
                    color: white;
                    padding: 18px;
                    text-align: center;
                }

                .container {
                    max-width: 1200px;
                    margin: 25px auto;
                    padding: 20px;
                }

                .top {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }

                .button {
                    background: #2563eb;
                    color: white;
                    border: none;
                    padding: 11px 18px;
                    border-radius: 7px;
                    cursor: pointer;
                }

                .table-container {
                    background: white;
                    border-radius: 12px;
                    padding: 10px;
                    overflow-x: auto;
                    box-shadow:
                        0 2px 8px
                        rgba(0, 0, 0, 0.08);
                }

                table {
                    width: 100%%;
                    border-collapse: collapse;
                    min-width: 900px;
                }

                th,
                td {
                    padding: 13px;
                    text-align: left;
                    border-bottom:
                        1px solid #eee;
                }

                th {
                    background: #f8fafc;
                }

                .low-stock {
                    color: #dc2626;
                    font-weight: bold;
                }

                .back {
                    margin-top: 20px;
                    background: #374151;
                }

                @media (max-width: 600px) {

                    .container {
                        padding: 12px;
                    }

                    .top {
                        align-items: flex-start;
                        flex-direction: column;
                    }

                }

            </style>

        </head>

        <body>

            <div class="header">
                <h1>Retail POS</h1>
            </div>

            <div class="container">

                <div class="top">

                    <h2>Products</h2>

                    <button
                        class="button"
                        onclick="location.reload()"
                    >
                        Refresh
                    </button>

                </div>

                <div class="table-container">

                    <table>

                        <thead>

                            <tr>
                                <th>Name</th>
                                <th>SKU</th>
                                <th>Category</th>
                                <th>Purchase</th>
                                <th>Selling</th>
                                <th>Stock</th>
                                <th>Minimum</th>
                                <th>Supplier</th>
                            </tr>

                        </thead>

                        <tbody>

                            %s

                        </tbody>

                    </table>

                </div>

                <button
                    class="button back"
                    onclick="location.href='/'"
                >
                    Dashboard
                </button>

            </div>

        </body>

        </html>
        """.formatted(rows);

    byte[] response =
            withSessionPolling(exchange, html).getBytes(
                    java.nio.charset.StandardCharsets.UTF_8
            );

    exchange.getResponseHeaders().set(
            "Content-Type",
            "text/html; charset=UTF-8"
    );

    exchange.sendResponseHeaders(
            200,
            response.length
    );

    try (OutputStream os =
            exchange.getResponseBody()) {

        os.write(response);
    }
}

private void handlePos(
        HttpExchange exchange
) throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    try {

        List<Product> products =

                productService.getAllProducts();

        StringBuilder productOptions =
                new StringBuilder();

        for (Product product : products) {

            String productId =
                    product.getId() == null
                    ? ""
                    : String.valueOf(product.getId());

            String productName =
                    product.getName() == null
                    ? ""
                    : String.valueOf(product.getName());

            String price =
                    String.format(
                            "%.2f",
                            product.getSellingPrice()
                    );

            productOptions.append("""
                <option
                    value="%s"
                    data-price="%s"
                >
                    %s - ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹%s
                </option>
                """.formatted(
                    escapeHtml(productId),
                    price,
                    escapeHtml(productName),
                    price
                ));
        }

        String html = """
            <!DOCTYPE html>

            <html>

            <head>

                <meta charset="UTF-8">

                <meta
                    name="viewport"
                    content="width=device-width, initial-scale=1.0"
                >

                <title>Retail POS - POS</title>

                <style>

                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f3f4f6;
                    }

                    .header {
                        background: #111827;
                        color: white;
                        padding: 18px;
                        text-align: center;
                    }

                    .container {
                        max-width: 1100px;
                        margin: 25px auto;
                        padding: 20px;
                    }

                    .card {
                        background: white;
                        padding: 20px;
                        border-radius: 12px;
                        box-shadow:
                            0 2px 8px
                            rgba(0, 0, 0, 0.08);
                    }

                    h2 {
                        margin-top: 0;
                    }

                    label {
                        display: block;
                        margin-top: 15px;
                        margin-bottom: 6px;
                        font-weight: bold;
                    }

                    select,
                    input {
                        width: 100%%;
                        padding: 12px;
                        border: 1px solid #d1d5db;
                        border-radius: 7px;
                        font-size: 16px;
                    }

                    .row {
                        display: grid;
                        grid-template-columns:
                            2fr 1fr auto;
                        gap: 12px;
                        align-items: end;
                    }

                    .button {
                        padding: 12px 20px;
                        border: none;
                        border-radius: 7px;
                        background: #2563eb;
                        color: white;
                        cursor: pointer;
                        font-size: 16px;
                    }

                    .button:hover {
                        opacity: 0.9;
                    }

                    .add {
                        background: #16a34a;
                    }

                    .complete {
                        background: #16a34a;
                        width: 100%%;
                        margin-top: 18px;
                    }

                    .remove {
                        background: #dc2626;
                        padding: 7px 12px;
                    }

                    .back {
                        background: #374151;
                        margin-top: 15px;
                    }

                    .cart {
                        margin-top: 25px;
                        overflow-x: auto;
                    }

                    table {
                        width: 100%%;
                        border-collapse: collapse;
                    }

                    th,
                    td {
                        padding: 12px;
                        text-align: left;
                        border-bottom:
                            1px solid #e5e7eb;
                    }

                    th {
                        background: #f8fafc;
                    }

                    .summary {
                        margin-top: 20px;
                        padding: 18px;
                        background: #f8fafc;
                        border-radius: 8px;
                        text-align: right;
                    }

                    .total {
                        font-size: 24px;
                        font-weight: bold;
                    }

                    .payment {
                        margin-top: 20px;
                    }

                    .message {
                        margin-top: 15px;
                        padding: 12px;
                        border-radius: 7px;
                        display: none;
                    }

                    .success {
                        display: block;
                        background: #dcfce7;
                        color: #166534;
                    }

                    .error {
                        display: block;
                        background: #fee2e2;
                        color: #991b1b;
                    }

                    @media (max-width: 700px) {

                        .container {
                            padding: 12px;
                        }

                        .row {
                            grid-template-columns: 1fr;
                        }

                        .button {
                            width: 100%%;
                        }

                    }

                </style>

            </head>

            <body>

                <div class="header">

                    <h1>Retail POS</h1>

                </div>

                <div class="container">

                    <div class="card">

                        <h2>New Sale</h2>

                        <div class="row">

                            <div>
                                <label for="barcodeInput">Scan Barcode</label>
                                <input
                                    type="text"
                                    id="barcodeInput"
                                    placeholder="Scan barcode and press Enter"
                                    autocomplete="off"
                                    autofocus
                                >
                                <div id="barcodeMessage" role="status"></div>
                                <div style="margin-top:10px">
                                    <button type="button" class="button primary" id="startCameraButton" onclick="startBarcodeCamera()">Start Camera</button>
                                    <button type="button" class="button" id="stopCameraButton" onclick="stopBarcodeCamera()" disabled>Stop Camera</button>
                                </div>
                                <div id="cameraScannerPanel" style="display:none;margin-top:10px">
                                    <video id="barcodeCameraVideo" autoplay muted playsinline style="width:100%%;max-width:480px;border-radius:8px;background:#111"></video>
                                    <p id="cameraScannerStatus" role="status">Camera is stopped.</p>
                                </div>
                            </div>

                            <div>
                                <label>Product</label>

                                <select id="product">

                                    <option value="">
                                        Select Product
                                    </option>

                                    %s

                                </select>

                            </div>

                            <div>

                                <label>
                                    Quantity
                                </label>

                                <input
                                    type="number"
                                    id="quantity"
                                    value="1"
                                    min="1"
                                >

                            </div>

                            <button
                                class="button add"
                                onclick="addToCart()"
                            >
                                Add to Cart
                            </button>

                        </div>

                        <div class="cart">

                            <h3>Cart</h3>

                            <table>

                                <thead>

                                    <tr>
                                        <th>Product</th>
                                        <th>Price</th>
                                        <th>Qty</th>
                                        <th>Total</th>
                                        <th>Action</th>
                                    </tr>

                                </thead>

                                <tbody id="cartBody">

                                    <tr>

                                        <td
                                            colspan="5"
                                            style="text-align:center"
                                        >
                                            Cart is empty
                                        </td>

                                    </tr>

                                </tbody>

                            </table>

                        </div>

                        <div class="summary">

                            <div>
                                Subtotal:
                                ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹<span id="subtotal">
                                    0.00
                                </span>
                            </div>

                            <br>

                            <div>
                                Discount:
                                ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹<span id="discount">
                                    0.00
                                </span>
                            </div>

                            <br>

                            <div class="total">
                                Total:
                                ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹<span id="total">
                                    0.00
                                </span>
                            </div>

                        </div>

                        <div class="payment">

                            <label>
                                Payment Method
                            </label>

                            <select id="paymentMethod">

                                <option value="CASH">
                                    Cash
                                </option>

                                <option value="CARD">
                                    Card
                                </option>

                                <option value="UPI">
                                    UPI
                                </option>

                            </select>

                        </div>

                        <button
                            class="button complete"
                            onclick="completeSale()"
                        >
                            Complete Sale
                        </button>

                        <div
                            id="saleMessage"
                            class="message"
                        ></div>

                        <button
                            class="button back"
                            onclick="location.href='/'"
                        >
                            Dashboard
                        </button>

                    </div>

                </div>

                <script>

                    let cart = [];

                    async function scanBarcodeForWeb() {
    const input = document.getElementById("barcodeInput");
    const message = document.getElementById("barcodeMessage");
    const barcode = input.value.trim();

    if (!barcode || input.dataset.scanning === "true") return;

    input.dataset.scanning = "true";
    message.innerText = "Looking up barcode...";

    try {
        const response = await fetch("/barcode-lookup", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: new URLSearchParams({ barcode }).toString(),
            credentials: "same-origin",
            cache: "no-store"
        });

        const product = await response.json();

        if (response.status === 401 || response.status === 403 ||
            response.redirected) {
            window.location.replace("/login");
            return;
        }

        if (!response.ok) {
            message.innerText = product.error || "Barcode lookup failed.";
            input.select();
            return;
        }

        const productId = String(product.id);
        const existing = cart.find(item => item.id === productId);

        if (existing) {
            existing.quantity += 1;
        } else {
            cart.push({
                id: productId,
                name: product.name || "",
                price: Number(product.price) || 0,
                quantity: 1
            });
        }

        renderCart();
        message.innerText = "Added: " + (product.name || "Product");
        input.value = "";
    } catch (error) {
        console.error("Barcode scan failed:", error);
        message.innerText = "Unable to look up barcode. Check connection.";
    } finally {
        delete input.dataset.scanning;
        input.focus();
    }
}

document.getElementById("barcodeInput").addEventListener("keydown", event => {
    if (event.key === "Enter") {
        event.preventDefault();
        scanBarcodeForWeb();
    }
});

document.getElementById("barcodeInput").focus();
let barcodeCameraStream = null;
let barcodeCameraRunning = false;
let barcodeCameraFrame = null;
let barcodeCameraDetector = null;
let barcodeCameraBusy = false;
let barcodeCameraAcceptedBarcode = null;
let barcodeCameraAbsentSince = null;
let barcodeCameraControls = null;
let barcodeCameraReader = null;
let barcodeCameraScriptPromise = null;

async function startBarcodeCamera() {
    const status = document.getElementById("cameraScannerStatus");
    const panel = document.getElementById("cameraScannerPanel");
    const video = document.getElementById("barcodeCameraVideo");
    const startButton = document.getElementById("startCameraButton");
    const stopButton = document.getElementById("stopCameraButton");

    if (barcodeCameraRunning) return;

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        panel.style.display = "block";
        status.innerText = "Camera unavailable. Open this POS using localhost or HTTPS.";
        return;
    }

    panel.style.display = "block";
    status.innerText = "Loading barcode decoder...";
    startButton.disabled = true;

    try {
        if (!window.ZXingBrowser) {
            if (!barcodeCameraScriptPromise) {
                barcodeCameraScriptPromise = new Promise((resolve, reject) => {
                    const script = document.createElement("script");
                    script.src = "https://unpkg.com/@zxing/browser@0.1.5";
                    script.onload = () => {
                        if (window.ZXingBrowser) resolve();
                        else reject(new Error("ZXing loaded, but its browser API was not found."));
                    };
                    script.onerror = () => reject(new Error(
                        "Could not load ZXing. Check your internet connection."
                    ));
                    document.head.appendChild(script);
                });
            }
            await barcodeCameraScriptPromise;
        }

        barcodeCameraReader = new ZXingBrowser.BrowserMultiFormatReader();
        barcodeCameraAcceptedBarcode = null;
        barcodeCameraAbsentSince = null;
        barcodeCameraRunning = true;
        status.innerText = "Starting camera. Allow camera permission if asked.";

        await barcodeCameraReader.decodeFromVideoDevice(
            null,
            video,
            (result, error, controls) => {
                if (controls) {
                    barcodeCameraControls = controls;
                    stopButton.disabled = false;
                }

                if (!barcodeCameraRunning) return;

                if (error && error.name !== "NotFoundException") {
                    console.debug("ZXing scan callback:", error.name, error.message);
                }

                if (result) {
                    console.log("ZXing barcode result:", result.getText());
                    const barcode = result.getText().trim();
                    barcodeCameraAbsentSince = null;

                    if (barcode && barcode !== barcodeCameraAcceptedBarcode) {
                        barcodeCameraAcceptedBarcode = barcode;

                        const input = document.getElementById("barcodeInput");
                        input.value = barcode;
                        status.innerText = "Barcode detected: " + barcode;

                        scanBarcodeForWeb();
                    }
                } else if (error) {
                    if (error.name === "NotFoundException" ||
                        String(error).includes("NotFoundException")) {
                        if (barcodeCameraAcceptedBarcode !== null) {
                            if (barcodeCameraAbsentSince === null) {
                                barcodeCameraAbsentSince = Date.now();
                            } else if (Date.now() - barcodeCameraAbsentSince > 1200) {
                                barcodeCameraAcceptedBarcode = null;
                                barcodeCameraAbsentSince = null;
                            }
                        }
                    } else {
                        console.debug("Barcode decoder:", error);
                    }
                }
            }
        );

        if (barcodeCameraRunning) {
            status.innerText = "Camera active. Hold a product barcode clearly in view.";
        }
    } catch (error) {
        console.error("Camera start failed:", error);
        stopBarcodeCamera();
        panel.style.display = "block";
        status.innerText = "Could not start camera: " +
            (error.message || "Permission or device error.");
    } finally {
        startButton.disabled = false;
    }
}

async function detectBarcodeCameraFrame() {
    if (!barcodeCameraRunning) return;

    if (!barcodeCameraBusy &&
        barcodeCameraDetector &&
        document.getElementById("barcodeCameraVideo").readyState >= 2) {

        barcodeCameraBusy = true;

        try {
            const video = document.getElementById("barcodeCameraVideo");
            const results = await barcodeCameraDetector.detect(video);
            const barcode = results.length ? results[0].rawValue.trim() : "";

            if (barcode) {
                barcodeCameraAbsentSince = null;

                if (barcode !== barcodeCameraAcceptedBarcode) {
                    barcodeCameraAcceptedBarcode = barcode;

                    const input = document.getElementById("barcodeInput");
                    input.value = barcode;
                    document.getElementById("cameraScannerStatus").innerText =
                        "Barcode detected: " + barcode;

                    await scanBarcodeForWeb();
                }
            } else if (barcodeCameraAcceptedBarcode !== null) {
                if (barcodeCameraAbsentSince === null) {
                    barcodeCameraAbsentSince = Date.now();
                } else if (Date.now() - barcodeCameraAbsentSince > 1200) {
                    barcodeCameraAcceptedBarcode = null;
                    barcodeCameraAbsentSince = null;
                }
            }
        } catch (error) {
            console.error("Camera barcode detection failed:", error);
        } finally {
            barcodeCameraBusy = false;
        }
    }

    if (barcodeCameraRunning) {
        barcodeCameraFrame = requestAnimationFrame(detectBarcodeCameraFrame);
    }
}

function stopBarcodeCamera() {
    barcodeCameraRunning = false;

    if (barcodeCameraControls) {
        try {
            barcodeCameraControls.stop();
        } catch (error) {
            console.debug("Camera controls already stopped:", error);
        }
        barcodeCameraControls = null;
    }

    if (barcodeCameraReader) {
        try {
            barcodeCameraReader.reset();
        } catch (error) {
            console.debug("Decoder reset:", error);
        }
        barcodeCameraReader = null;
    }

    if (barcodeCameraFrame !== null) {
        cancelAnimationFrame(barcodeCameraFrame);
        barcodeCameraFrame = null;
    }

    if (barcodeCameraStream) {
        barcodeCameraStream.getTracks().forEach(track => track.stop());
        barcodeCameraStream = null;
    }

    const video = document.getElementById("barcodeCameraVideo");
    const panel = document.getElementById("cameraScannerPanel");
    const status = document.getElementById("cameraScannerStatus");
    const stopButton = document.getElementById("stopCameraButton");

    if (video) video.srcObject = null;
    if (panel) panel.style.display = "none";
    if (status) status.innerText = "Camera is stopped.";
    if (stopButton) stopButton.disabled = true;

    barcodeCameraAcceptedBarcode = null;
    barcodeCameraAbsentSince = null;
    barcodeCameraBusy = false;
}

function addToCart() {
                        const barcodeInput =
                            document.getElementById("barcodeInput");

                        if (barcodeInput && barcodeInput.value.trim()) {
                            scanBarcodeForWeb();
                            return;
                        }

                        const select =
                            document.getElementById(
                                "product"
                            );

                        const quantity =
                            parseInt(
                                document.getElementById(
                                    "quantity"
                                ).value
                            ) || 0;

                        if (!select.value) {

                            alert(
                                "Please select a product."
                            );

                            return;
                        }

                        if (quantity <= 0) {

                            alert(
                                "Quantity must be at least 1."
                            );

                            return;
                        }

                        const option =
                            select.options[
                                select.selectedIndex
                            ];

                        const productId =
                            select.value;

                        const productName =
                            option.text
                                .split(" - ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹")[0]
                                .trim();

                        const price =
                            parseFloat(
                                option.dataset.price
                            ) || 0;

                        const existing =
                            cart.find(
                                item =>
                                    item.id === productId
                            );

                        if (existing) {

                            existing.quantity +=
                                quantity;

                        } else {

                            cart.push({
                                id: productId,
                                name: productName,
                                price: price,
                                quantity: quantity
                            });
                        }

                        renderCart();

                        document.getElementById(
                            "quantity"
                        ).value = 1;
                    }

                    function removeFromCart(index) {

                        cart.splice(index, 1);

                        renderCart();
                    }

                    function renderCart() {

                        const body =
                            document.getElementById(
                                "cartBody"
                            );

                        body.innerHTML = "";

                        if (cart.length === 0) {

                            body.innerHTML = `
                                <tr>
                                    <td
                                        colspan="5"
                                        style="text-align:center"
                                    >
                                        Cart is empty
                                    </td>
                                </tr>
                            `;

                        } else {

                            cart.forEach(
                                (item, index) => {

                                    const itemTotal =
                                        item.price *
                                        item.quantity;

                                    body.innerHTML += `
                                        <tr>

                                            <td>
                                                ${item.name}
                                            </td>

                                            <td>
                                                ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹${item.price.toFixed(2)}
                                            </td>

                                            <td>
                                                ${item.quantity}
                                            </td>

                                            <td>
                                                ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹${itemTotal.toFixed(2)}
                                            </td>

                                            <td>
                                                <button
                                                    class="button remove"
                                                    onclick="removeFromCart(${index})"
                                                >
                                                    Remove
                                                </button>
                                            </td>

                                        </tr>
                                    `;
                                }
                            );
                        }

                        calculateSummary();
                    }

                    function calculateSummary() {

                        let subtotal = 0;

                        cart.forEach(item => {

                            subtotal +=
                                item.price *
                                item.quantity;
                        });

                        const discount = 0;

                        const total =
                            subtotal - discount;

                        document.getElementById(
                            "subtotal"
                        ).innerText =
                            subtotal.toFixed(2);

                        document.getElementById(
                            "discount"
                        ).innerText =
                            discount.toFixed(2);

                        document.getElementById(
                            "total"
                        ).innerText =
                            total.toFixed(2);
                    }

                    async function completeSale() {

                        if (cart.length === 0) {

                            alert(
                                "Cart is empty."
                            );

                            return;
                        }

                        const paymentMethod =
                            document.getElementById(
                                "paymentMethod"
                            ).value;

                        const items =
                            cart
                                .map(
                                    item =>
                                        item.id +
                                        ":" +
                                        item.quantity
                                )
                                .join(",");

                        const formData =
                            new URLSearchParams();

                        formData.append(
                            "items",
                            items
                        );

                        formData.append(
                            "paymentMethod",
                            paymentMethod
                        );

                        const message =
                            document.getElementById(
                                "saleMessage"
                            );

                        try {

                            const response =
                                await fetch(
                                    "/complete-sale",
                                    {
                                        method: "POST",

                                        headers: {
                                            "Content-Type":
                                                "application/x-www-form-urlencoded"
                                        },

                                        body:
                                            formData.toString()
                                    }
                                );

                            const result =
                                await response.json();

                            if (!result.success) {

                                message.className =
                                    "message error";

                                message.innerText =
                                    result.message ||
                                    "Sale failed.";

                                return;
                            }

                            message.className =
                                "message success";

                            message.innerHTML =
                                "Sale completed successfully! " +
                                "Invoice: <strong>" +
                                result.invoice +
                                "</strong> | " +
                                "Total: ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¹" +
                                Number(
                                    result.total
                                ).toFixed(2);

                            cart = [];

                            renderCart();

                            calculateSummary();

                        } catch (error) {

                            console.error(error);

                            message.className =
                                "message error";

                            message.innerText =
                                "Unable to complete sale.";
                        }
                    }

                </script>

            </body>

            </html>
            """.formatted(productOptions);

        byte[] response =
                withSessionPolling(exchange, html).getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                200,
                response.length
        );

        try (OutputStream os =
                exchange.getResponseBody()) {

            os.write(response);
        }

    } catch (Exception e) {

        e.printStackTrace();

        String error =
                "POS ERROR: "
                + e.getClass().getName()
                + "\n"
                + e.getMessage();

        byte[] response =
                error.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                500,
                response.length
        );

        try (OutputStream os =
                exchange.getResponseBody()) {

            os.write(response);
        }
    }
}

private void handleBarcodeLookup(HttpExchange exchange) throws IOException {
    Session session = requireAuth(exchange);
    if (session == null) {
        return;
    }

    exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8");
    exchange.getResponseHeaders().set("Cache-Control", "no-store");

    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set("Allow", "POST");
        sendResponse(exchange, 405, "{\"error\":\"Method not allowed.\"}");
        return;
    }

    try {
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);

        java.util.Map<String, String> formData = parseFormData(body);
        String barcode = formData.get("barcode");

        if (barcode == null || barcode.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Enter a barcode.\"}");
            return;
        }

        java.util.Optional<Product> result =
                productService.findByBarcode(barcode.trim());

        if (result.isEmpty()) {
            sendResponse(exchange, 404,
                    "{\"error\":\"No product found for this barcode.\"}");
            return;
        }

        Product product = result.get();

        String response = String.format(
                java.util.Locale.ROOT,
                "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%.2f}",
                escapeJson(product.getId()),
                escapeJson(product.getName()),
                product.getSellingPrice());

        sendResponse(exchange, 200, response);
    } catch (Exception e) {
        e.printStackTrace();
        sendResponse(exchange, 500,
                "{\"error\":\"Barcode lookup failed.\"}");
    }
}
private void handleCompleteSale(
        HttpExchange exchange
) throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    if (!exchange.getRequestMethod()
            .equalsIgnoreCase("POST")) {

        sendResponse(
                exchange,
                405,
                "Method Not Allowed"
        );

        return;
    }

    try {

        String body =
                new String(
                        exchange.getRequestBody()
                                .readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8
                );

        java.util.Map<String, String> formData =
                parseFormData(body);

        String itemsData =
                formData.get("items");

        String paymentMethod =
                formData.get("paymentMethod");

        if (itemsData == null ||
                itemsData.isBlank()) {

            throw new IllegalArgumentException(
                    "Cart is empty."
            );
        }

        if (paymentMethod == null ||
                paymentMethod.isBlank()) {

            paymentMethod = "CASH";
        }

        List<Product> products =
                new ArrayList<>();

        List<Integer> quantities =
                new ArrayList<>();

        double subtotal = 0.0;

        String[] cartItems =
                itemsData.split(",");

        for (String cartItem : cartItems) {

            String[] parts =
                    cartItem.split(":");

            if (parts.length != 2) {

                throw new IllegalArgumentException(
                        "Invalid cart item."
                );
            }

            String productId =
                    parts[0];

            int quantity =
                    Integer.parseInt(parts[1]);

            Product product =
                    productService
                            .findById(productId)
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Product not found."
                                    )
                            );

            if (quantity <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero."
                );
            }

            if (quantity >
                    product.getStockQuantity()) {

                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + product.getName()
                );
            }

            products.add(product);
            quantities.add(quantity);

            subtotal +=
                    product.getSellingPrice()
                            * quantity;
        }

        double discount = 0.0;
        double tax = 0.0;

        double total =
                subtotal - discount + tax;

        String invoiceNumber =
                "SALE-"
                + System.currentTimeMillis();

        Sale sale =
                new Sale(
                        invoiceNumber,
                        null,
                        "Walk-in Customer",
                        subtotal,
                        discount,
                        tax,
                        total,
                        paymentMethod,
                        "COMPLETED",
                        session.getUsername()
                );

        List<SaleItem> saleItems =
                new ArrayList<>();

        for (int i = 0;
             i < products.size();
             i++) {

            Product product =
                    products.get(i);

            int quantity =
                    quantities.get(i);

            double itemTotal =
                    product.getSellingPrice()
                            * quantity;

            SaleItem item =
                    new SaleItem(
                            null,
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            quantity,
                            product.getSellingPrice(),
                            0.0,
                            itemTotal
                    );

            saleItems.add(item);
        }

        saleService.createSale(
                sale,
                saleItems
        );

        for (int i = 0;
             i < products.size();
             i++) {

            productService.reduceStock(
                    products.get(i).getId(),
                    quantities.get(i)
            );
        }

        String response =
                """
                {
                    "success": true,
                    "invoice": "%s",
                    "subtotal": %.2f,
                    "total": %.2f
                }
                """.formatted(
                        escapeJson(invoiceNumber),
                        subtotal,
                        total
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        sendResponse(
                exchange,
                200,
                response
        );

    } catch (Exception e) {

        e.printStackTrace();

        String message =
                e.getMessage() == null
                        ? "Sale failed."
                        : e.getMessage();

        String response =
                """
                {
                    "success": false,
                    "message": "%s"
                }
                """.formatted(
                        escapeJson(message)
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        sendResponse(
                exchange,
                400,
                response
        );
    }
}
private java.util.Map<String, String> parseFormData(
        String body
) {

    java.util.Map<String, String> data =
            new java.util.HashMap<>();

    if (body == null || body.isBlank()) {
        return data;
    }

    String[] pairs =
            body.split("&");

    for (String pair : pairs) {

        String[] parts =
                pair.split("=", 2);

        if (parts.length == 2) {

            String key =
                    java.net.URLDecoder.decode(
                            parts[0],
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            String value =
                    java.net.URLDecoder.decode(
                            parts[1],
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            data.put(key, value);
        }
    }

    return data;
}
private void handleSessionHeartbeat(HttpExchange exchange) throws IOException {
    if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set("Allow", "GET");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        sendResponse(exchange, 405, "Method not allowed.");
        return;
    }

    Session session = requireAuth(exchange);
    if (session == null) {
        return;
    }

    Device currentDevice = deviceRepository
            .findByUserIdAndDeviceId(
                    session.getUserId(),
                    session.getDeviceId()
            )
            .orElse(null);

    if (currentDevice != null
            && "BLOCKED".equalsIgnoreCase(currentDevice.getStatus())) {
        sessionService.forceLogout(session.getId());
        exchange.getResponseHeaders().set(
                "Set-Cookie",
                "RETAILPOS_SESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"
        );
        exchange.getResponseHeaders().set("Location", "/login");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return;
    }

    List<String> states = new ArrayList<>();
    for (Device device : deviceRepository.findAll()) {
        states.add(
                String.valueOf(device.getId()) + "|"
                + String.valueOf(device.getUserId()) + "|"
                + String.valueOf(device.getDeviceId()) + "|"
                + String.valueOf(device.getStatus())
        );
    }
    states.sort(String::compareTo);

    String fingerprint = Integer.toHexString(states.toString().hashCode());
    exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");
    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
    sendResponse(exchange, 200, fingerprint);
}
private String withSessionPolling(HttpExchange exchange, String html) {
    if (html == null) {
        return html;
    }

    String lowerHtml = html.toLowerCase(java.util.Locale.ROOT);
    int bodyIndex = lowerHtml.lastIndexOf("</body>");
    if (bodyIndex < 0) {
        return html;
    }

    String path = exchange.getRequestURI().getPath()
            .toLowerCase(java.util.Locale.ROOT);

    if (path.equals("/login") || path.contains("otp")) {
        return html;
    }

    if (html.contains("retailPosSessionPolling")) {
        return html;
    }

    String script = """
<script>
(function retailPosSessionPolling() {
    const pagePath = window.location.pathname.toLowerCase();
    let previousFingerprint = null;
    let checking = false;

    async function pollSessionAndDevices() {
        if (checking) return;
        checking = true;

        try {
            const response = await fetch('/session-heartbeat', {
                method: 'GET',
                cache: 'no-store',
                credentials: 'same-origin'
            });

            if (response.redirected ||
                response.status === 401 ||
                response.status === 403) {
                window.location.replace('/login');
                return;
            }

            if (!response.ok) return;

            const fingerprint = (await response.text()).trim();

            if (previousFingerprint === null) {
                previousFingerprint = fingerprint;
            } else if (previousFingerprint !== fingerprint) {
                previousFingerprint = fingerprint;

                if (pagePath !== '/devices' &&
                    !pagePath.startsWith('/devices/')) {
                    window.location.reload();
                }
            }
        } catch (error) {
            // Keep the page open during temporary network failures.
        } finally {
            checking = false;
        }
    }

    pollSessionAndDevices();
    window.setInterval(pollSessionAndDevices, 4000);
})();
</script>
""";

    return html.substring(0, bodyIndex)
            + script
            + html.substring(bodyIndex);
}
private void sendResponse(
        HttpExchange exchange,
        int statusCode,
        String response
) throws IOException {

    if (response != null
            && response.toLowerCase(java.util.Locale.ROOT).contains("</body>")) {
        response = withSessionPolling(exchange, response);
    }
    byte[] bytes =
            response.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8
            );

    exchange.sendResponseHeaders(
            statusCode,
            bytes.length
    );

    try (OutputStream os =
            exchange.getResponseBody()) {

        os.write(bytes);
    }
}

private String escapeJson(
        String value
) {

    if (value == null) {
        return "";
    }

    return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
}

private void handleInventory(
        HttpExchange exchange
) throws IOException {
    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }
    try {

        List<Product> products =
                productService.getAllProducts();

        List<StockTransaction> transactions =
                inventoryService.getAllTransactions();

        StringBuilder productRows =
                new StringBuilder();

        for (Product product : products) {

            boolean lowStock =
                    product.getStockQuantity()
                    <= product.getMinimumStock();

            productRows.append("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td class="%s">%s</td>
                </tr>
                """.formatted(
                    escapeHtml(product.getName()),
                    escapeHtml(product.getSku()),
                    product.getStockQuantity(),
                    product.getMinimumStock(),
                    lowStock ? "low-stock" : "normal-stock",
                    lowStock ? "Low Stock" : "In Stock"
            ));
        }

        if (products.isEmpty()) {
            productRows.append(
                "<tr><td colspan=\"5\">No products found.</td></tr>"
            );
        }

        StringBuilder transactionRows =
                new StringBuilder();

        for (StockTransaction transaction : transactions) {

            String createdAt =
                    transaction.getCreatedAt() == null
                    ? ""
                    : transaction.getCreatedAt()
                            .toString()
                            .replace("T", " ");

            transactionRows.append("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                    escapeHtml(transaction.getProductName()),
                    escapeHtml(transaction.getType()),
                    escapeHtml(transaction.getProductId()),
                    transaction.getQuantity(),
                    transaction.getPreviousStock(),
                    transaction.getNewStock(),
                    escapeHtml(transaction.getReason()),
                    escapeHtml(createdAt)
            ));
        }

        if (transactions.isEmpty()) {
            transactionRows.append(
                "<tr><td colspan=\"8\">No stock transactions found.</td></tr>"
            );
        }

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">
                <title>Retail POS - Inventory</title>

                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f3f4f6;
                        color: #111827;
                    }

                    .header {
                        background: #111827;
                        color: white;
                        padding: 18px;
                        text-align: center;
                    }

                    .container {
                        max-width: 1250px;
                        margin: 25px auto;
                        padding: 20px;
                    }

                    .panel {
                        background: white;
                        padding: 18px;
                        margin-bottom: 24px;
                        border-radius: 12px;
                        box-shadow: 0 2px 8px rgba(0,0,0,.08);
                        overflow-x: auto;
                    }

                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        min-width: 650px;
                    }

                    th, td {
                        padding: 12px;
                        text-align: left;
                        border-bottom: 1px solid #e5e7eb;
                    }

                    th {
                        background: #f8fafc;
                    }

                    .low-stock {
                        color: #dc2626;
                        font-weight: bold;
                    }

                    .normal-stock {
                        color: #15803d;
                        font-weight: bold;
                    }

                    .button {
                        display: inline-block;
                        background: #2563eb;
                        color: white;
                        padding: 11px 18px;
                        border-radius: 7px;
                        text-decoration: none;
                    }

                    @media (max-width: 600px) {
                        .container {
                            padding: 12px;
                        }
                    }
                </style>
            </head>

            <body>
                <div class="header">
                    <h1>Retail POS</h1>
                    <p>Inventory Management</p>
                </div>

                <div class="container">
                    <p>
                        <a class="button" href="/">Back to Dashboard</a>
                        <a class="button" href="/inventory">Refresh</a>
                    </p>

                    <div class="panel">
                        <h2>Current Stock</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>SKU</th>
                                    <th>Current Stock</th>
                                    <th>Minimum Stock</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>

                    <div class="panel">
                        <h2>Stock Transaction History</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Type</th>
                                    <th>Product ID</th>
                                    <th>Quantity Changed</th>
                                    <th>Previous Stock</th>
                                    <th>New Stock</th>
                                    <th>Reason</th>
                                    <th>Date &amp; Time</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                productRows.toString(),
                transactionRows.toString()
        );

        byte[] bytes =
                withSessionPolling(exchange, html).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }

    } catch (Exception e) {

        String message =
                "Inventory error: "
                + escapeHtml(
                        e.getMessage() == null
                        ? "Unknown error"
                        : e.getMessage()
                );

        byte[] bytes =
                message.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(500, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}

private void handleSales(
        HttpExchange exchange
) throws IOException {
           Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }
    try {


User currentUser = getCurrentUser(session);

if (currentUser == null) {
    sendResponse(
            exchange,
            403,
            "Forbidden: User not found."
    );
    return;
}

List<Sale> sales =
        saleService.getAllSales();

if ("CASHIER".equals(currentUser.getRole())) {
    sales = sales.stream()
            .filter(sale ->
                    session.getUsername()
                            .equals(sale.getSoldBy()))
            .toList();
} else if (!hasAnyRole(
        currentUser, "ADMIN", "MANAGER")) {
    sendResponse(
            exchange,
            403,
            "Forbidden: You do not have permission to view sales."
    );
    return;
}


        StringBuilder rows =
                new StringBuilder();

        if (sales.isEmpty()) {

            rows.append("""
                    <tr>
                        <td colspan="9"
                            style="text-align:center;">
                            No sales found.
                        </td>
                    </tr>
                    """);

        } else {

            for (Sale sale : sales) {

                String createdAt =
                        sale.getCreatedAt() == null
                                ? ""
                                : sale.getCreatedAt()
                                        .toString()
                                        .replace("T", " ");

                rows.append("""
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>&#8377;%.2f</td>
                            <td>&#8377;%.2f</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>
                                <a class="button"
                                   href="/sale-details?id=%s">
                                    View Details
                                </a>
                            </td>
                        </tr>
                        """.formatted(
                                escapeHtml(
                                        sale.getInvoiceNumber()
                                ),
                                escapeHtml(
                                        sale.getCustomerName()
                                ),
                                escapeHtml(
                                        sale.getPaymentMethod()
                                ),
                                sale.getSubtotal(),
                                sale.getTotal(),
                                escapeHtml(
                                        sale.getStatus()
                                ),
                                escapeHtml(
                                        sale.getSoldBy()
                                ),
                                escapeHtml(
                                        createdAt
                                ),
                                escapeHtml(
                                        sale.getId()
                                )
                        ));
            }
        }

        String html =
                """
                <!DOCTYPE html>
                <html>

                <head>

                    <meta charset="UTF-8">

                    <title>Retail POS - Sales</title>

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            background: #f4f6f8;
                        }

                        .header {
                            background: #1f2937;
                            color: white;
                            padding: 20px;
                        }

                        .container {
                            padding: 25px;
                        }

                        .card {
                            background: white;
                            padding: 20px;
                            border-radius: 10px;
                            box-shadow:
                                0 2px 8px
                                rgba(0,0,0,0.08);
                            overflow-x: auto;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                        }

                        th,
                        td {
                            padding: 12px;
                            border-bottom: 1px solid #ddd;
                            text-align: left;
                            white-space: nowrap;
                        }

                        th {
                            background: #f1f5f9;
                        }

                        .button {
                            display: inline-block;
                            padding: 8px 12px;
                            margin-right: 5px;
                            background: #2563eb;
                            color: white;
                            text-decoration: none;
                            border-radius: 6px;
                        }

                        .button:hover {
                            background: #1d4ed8;
                        }

                    </style>

                </head>

                <body>

                    <div class="header">

                        <h1>Retail POS</h1>
                        <h2>Sales</h2>

                    </div>

                    <div class="container">

                        <a class="button"
                           href="/">
                            Dashboard
                        </a>

                        <a class="button"
                           href="/pos">
                            New Sale
                        </a>

                        <div class="card">

                            <h2>Sales History</h2>

                            <table>

                                <thead>

                                    <tr>
                                        <th>Invoice</th>
                                        <th>Customer</th>
                                        <th>Payment</th>
                                        <th>Subtotal</th>
                                        <th>Total</th>
                                        <th>Status</th>
                                        <th>Sold By</th>
                                        <th>Date</th>
                                        <th>Action</th>
                                    </tr>

                                </thead>

                                <tbody>

                                    %s

                                </tbody>

                            </table>

                        </div>

                    </div>

                </body>

                </html>
                """.formatted(rows);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        sendResponse(
                exchange,
                200,
                html
        );

    } catch (Exception e) {

        e.printStackTrace();

        sendResponse(
                exchange,
                500,
                "Failed to load sales."
        );
    }
}

private void handleSaleDetails(
        HttpExchange exchange
) throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    try {


        String query =
                exchange.getRequestURI()
                        .getQuery();

        if (query == null ||
                !query.startsWith("id=")) {

            sendResponse(
                    exchange,
                    400,
                    "Sale ID is required."
            );

            return;
        }

        String saleId =
                java.net.URLDecoder.decode(
                        query.substring(3),
                        java.nio.charset.StandardCharsets.UTF_8
                );

        Sale sale =
                saleService
                        .findById(saleId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sale not found."
                                )
                        );

        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            sendResponse(
                    exchange,
                    403,
                    "Forbidden: User not found."
            );
            return;
        }

        if ("CASHIER".equals(currentUser.getRole())
                && !session.getUsername()
                        .equals(sale.getSoldBy())) {

            sendResponse(
                    exchange,
                    403,
                    "Forbidden: You cannot view another cashier's sale."
            );
            return;
        }

        if (!hasAnyRole(
                currentUser, "ADMIN", "MANAGER", "CASHIER")) {
            sendResponse(
                    exchange,
                    403,
                    "Forbidden: You do not have permission to view sale details."
            );
            return;
        }


        List<SaleItem> items =
                saleService.getSaleItems(saleId);

        StringBuilder rows =
                new StringBuilder();

        for (SaleItem item : items) {

            rows.append("""
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%d</td>
                        <td>&#8377;%.2f</td>
                        <td>&#8377;%.2f</td>
                    </tr>
                    """.formatted(
                            escapeHtml(
                                    item.getProductName()
                            ),
                            escapeHtml(
                                    item.getSku()
                            ),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotal()
                    ));
        }

        String createdAt =
                sale.getCreatedAt() == null
                        ? ""
                        : sale.getCreatedAt()
                                .toString()
                                .replace("T", " ");

        String html =
                """
                <!DOCTYPE html>
                <html>

                <head>

                    <meta charset="UTF-8">

                    <title>Sale Details</title>

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            background: #f4f6f8;
                        }

                        .header {
                            background: #1f2937;
                            color: white;
                            padding: 20px;
                        }

                        .container {
                            padding: 25px;
                        }

                        .card {
                            background: white;
                            padding: 20px;
                            margin-bottom: 20px;
                            border-radius: 10px;
                            box-shadow:
                                0 2px 8px
                                rgba(0,0,0,0.08);
                            overflow-x: auto;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                        }

                        th,
                        td {
                            padding: 12px;
                            border-bottom: 1px solid #ddd;
                            text-align: left;
                        }

                        th {
                            background: #f1f5f9;
                        }

                        .button {
                            display: inline-block;
                            padding: 10px 16px;
                            background: #2563eb;
                            color: white;
                            text-decoration: none;
                            border-radius: 6px;
                        }

                        .total {
                            font-size: 22px;
                            font-weight: bold;
                        }

                    </style>

                </head>

                <body>

                    <div class="header">

                        <h1>Retail POS</h1>
                        <h2>Sale Details</h2>

                    </div>

                    <div class="container">

                        <a class="button"
                           href="/sales">
                            ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â Back to Sales
                        </a>

                        <div class="card">

                            <h2>Invoice: %s</h2>

                            <p>
                                <strong>Customer:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Payment:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Status:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Sold By:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Date:</strong>
                                %s
                            </p>

                        </div>

                        <div class="card">

                            <h2>Items</h2>

                            <table>

                                <thead>

                                    <tr>
                                        <th>Product</th>
                                        <th>SKU</th>
                                        <th>Quantity</th>
                                        <th>Unit Price</th>
                                        <th>Total</th>
                                    </tr>

                                </thead>

                                <tbody>

                                    %s

                                </tbody>

                            </table>

                        </div>

                        <div class="card">

                            <p>
                                Subtotal:
                                &#8377;%.2f
                            </p>

                            <p>
                                Discount:
                                &#8377;%.2f
                            </p>

                            <p>
                                Tax:
                                &#8377;%.2f
                            </p>

                            <p class="total">
                                Total:
                                &#8377;%.2f
                            </p>

                        </div>

                    </div>

                </body>

                </html>
                """.formatted(
                        escapeHtml(
                                sale.getInvoiceNumber()
                        ),
                        escapeHtml(
                                sale.getCustomerName()
                        ),
                        escapeHtml(
                                sale.getPaymentMethod()
                        ),
                        escapeHtml(
                                sale.getStatus()
                        ),
                        escapeHtml(
                                sale.getSoldBy()
                        ),
                        escapeHtml(
                                createdAt
                        ),
                        rows,
                        sale.getSubtotal(),
                        sale.getDiscount(),
                        sale.getTax(),
                        sale.getTotal()
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        sendResponse(
                exchange,
                200,
                html
        );

    } catch (Exception e) {

        e.printStackTrace();

        sendResponse(
                exchange,
                500,
                "Failed to load sale details: "
                        + escapeHtml(
                                e.getMessage()
                        )
        );
    }
}

private void handlePurchases(HttpExchange exchange)
        throws IOException {

        Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    User currentUser = getCurrentUser(session);

    if (!hasAnyRole(currentUser, "ADMIN", "MANAGER")) {
        sendResponse(
                exchange,
                403,
                "Forbidden: You do not have permission to view purchases."
        );
        return;
    }

    try {
        List<Purchase> purchases =

                purchaseService.getAllPurchases();

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <title>Purchases - Retail POS</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 25px;
                            background: #f4f6f9;
                        }
                        .container {
                            background: white;
                            padding: 20px;
                            border-radius: 8px;
                            overflow-x: auto;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 20px;
                        }
                        th, td {
                            padding: 12px;
                            border: 1px solid #ddd;
                            text-align: left;
                        }
                        th {
                            background: #263b5e;
                            color: white;
                        }
                        .button {
                            display: inline-block;
                            padding: 10px 16px;
                            background: #263b5e;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                        }
                    </style>
                </head>
                <body>
                <div class="container">
                <h1>Purchase Management</h1>
                <a class="button" href="/">Dashboard</a>
                <h3>Purchase Records</h3>
                <table>
                <tr>
                    <th>Invoice</th>
                    <th>Supplier</th>
                    <th>Subtotal</th>
                    <th>Discount</th>
                    <th>Tax</th>
                    <th>Total</th>
                    <th>Payment</th>
                    <th>Status</th>
                    <th>Date</th>
                </tr>
                """);

        for (Purchase p : purchases) {
            html.append("<tr>");

            html.append("<td>")
                    .append(escapeHtml(
                            String.valueOf(p.getInvoiceNumber())))
                    .append("</td>");

            html.append("<td>")
                    .append(escapeHtml(
                            String.valueOf(p.getSupplierName())))
                    .append("</td>");

            html.append("<td>")
                    .append(String.format("%.2f", p.getSubtotal()))
                    .append("</td>");

            html.append("<td>")
                    .append(String.format("%.2f", p.getDiscount()))
                    .append("</td>");

            html.append("<td>")
                    .append(String.format("%.2f", p.getTax()))
                    .append("</td>");

            html.append("<td>")
                    .append(String.format("%.2f", p.getTotal()))
                    .append("</td>");

            html.append("<td>")
                    .append(escapeHtml(
                            String.valueOf(p.getPaymentMethod())))
                    .append("</td>");

            html.append("<td>")
                    .append(escapeHtml(
                            String.valueOf(p.getStatus())))
                    .append("</td>");

            html.append("<td>")
                    .append(escapeHtml(
                            String.valueOf(p.getCreatedAt())))
                    .append("</td>");

            html.append("</tr>");
        }

        if (purchases.isEmpty()) {
            html.append("""
                    <tr>
                        <td colspan="9">No purchase records found.</td>
                    </tr>
                    """);
        }

        html.append("""
                </table>
                </div>
                </body>
                </html>
                """);

        sendResponse(exchange, 200, html.toString());

    } catch (Exception e) {
        e.printStackTrace();
        sendResponse(
                exchange,
                500,
                "Failed to load purchases: "
                        + escapeHtml(String.valueOf(e.getMessage()))
        );
    }
}

private void handleSettings(HttpExchange exchange)
        throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    User currentUser = userService
            .findByUsername(session.getUsername())
            .orElse(null);

    if (currentUser == null
            || !"ADMIN".equals(currentUser.getRole())) {
        sendResponse(
                exchange,
                403,
                "Forbidden: Admin access required."
        );
        return;
    }

    if ("POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> formData = parseFormData(body);

        String storeName = formData.getOrDefault(
                "storeName", "").trim();
        String address = formData.getOrDefault(
                "storeAddress", "").trim();
        String phone = formData.getOrDefault(
                "phone", "").trim();
        String email = formData.getOrDefault(
                "email", "").trim();
        String currency = formData.getOrDefault(
                "currency", "").trim();
        String taxNumber = formData.getOrDefault(
                "taxNumber", "").trim();
        String invoicePrefix = formData.getOrDefault(
                "invoicePrefix", "").trim();
        boolean showTax = "true".equals(
                formData.get("showTaxOnInvoice"));

        try {
            settingsService.saveSettings(
                    new Settings(
                            storeName,
                            address,
                            phone,
                            email,
                            currency,
                            taxNumber,
                            invoicePrefix,
                            showTax
                    )
            );

            exchange.getResponseHeaders().set(
                    "Location", "/settings?saved=1");
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
            return;

        } catch (IllegalArgumentException e) {
            sendResponse(
                    exchange,
                    400,
                    escapeHtml(e.getMessage())
            );
            return;
        } catch (Exception e) {
            sendResponse(
                    exchange,
                    500,
                    "Unable to save settings. Check application logs."
            );
            return;
        }
    }

    if (!"GET".equalsIgnoreCase(
            exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set(
                "Allow", "GET, POST");
        sendResponse(exchange, 405, "Method Not Allowed");
        return;
    }

    Settings settings = settingsService.getSettings();

    if (settings == null) {
        settings = new Settings(
                "", "", "", "", "INR", "",
                "INV-", true
        );
    }

    StringBuilder html = new StringBuilder("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1">
                <title>Retail POS - Settings</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 760px;
                        margin: 30px auto;
                        padding: 20px;
                        background: #f4f6f9;
                    }
                    form {
                        background: white;
                        padding: 24px;
                        border-radius: 10px;
                    }
                    label {
                        display: block;
                        margin-top: 14px;
                        font-weight: bold;
                    }
                    input, select {
                        box-sizing: border-box;
                        width: 100%;
                        padding: 10px;
                        margin-top: 5px;
                    }
                    input[type=checkbox] {
                        width: auto;
                    }
                    button {
                        margin-top: 20px;
                        padding: 11px 20px;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <h1>Store Settings</h1>
            """);

    if ("1".equals(
            exchange.getRequestURI().getQuery())) {
        html.append("<p>Settings saved successfully.</p>");
    } else if ("saved=1".equals(
            exchange.getRequestURI().getRawQuery())) {
        html.append("<p>Settings saved successfully.</p>");
    }

    html.append("""
            <form method="post" action="/settings">
                <label>Store Name *</label>
            """);
    html.append("<input name=\"storeName\" required value=\"")
            .append(escapeHtml(settings.getStoreName()))
            .append("\">");

    html.append("<label>Store Address</label>")
            .append("<input name=\"storeAddress\" value=\"")
            .append(escapeHtml(settings.getStoreAddress()))
            .append("\">");

    html.append("<label>Phone</label>")
            .append("<input name=\"phone\" value=\"")
            .append(escapeHtml(settings.getPhone()))
            .append("\">");

    html.append("<label>Email</label>")
            .append("<input type=\"email\" name=\"email\" value=\"")
            .append(escapeHtml(settings.getEmail()))
            .append("\">");

    html.append("""
                <label>Currency *</label>
                <select name="currency" required>
            """);

    for (String currency : List.of(
            "INR", "USD", "EUR", "GBP")) {
        html.append("<option value=\"")
                .append(currency)
                .append("\"")
                .append(currency.equals(settings.getCurrency())
                        ? " selected" : "")
                .append(">")
                .append(currency)
                .append("</option>");
    }

    html.append("</select>");

    html.append("<label>Tax Number</label>")
            .append("<input name=\"taxNumber\" value=\"")
            .append(escapeHtml(settings.getTaxNumber()))
            .append("\">");

    html.append("<label>Invoice Prefix</label>")
            .append("<input name=\"invoicePrefix\" value=\"")
            .append(escapeHtml(settings.getInvoicePrefix()))
            .append("\">");

    html.append("<label><input type=\"checkbox\" ")
            .append("name=\"showTaxOnInvoice\" value=\"true\"")
            .append(settings.isShowTaxOnInvoice()
                    ? " checked" : "")
            .append("> Show Tax on Invoice</label>");

    html.append("""
                <button type="submit">Save Settings</button>
            </form>
            <p>
                <a href="/audit-logs">Audit Logs</a> |
                <a href="/backup">Backup Database</a>
            </p>
            <p><a href="/">Back to Dashboard</a></p>
            <p><a href="/">Back to Dashboard</a></p>
            </body>
            </html>
            """);

    sendResponse(exchange, 200, html.toString());
}


private void handleAuditLogs(HttpExchange exchange)
        throws IOException {

    Session session = requireAuth(exchange);
    if (session == null) return;

    User currentUser = userService
            .findByUsername(session.getUsername())
            .orElse(null);

    if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
        sendResponse(exchange, 403, "Forbidden: Admin access required.");
        return;
    }

    if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set("Allow", "GET");
        sendResponse(exchange, 405, "Method Not Allowed");
        return;
    }

    try {
        AuditLogService service =
                new AuditLogService(new AuditLogRepositoryImpl());
        List<AuditLog> logs = service.getAllLogs();

        StringBuilder html = new StringBuilder("""
                <!DOCTYPE html><html><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Retail POS - Audit Logs</title>
                <style>
                body{font-family:Arial,sans-serif;margin:24px;background:#f4f6f9}
                .wrap{overflow-x:auto;background:white;padding:12px}
                table{border-collapse:collapse;width:100%}
                th,td{border:1px solid #ddd;padding:10px;text-align:left}
                th{background:#e9edf3}
                </style></head><body>
                <h1>Audit Log / Activity Log</h1>
                <p><a href="/settings">Back to Settings</a> |
                <a href="/">Back to Dashboard</a></p>
                <div class="wrap"><table>
                <tr><th>Username</th><th>Action</th><th>Module</th>
                <th>Description</th><th>Created At</th></tr>
                """);

        for (AuditLog log : logs) {
            html.append("<tr><td>")
                    .append(escapeHtml(String.valueOf(log.getUsername())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(log.getAction())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(log.getModule())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(log.getDescription())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(log.getCreatedAt())))
                    .append("</td></tr>");
        }

        html.append("</table></div></body></html>");
        sendResponse(exchange, 200, html.toString());
    } catch (Exception ex) {
        ex.printStackTrace();
        sendResponse(exchange, 500,
                "Unable to load audit logs. Check application logs.");
    }
}

private void handleDatabaseBackup(HttpExchange exchange)
        throws IOException {

    Session session = requireAuth(exchange);
    if (session == null) return;

    User currentUser = userService
            .findByUsername(session.getUsername())
            .orElse(null);

    if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
        sendResponse(exchange, 403, "Forbidden: Admin access required.");
        return;
    }

    if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set("Allow", "GET");
        sendResponse(exchange, 405, "Method Not Allowed");
        return;
    }

    Path backupPath = null;
    try {
        backupPath = Files.createTempFile("RetailPOS_Backup_", ".json");
        new BackupService().createBackup(backupPath.toFile());
        byte[] bytes = Files.readAllBytes(backupPath);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set(
                "Content-Disposition",
                "attachment; filename=\"RetailPOS_Backup.json\"");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        if (exchange.getResponseCode() == -1) {
            sendResponse(exchange, 500,
                    "Backup failed. Check application logs.");
        }
    } finally {
        if (backupPath != null) {
            try {
                Files.deleteIfExists(backupPath);
            } catch (IOException ignored) {
                // Best-effort temporary-file cleanup.
            }
        }
    }
}

    private void handleDeviceStatusData(HttpExchange exchange) throws IOException {
        Session session = requireAuth(exchange);
        if (session == null) {
            return;
        }

        User currentUser = userService
                .findByUsername(session.getUsername())
                .orElse(null);

        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            sendResponse(exchange, 403, "Forbidden: Admin access required.");
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            sendResponse(exchange, 405, "Method not allowed.");
            return;
        }

        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

        StringBuilder rows = new StringBuilder();
        List<Device> devices = deviceRepository.findAll();

        if (devices.isEmpty()) {
            rows.append("<tr><td colspan=\"9\">No devices registered.</td></tr>");
        }

        for (Device device : devices) {
            String id = device.getId() == null ? "" : device.getId();
            String status = device.getStatus() == null ? "UNKNOWN" : device.getStatus();

            rows.append("<tr><td>")
                    .append(escapeHtml(String.valueOf(device.getUsername())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getDeviceName())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getOperatingSystem())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getOsVersion())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getIpAddress())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getRegisteredAt())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getLastLoginAt())))
                    .append("</td><td>")
                    .append(escapeHtml(status))
                    .append("</td><td>");

            rows.append("<form method=\"post\" action=\"/devices\" style=\"display:inline\">")
                    .append("<input type=\"hidden\" name=\"id\" value=\"")
                    .append(escapeHtml(id))
                    .append("\"><input type=\"hidden\" name=\"action\" value=\"toggle\">")
                    .append("<button class=\"btn primary\" type=\"submit\">")
                    .append("ACTIVE".equalsIgnoreCase(status) ? "Block" : "Activate")
                    .append("</button></form>");

            rows.append("<form method=\"post\" action=\"/devices\" style=\"display:inline\" ")
                    .append("onsubmit=\"return confirm('Permanently remove this device record?')\">")
                    .append("<input type=\"hidden\" name=\"id\" value=\"")
                    .append(escapeHtml(id))
                    .append("\"><input type=\"hidden\" name=\"action\" value=\"remove\">")
                    .append("<button class=\"btn danger\" type=\"submit\">Remove</button></form>");

            rows.append("</td></tr>");
        }

        sendResponse(exchange, 200, rows.toString());
    }
private void handleDevices(HttpExchange exchange)
        throws IOException {

    Session session = requireAuth(exchange);
    if (session == null) return;

    User currentUser = userService
            .findByUsername(session.getUsername())
            .orElse(null);

    if (currentUser == null
            || !"ADMIN".equals(currentUser.getRole())) {
        sendResponse(exchange, 403,
                "Forbidden: Admin access required.");
        return;
    }

    String method = exchange.getRequestMethod();
    String message = "";

    try {
        if ("POST".equalsIgnoreCase(method)) {
            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Map<String, String> formData = parseFormData(body);
            String action = formData.get("action");
            String id = formData.get("id");
            id = id == null ? null : id.trim();

            if (id == null || id.isBlank()
                    || action == null) {
                sendResponse(exchange, 400, "Invalid device request.");
                return;
            }

            Device selected = null;

            for (Device device : deviceRepository.findAll()) {
                if (id.equals(device.getId())) {
                    selected = device;
                    break;
                }
            }

            if (selected == null) {
                sendResponse(exchange, 404, "Device not found.");
                return;
            }

            if ("toggle".equals(action)) {
                String status = selected.getStatus();

                if ("ACTIVE".equalsIgnoreCase(status)) {
                    selected.setStatus("BLOCKED");
                } else if ("BLOCKED".equalsIgnoreCase(status)) {
                    selected.setStatus("ACTIVE");
                } else {
                    sendResponse(exchange, 400,
                            "Unknown device status.");
                    return;
                }

                deviceRepository.update(selected);
                if ("BLOCKED".equalsIgnoreCase(selected.getStatus())) {
                    for (Session activeSession : sessionService.getActiveSessions()) {
                        if (selected.getUserId() != null
                                && selected.getDeviceId() != null
                                && selected.getUserId().equals(activeSession.getUserId())
                                && selected.getDeviceId().equals(activeSession.getDeviceId())) {
                            sessionService.forceLogout(activeSession.getId());
                        }
                    }
                }
                message = "Device status updated.";
            } else if ("remove".equals(action)) {
                deviceRepository.deleteById(selected.getId());
                message = "Device record removed.";
            } else {
                sendResponse(exchange, 400, "Unknown device action.");
                return;
            }

        } else if (!"GET".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Allow", "GET, POST");
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        List<Device> devices = deviceRepository.findAll();

        StringBuilder html = new StringBuilder("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Retail POS - Device Management</title>
                <style>
                body{font-family:Arial,sans-serif;margin:24px;background:#f4f6f9;color:#222}
                .wrap{overflow-x:auto;background:white;padding:14px;border-radius:8px}
                table{border-collapse:collapse;width:100%;background:white}
                th,td{border:1px solid #ddd;padding:9px;text-align:left;white-space:nowrap}
                th{background:#e9edf3}
                .btn{padding:7px 10px;border:0;border-radius:4px;cursor:pointer;margin:2px}
                .primary{background:#1769aa;color:white}
                .danger{background:#b42318;color:white}
                .notice{padding:10px;background:#e7f6e7;margin:12px 0}
                input[type=search]{padding:9px;width:min(320px,90%)}
                a{margin-right:12px}
                </style>
                </head>
                <body>
                <h1>Device Management</h1>
                <p><a href="/">Dashboard</a><a href="/settings">Settings</a>
                <a href="/audit-logs">Audit Logs</a></p>
                """);

        if (!message.isBlank()) {
            html.append("<p class=\"notice\">")
                    .append(escapeHtml(message))
                    .append("</p>");
        }

        html.append("""
                <p>
                <input id="deviceSearch" type="search"
                    placeholder="Search username, device, OS, IP or status">
                <button class="btn primary" onclick="refreshDeviceTable()">Refresh</button> <span id="deviceRefreshStatus">Auto-refresh: 5 seconds</span>
                </p>
                <div class="wrap"><table id="devicesTable">
                <thead><tr>
                <th>Username</th><th>Device Name</th>
                <th>Operating System</th><th>OS Version</th>
                <th>IP Address</th><th>Registered At</th>
                <th>Last Login</th><th>Status</th><th>Actions</th>
                </tr></thead><tbody>
                """);

        for (Device device : devices) {
            String id = device.getId() == null ? "" : device.getId();
            String status = device.getStatus() == null
                    ? "UNKNOWN" : device.getStatus();

            html.append("<tr><td>")
                    .append(escapeHtml(String.valueOf(device.getUsername())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getDeviceName())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getOperatingSystem())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getOsVersion())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getIpAddress())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getRegisteredAt())))
                    .append("</td><td>")
                    .append(escapeHtml(String.valueOf(device.getLastLoginAt())))
                    .append("</td><td>")
                    .append(escapeHtml(status))
                    .append("</td><td>");

            html.append("""
                    <form method="post" action="/devices" style="display:inline">
                    <input type="hidden" name="id" value="
                    """)
                    .append(escapeHtml(id))
                    .append("\"><input type=\"hidden\" name=\"action\" value=\"toggle\">")
                    .append("<button class=\"btn primary\" type=\"submit\">")
                    .append("ACTIVE".equalsIgnoreCase(status)
                            ? "Block" : "Activate")
                    .append("</button></form>");

            html.append("""
                    <form method="post" action="/devices" style="display:inline"
                    onsubmit="return confirm('Permanently remove this device record?')">
                    <input type="hidden" name="id" value="
                    """)
                    .append(escapeHtml(id))
                    .append("\"><input type=\"hidden\" name=\"action\" value=\"remove\">")
                    .append("<button class=\"btn danger\" type=\"submit\">Remove</button></form>");

            html.append("</td></tr>");
        }

        html.append("""
                </tbody></table></div>
                <script>
                document.getElementById('deviceSearch').addEventListener('input',function(){
                    const q=this.value.toLowerCase();
                    document.querySelectorAll('#devicesTable tbody tr').forEach(function(row){
                        row.style.display=row.innerText.toLowerCase().includes(q)?'':'none';
                    });
                });
                async function refreshDeviceTable() {
                    const status = document.getElementById('deviceRefreshStatus');
                    try {
                        const response = await fetch('/device-status-data', {
                            cache: 'no-store',
                            credentials: 'same-origin'
                        });

                        if (response.redirected) {
                            window.location.replace('/login');
                            return;
                        }

                        if (response.status === 401) {
                            window.location.replace('/login');
                            return;
                        }

                        if (!response.ok) {
                            throw new Error('HTTP ' + response.status);
                        }

                        const rows = await response.text();
                        document.querySelector('#devicesTable tbody').innerHTML = rows;

                        const q = document.getElementById('deviceSearch').value.toLowerCase();
                        document.querySelectorAll('#devicesTable tbody tr').forEach(function(row) {
                            row.style.display = row.innerText.toLowerCase().includes(q) ? '' : 'none';
                        });

                        status.textContent = 'Last updated: ' + new Date().toLocaleTimeString();
                    } catch (error) {
                        status.textContent = 'Refresh failed; retrying automatically';
                    }
                }

                setInterval(refreshDeviceTable, 5000);                </script>
                </body></html>
                """);

        sendResponse(exchange, 200, html.toString());

    } catch (Exception ex) {
        ex.printStackTrace();
        sendResponse(exchange, 500,
                "Unable to load or update devices. Check application logs.");
    }
}

private void handleReports(HttpExchange exchange)
        throws IOException {
Session session = requireAuth(exchange);

if (session == null) {
    return;
}

User currentUser = getCurrentUser(session);
System.out.println(
        "REPORT DEBUG: username="
        + (currentUser == null ? "null" : currentUser.getUsername())
        + ", role=["
        + (currentUser == null ? "null" : currentUser.getRole())
        + "]"
);

if (!hasAnyRole(currentUser, "ADMIN", "MANAGER")) {
    sendResponse(
            exchange,
            403,
            "Forbidden: You do not have permission to view reports."
    );
    return;
}

try {
    List<Sale> sales = saleService.getAllSales();

    List<Purchase> purchases =
            purchaseService.getAllPurchases();


        double totalSales = 0.0;
        double totalPurchases = 0.0;

        for (Sale sale : sales) {
            totalSales += sale.getTotal();
        }

        for (Purchase purchase : purchases) {
            totalPurchases += purchase.getTotal();
        }

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <title>Reports - Retail POS</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 25px;
                            background: #f4f6f9;
                        }
                        .container {
                            background: white;
                            padding: 20px;
                            border-radius: 8px;
                        }
                        .card {
                            padding: 18px;
                            margin: 12px 0;
                            background: #eef3f9;
                            border-radius: 6px;
                        }
                        .button {
                            display: inline-block;
                            padding: 10px 16px;
                            background: #263b5e;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                        }
                    </style>
                </head>
                <body>
                <div class="container">
                <h1>Business Reports</h1>
                <a class="button" href="/">Dashboard</a>
                """);

        html.append("<div class='card'><h3>Total Sales</h3><p>")
                .append(String.format("%.2f", totalSales))
                .append("</p><p>Sales records: ")
                .append(sales.size())
                .append("</p></div>");

        html.append("<div class='card'><h3>Total Purchases</h3><p>")
                .append(String.format("%.2f", totalPurchases))
                .append("</p><p>Purchase records: ")
                .append(purchases.size())
                .append("</p></div>");

        html.append("<div class='card'><h3>Sales Minus Purchases</h3><p>")
                .append(String.format(
                        "%.2f", totalSales - totalPurchases))
                .append("</p></div>");

        html.append("""
                </div>
                </body>
                </html>
                """);

        sendResponse(exchange, 200, html.toString());

    } catch (Exception e) {
        e.printStackTrace();
        sendResponse(
                exchange,
                500,
                "Failed to load reports: "
                        + escapeHtml(String.valueOf(e.getMessage()))
        );
    }
}

private void handleUsers(HttpExchange exchange) throws IOException {

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    User currentUser = userService
            .findByUsername(session.getUsername())
            .orElse(null);

    if (currentUser == null
            || !"ADMIN".equals(currentUser.getRole())) {

        sendResponse(
                exchange,
                403,
                "Forbidden: Admin access required."
        );
        return;
    }


String method = exchange.getRequestMethod();

if ("POST".equalsIgnoreCase(method)) {

    String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
    );

    Map<String, String> formData = parseFormData(body);

String action = formData.get("action");

if ("delete".equals(action)) {
    String userId = formData.get("userId");

    if (userId == null || userId.isBlank()) {
        sendResponse(exchange, 400, "User ID is required.");
        return;
    }

    try {
        var targetUser = userService.findById(userId);

        if (targetUser.isEmpty()) {
            sendResponse(exchange, 404, "User not found.");
            return;
        }

        if (targetUser.get().getUsername()
                .equals(currentUser.getUsername())) {
            sendResponse(
                    exchange, 400,
                    "You cannot delete your own account."
            );
            return;
        }

        userService.deleteUser(userId);

        exchange.getResponseHeaders().set("Location", "/users");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return;

    } catch (Exception e) {
        sendResponse(
                exchange, 500,
                "Unable to delete user. Check application logs."
        );
        return;
    }
}
if ("update".equals(action)) {
    String userId = formData.get("userId");
    String fullName = formData.get("fullName");
    String email = formData.get("email");
    String role = formData.get("role");

    if (userId == null || userId.isBlank()) {
        sendResponse(exchange, 400, "User ID is required.");
        return;
    }

    if (fullName == null || fullName.isBlank()
            || email == null || email.isBlank()) {
        sendResponse(
                exchange, 400,
                "Full Name and Email are required."
        );
        return;
    }
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
    sendResponse(
            exchange, 400,
            "Please enter a valid email address."
    );
    return;
}
    if (!"MANAGER".equals(role)
            && !"CASHIER".equals(role)) {
        sendResponse(exchange, 400, "Invalid role.");
        return;
    }

    try {
        var result = userService.findById(userId);

        if (result.isEmpty()) {
            sendResponse(exchange, 404, "User not found.");
            return;
        }

        User targetUser = result.get();

        if (targetUser.getUsername()
                .equals(currentUser.getUsername())) {
            sendResponse(
                    exchange, 400,
                    "You cannot edit your own account here."
            );
            return;
        }

        targetUser.setFullName(fullName.trim());
        targetUser.setEmail(email.trim());
        targetUser.setRole(role);

        userService.updateUser(targetUser);

        exchange.getResponseHeaders()
                .set("Location", "/users");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return;

    } catch (Exception e) {
        sendResponse(
                exchange, 500,
                "Unable to update user. Check application logs."
        );
        return;
    }
}

    String username = formData.get("username");
    String password = formData.get("password");
    String fullName = formData.get("fullName");
    String email = formData.get("email");
    String role = formData.get("role");

    // Only these roles can be created through the web page.
    if (!"MANAGER".equals(role)
            && !"CASHIER".equals(role)) {

        sendResponse(
                exchange,
                400,
                "Invalid role. Select MANAGER or CASHIER."
        );
        return;
    }

    try {
        userService.createUser(
                username,
                password,
                fullName,
                email,
                role
        );

        exchange.getResponseHeaders().set(
                "Location",
                "/users"
        );

        exchange.sendResponseHeaders(303, -1);
        exchange.close();
        return;

    } catch (IllegalArgumentException e) {

        sendResponse(
                exchange,
                400,
                "Unable to create user: "
                        + escapeHtml(e.getMessage())
        );
        return;

    } catch (Exception e) {

        sendResponse(
                exchange,
                500,
                "Unable to create user. Check application logs."
        );
        return;
    }
}

if (!"GET".equalsIgnoreCase(method)) {

    exchange.getResponseHeaders().set("Allow", "GET, POST");

    sendResponse(
            exchange,
            405,
            "Method Not Allowed"
    );
    return;
}


String query = exchange.getRequestURI().getRawQuery();
String editUserId = null;

if (query != null) {
    for (String parameter : query.split("&")) {
        String[] parts = parameter.split("=", 2);

        if (parts.length == 2
                && "userId".equals(parts[0])) {
            editUserId = java.net.URLDecoder.decode(
                    parts[1],
                    StandardCharsets.UTF_8
            );
            break;
        }
    }
}

boolean editMode = query != null
        && query.contains("action=edit");

User editUser = null;

if (editMode) {
    if (editUserId == null || editUserId.isBlank()) {
        sendResponse(exchange, 400, "User ID is required.");
        return;
    }

    var result = userService.findById(editUserId);

    if (result.isEmpty()) {
        sendResponse(exchange, 404, "User not found.");
        return;
    }

    editUser = result.get();

    if (editUser.getUsername().equals(currentUser.getUsername())) {
        sendResponse(
                exchange,
                400,
                "You cannot edit your own account here."
        );
        return;
    }
}

    StringBuilder html = new StringBuilder();

    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Users - Retail POS</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    margin: 30px;
                    background: #f4f6f9;
                }
                h1 { color: #243447; }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    background: white;
                }
                th, td {
                    padding: 12px;
                    border: 1px solid #ddd;
                    text-align: left;
                }
                th { background: #243447; color: white; }
                a { color: #1769aa; }
            </style>
        </head>
        <body>

<h1>Retail POS - Users</h1>
<p>Registered system users</p>
        """);

    if (editMode) {
        html.append("""
                <h2>Edit User</h2>
                <form method="post" action="/users">
                    <input type="hidden" name="action" value="update">
                """);

        html.append("<input type=\"hidden\" name=\"userId\" value=\"")
                .append(escapeHtml(editUser.getId()))
                .append("\">");

        html.append("<label>Username:</label><br>")
                .append("<input type=\"text\" value=\"")
                .append(escapeHtml(editUser.getUsername()))
                .append("\" readonly><br><br>");

        html.append("<label>Full Name:</label><br>")
                .append("<input type=\"text\" name=\"fullName\" value=\"")
                .append(escapeHtml(editUser.getFullName()))
                .append("\" required><br><br>");

        html.append("<label>Email:</label><br>")
                .append("<input type=\"email\" name=\"email\" value=\"")
                .append(escapeHtml(editUser.getEmail()))
                .append("\" required><br><br>");

        html.append("<label>Role:</label><br>")
                .append("<select name=\"role\" required>");

        html.append("<option value=\"MANAGER\"")
                .append("MANAGER".equals(editUser.getRole()) ? " selected" : "")
                .append(">MANAGER</option>");

        html.append("<option value=\"CASHIER\"")
                .append("CASHIER".equals(editUser.getRole()) ? " selected" : "")
                .append(">CASHIER</option>");

        html.append("""
                    </select><br><br>
                    <button type="submit">Update User</button>
                    <a href="/users">Cancel</a>
                </form>
                """);
    } else {
        html.append("""
                <h2>Create New User</h2>

                <form method="post" action="/users">
                    <label>Username:</label><br>
                    <input type="text" name="username"
                           required autocomplete="off"><br><br>

                    <label>Password:</label><br>
                    <input type="password" name="password"
                           required minlength="6"
                           autocomplete="new-password"><br><br>

                    <label>Full Name:</label><br>
                    <input type="text" name="fullName"
                           required><br><br>

                    <label>Email:</label><br>
                    <input type="email" name="email"
                           required><br><br>

                    <label>Role:</label><br>
                    <select name="role" required>
                        <option value="MANAGER">MANAGER</option>
                        <option value="CASHIER">CASHIER</option>
                    </select><br><br>

                    <button type="submit">Create User</button>
                </form>
                """);
        }

    html.append("""
            <hr>
            <h2>Registered Users</h2>
            <table>
                <tr>
                    <th>Username</th>
                    <th>Full Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            """);

    try {
        for (var user : userService.getAllUsers()) {
            html.append("<tr>");
            html.append("<td>")
                .append(escapeHtml(user.getUsername()))
                .append("</td>");
            html.append("<td>")
                .append(escapeHtml(user.getFullName()))
                .append("</td>");
            html.append("<td>")
                .append(escapeHtml(user.getEmail()))
                .append("</td>");
            html.append("<td>")
                .append(escapeHtml(user.getRole()))
                .append("</td>");
            html.append("<td>")
                .append(escapeHtml(user.getStatus()))
                .append("</td>");
                html.append("<td>");

if (!user.getUsername().equals(currentUser.getUsername())) {
    html.append("<a href=\"/users?action=edit&userId=")
        .append(escapeHtml(user.getId()))
        .append("\">Edit</a> ");
    html.append("""
        <form method="post" action="/users"
              onsubmit="return confirm('Are you sure you want to delete this user?');">
            <input type="hidden" name="action" value="delete">
        """);

    html.append("<input type=\"hidden\" name=\"userId\" value=\"")
            .append(escapeHtml(user.getId()))
            .append("\">");

    html.append("""
            <button type="submit">Delete</button>
        </form>
        """);

} else {
    html.append("Current account");
}

html.append("</td>");
            html.append("</tr>");
        }
    } catch (Exception e) {
        html.append("""
            <tr><td colspan="5">
                Unable to load users. Please check the application logs.
            </td></tr>
            """);
    }

    html.append("""
            </table>
            <p><a href="/">Back to Dashboard</a></p>
        </body>
        </html>
            """);

    sendResponse(exchange, 200, html.toString());
}
private void handleLogin(
        HttpExchange exchange
) throws IOException {

    exchange.getResponseHeaders().set(
            "Content-Type",
            "text/html; charset=UTF-8"
    );

    if ("GET".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Retail POS Login</title>
                </head>
                <body>
                    <h1>Retail POS Login</h1>
                    <form method="post" action="/login">
                        <label>Username:</label>
                        <input type="text" name="username"
                               required autocomplete="username">
                        <br><br>
                        <label>Password:</label>
                        <input type="password" name="password"
                               required autocomplete="current-password">
                        <br><br>
                        <button type="submit">Login</button>
                    </form>
                </body>
                </html>
                """;

        sendResponse(exchange, 200, html);
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        sendResponse(exchange, 405, "Method Not Allowed");
        return;
    }

    String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
    );

    Map<String, String> formData = parseFormData(body);
    String username = formData.get("username");
    String password = formData.get("password");

    User user = authService.login(
            username,
            password
    ).orElse(null);

    if (user == null) {
        sendResponse(
                exchange,
                401,
                "Login failed. Check your credentials or account status."
        );
        return;
    }

    if (user.getEmail() == null
            || user.getEmail().isBlank()) {

        sendResponse(
                exchange,
                400,
                "An email address is required for OTP verification."
        );
        return;
    }

    if (otpService.isOtpLocked(user)) {
        sendResponse(
                exchange,
                429,
                "OTP verification is temporarily locked."
        );
        return;
    }

    String browserId = null;

String cookieHeader = exchange.getRequestHeaders()
        .getFirst("Cookie");

if (cookieHeader != null) {
    for (String cookie : cookieHeader.split(";")) {
        String[] parts = cookie.trim().split("=", 2);

        if (parts.length == 2
                && "RETAILPOS_DEVICE".equals(parts[0])
                && parts[1].matches("[a-fA-F0-9-]{36}")) {
            browserId = parts[1];
            break;
        }
    }
}

boolean newBrowser = browserId == null;

if (newBrowser) {
    browserId = UUID.randomUUID().toString();
}

String deviceId = "WEB-" + browserId;
    String pendingToken = UUID.randomUUID().toString();
    if (newBrowser) {
    exchange.getResponseHeaders().add(
            "Set-Cookie",
            "RETAILPOS_DEVICE=" + browserId
                    + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=31536000"
    );
}

    try {
        EmailService emailService = new EmailService();

        var otpCode = otpService.generateOtp(
                user,
                deviceId
        );

        emailService.sendOtpEmail(
                user.getEmail(),
                user.getUsername(),
                otpCode.getCode()
        );

        pendingWebLogins.put(
                pendingToken,
                new PendingWebLogin(
                        user,
                        deviceId,
                        LocalDateTime.now().plusMinutes(5)
                )
        );

    } catch (RuntimeException e) {
        System.err.println(
                "Web login OTP delivery failed: "
                + e.getMessage()
        );

        sendResponse(
                exchange,
                500,
                "Could not send verification email. Please try again."
        );
        return;
    }

    String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Verify OTP</title>
            </head>
            <body>
                <h1>Email Verification</h1>
                <p>Enter the six-digit OTP sent to your registered email.</p>
                <form method="post" action="/verify-otp">
                    <input type="hidden" name="token" value="%s">
                    <label>OTP:</label>
                    <input type="text" name="otp"
                           inputmode="numeric"
                           pattern="[0-9]{6}"
                           maxlength="6" required>
                    <button type="submit">Verify OTP</button>
                </form>
            </body>
            </html>
            """.formatted(pendingToken);

    sendResponse(exchange, 200, html);
}
private void handleVerifyOtp(
        HttpExchange exchange
) throws IOException {

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {
        sendResponse(exchange, 405, "Method Not Allowed");
        return;
    }

    String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
    );

    Map<String, String> formData = parseFormData(body);

    String token = formData.get("token");
    String enteredOtp = formData.get("otp");

    if (token == null
            || enteredOtp == null
            || !enteredOtp.matches("[0-9]{6}")) {
        sendResponse(exchange, 400, "Invalid OTP request.");
        return;
    }

    PendingWebLogin pending = pendingWebLogins.get(token);

    if (pending == null) {
        sendResponse(exchange, 401, "Invalid or expired login request.");
        return;
    }

    if (LocalDateTime.now().isAfter(pending.expiresAt)) {
        pendingWebLogins.remove(token);
        sendResponse(exchange, 401, "OTP expired. Please log in again.");
        return;
    }

    boolean verified = otpService.verifyOtp(
            pending.user,
            pending.deviceId,
            enteredOtp
    );

    if (!verified) {
        sendResponse(exchange, 401, "Incorrect or expired OTP.");
        return;
    }

   Optional<Device> existingDevice =
        deviceRepository.findByUserIdAndDeviceId(
                pending.user.getId(),
                pending.deviceId
        );

if (existingDevice.isPresent()
        && "BLOCKED".equalsIgnoreCase(
                existingDevice.get().getStatus())) {

    pendingWebLogins.remove(token);

    sendResponse(
            exchange,
            403,
            "This device is blocked. Contact an administrator."
    );
    return;
}

String userAgent = exchange.getRequestHeaders()
        .getFirst("User-Agent");

String operatingSystem = "Unknown";
String deviceName = "Web Browser";

if (userAgent != null) {
    if (userAgent.contains("Android")) {
        operatingSystem = "Android";
        deviceName = "Android Device";
    } else if (userAgent.contains("iPhone")) {
        operatingSystem = "iOS";
        deviceName = "iPhone";
    } else if (userAgent.contains("iPad")) {
        operatingSystem = "iOS";
        deviceName = "iPad";
    } else if (userAgent.contains("Windows")) {
        operatingSystem = "Windows";
        deviceName = "Windows PC";
    } else if (userAgent.contains("Mac OS")) {
        operatingSystem = "macOS";
        deviceName = "Mac";
    } else if (userAgent.contains("Linux")) {
        operatingSystem = "Linux";
        deviceName = "Linux Device";
    }
}

String ipAddress = exchange.getRemoteAddress()
        .getAddress()
        .getHostAddress();

if (existingDevice.isPresent()) {
    Device device = existingDevice.get();
    device.setLastLoginAt(LocalDateTime.now());
    device.setIpAddress(ipAddress);
    device.setOperatingSystem(operatingSystem);
    device.setDeviceName(deviceName);
    deviceRepository.update(device);
} else {
    Device device = new Device(
            pending.deviceId,
            pending.user.getId(),
            pending.user.getUsername(),
            deviceName,
            operatingSystem,
            userAgent == null ? "" : userAgent,
            ipAddress
    );

    deviceRepository.save(device);
}

pendingWebLogins.remove(token);

var session = sessionService.createSession(
        pending.user.getId(),
        pending.user.getUsername(),
        pending.deviceId
);

exchange.getResponseHeaders().set(
        "Set-Cookie",
        "RETAILPOS_SESSION=" + session.getId()
                + "; Path=/; HttpOnly; SameSite=Strict"
);

exchange.getResponseHeaders().set("Location", "/");
exchange.sendResponseHeaders(303, -1);
exchange.close();
}


private static class PendingWebLogin {

    private final User user;
    private final String deviceId;
    private final LocalDateTime expiresAt;

    private PendingWebLogin(
            User user,
            String deviceId,
            LocalDateTime expiresAt
    ) {
        this.user = user;
        this.deviceId = deviceId;
        this.expiresAt = expiresAt;
    }
}

private void handleLogout(HttpExchange exchange)
        throws IOException {

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
                "Allow", "POST"
        );

        sendResponse(
                exchange,
                405,
                "Method Not Allowed"
        );

        return;
    }

    Session session = requireAuth(exchange);

    if (session == null) {
        return;
    }

    sessionService.logout(session);

    exchange.getResponseHeaders().set(
            "Set-Cookie",
            "RETAILPOS_SESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"
    );

    exchange.getResponseHeaders().set(
            "Location",
            "/login"
    );

    exchange.sendResponseHeaders(303, -1);
    exchange.close();
}

}
