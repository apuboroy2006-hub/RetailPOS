package com.retailpos.repository;

import com.retailpos.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);

    List<User> findAll();

    void update(User user);

    void deleteById(String id);
}