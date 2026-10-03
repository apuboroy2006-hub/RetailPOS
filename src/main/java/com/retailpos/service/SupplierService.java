package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Supplier;
import com.retailpos.repository.SupplierRepository;

public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public void createSupplier(
            String name,
            String phone,
            String email,
            String address
    ) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        Optional<Supplier> existing =
                supplierRepository.findByPhone(phone.trim());

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "A supplier with this phone number already exists."
            );
        }

        Supplier supplier = new Supplier(
                name.trim(),
                phone.trim(),
                email == null ? "" : email.trim(),
                address == null ? "" : address.trim(),
                "ACTIVE"
        );

        supplierRepository.save(supplier);
    }

    public Optional<Supplier> findById(String id) {
        return supplierRepository.findById(id);
    }

    public Optional<Supplier> findByPhone(String phone) {
        return supplierRepository.findByPhone(phone);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> searchByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return supplierRepository.findAll();
        }

        return supplierRepository.findByName(name.trim());
    }

    public void updateSupplier(Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null.");
        }

        if (supplier.getId() == null ||
                supplier.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID is required.");
        }

        if (supplier.getName() == null ||
                supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        if (supplier.getPhone() == null ||
                supplier.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        Optional<Supplier> existing =
                supplierRepository.findByPhone(
                        supplier.getPhone().trim()
                );

        if (existing.isPresent()
                && !existing.get().getId().equals(supplier.getId())) {

            throw new IllegalArgumentException(
                    "Another supplier with this phone number already exists."
            );
        }

        supplier.setName(supplier.getName().trim());
        supplier.setPhone(supplier.getPhone().trim());

        if (supplier.getEmail() == null) {
            supplier.setEmail("");
        }

        if (supplier.getAddress() == null) {
            supplier.setAddress("");
        }

        supplierRepository.update(supplier);
    }

    public void deleteSupplier(String id) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID is required.");
        }

        supplierRepository.deleteById(id);
    }
}