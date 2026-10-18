package com.retailpos.repository.railway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.retailpos.model.Product;
import com.retailpos.repository.ProductRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RailwayProductRepositoryImpl
        implements ProductRepository {

    private static final String BASE_URL =
            "https://zesty-fascination-production-cf07.up.railway.app/api/products";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RailwayProductRepositoryImpl() {

        httpClient = HttpClient.newHttpClient();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(Product product) {

        try {

            String json =
                    objectMapper.writeValueAsString(product);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            checkResponse(response);

            Product savedProduct =
                    objectMapper.readValue(
                            response.body(),
                            Product.class
                    );

            product.setId(savedProduct.getId());
            product.setCreatedAt(
                    savedProduct.getCreatedAt()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create product through Railway API: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    @Override
    public Optional<Product> findById(String id) {

        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            BASE_URL + "/" + id
                                    )
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 404) {
                return Optional.empty();
            }

            checkResponse(response);

            Product product =
                    objectMapper.readValue(
                            response.body(),
                            Product.class
                    );

            return Optional.of(product);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to find product through Railway API: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    @Override
    public Optional<Product> findBySku(String sku) {

        if (sku == null || sku.isBlank()) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(product ->
                        product.getSku() != null
                                && product.getSku()
                                .equalsIgnoreCase(sku)
                )
                .findFirst();
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {

        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(product ->
                        product.getBarcode() != null
                                && product.getBarcode()
                                .equalsIgnoreCase(barcode)
                )
                .findFirst();
    }

    @Override
    public List<Product> findAll() {

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            checkResponse(response);

            Product[] products =
                    objectMapper.readValue(
                            response.body(),
                            Product[].class
                    );

            return Arrays.asList(products);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load products from Railway API: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    @Override
    public List<Product> findByName(String name) {

        if (name == null || name.isBlank()) {
            return findAll();
        }

        String searchText =
                name.trim().toLowerCase();

        return findAll()
                .stream()
                .filter(product ->
                        product.getName() != null
                                && product.getName()
                                .toLowerCase()
                                .contains(searchText)
                )
                .collect(Collectors.toList());
    }

    @Override
    public void update(Product product) {

        if (product == null ||
                product.getId() == null ||
                product.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid product."
            );
        }

        try {

            String json =
                    objectMapper.writeValueAsString(product);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            BASE_URL + "/"
                                                    + product.getId()
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .PUT(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            checkResponse(response);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to update product through Railway API: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    @Override
    public void deleteById(String id) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            BASE_URL + "/" + id
                                    )
                            )
                            .DELETE()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            checkResponse(response);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to delete product through Railway API: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        int statusCode =
                response.statusCode();

        if (statusCode >= 200 &&
                statusCode < 300) {

            return;
        }

        String body =
                response.body();

        throw new IOException(
                "Railway API returned HTTP "
                        + statusCode
                        + ": "
                        + body
        );
    }

    private String getErrorMessage(Exception e) {

        if (e.getMessage() != null &&
                !e.getMessage().isBlank()) {

            return e.getMessage();
        }

        return e.getClass().getSimpleName();
    }
}