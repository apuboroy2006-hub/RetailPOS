package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Customer;

public interface CustomerRepository {

    void save(Customer customer);

    Optional<Customer> findById(String id);

    Optional<Customer> findByPhone(String phone);

    List<Customer> findAll();

    List<Customer> findByName(String name);

    void update(Customer customer);

    void deleteById(String id);
}