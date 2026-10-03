package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Customer;
import com.retailpos.repository.CustomerRepository;

public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(
            CustomerRepository customerRepository
    ) {
        this.customerRepository =
                customerRepository;
    }

    // =========================
    // CREATE CUSTOMER
    // =========================

    public void createCustomer(
            String name,
            String phone,
            String email,
            String address
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer name is required."
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        if (customerRepository
                .findByPhone(phone)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Phone number already exists."
            );
        }

        Customer customer =
                new Customer(
                        name,
                        phone,
                        email,
                        address,
                        "ACTIVE"
                );

        customerRepository.save(customer);
    }

    // =========================
    // FIND BY ID
    // =========================

    public Optional<Customer> findById(
            String id
    ) {

        return customerRepository.findById(id);
    }

    // =========================
    // FIND BY PHONE
    // =========================

    public Optional<Customer> findByPhone(
            String phone
    ) {

        return customerRepository.findByPhone(
                phone
        );
    }

    // =========================
    // GET ALL CUSTOMERS
    // =========================

    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Customer> searchByName(
            String name
    ) {

        if (name == null || name.isBlank()) {
            return getAllCustomers();
        }

        return customerRepository.findByName(
                name
        );
    }

    // =========================
    // UPDATE CUSTOMER
    // =========================

    public void updateCustomer(
            Customer customer
    ) {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer is required."
            );
        }

        if (customer.getId() == null
                || customer.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        if (customer.getName() == null
                || customer.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name is required."
            );
        }

        if (customer.getPhone() == null
                || customer.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        Optional<Customer> existing =
                customerRepository.findByPhone(
                        customer.getPhone()
                );

        if (existing.isPresent()
                && !existing.get()
                .getId()
                .equals(customer.getId())) {

            throw new IllegalArgumentException(
                    "Phone number already exists."
            );
        }

        customerRepository.update(customer);
    }

    // =========================
    // DELETE CUSTOMER
    // =========================

    public void deleteCustomer(
            String id
    ) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        customerRepository.deleteById(id);
    }
}