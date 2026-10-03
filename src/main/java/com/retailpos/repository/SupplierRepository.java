package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Supplier;

public interface SupplierRepository {

    void save(Supplier supplier);

    Optional<Supplier> findById(String id);

    Optional<Supplier> findByPhone(String phone);

    List<Supplier> findAll();

    List<Supplier> findByName(String name);

    void update(Supplier supplier);

    void deleteById(String id);
}