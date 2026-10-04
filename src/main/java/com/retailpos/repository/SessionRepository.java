package com.retailpos.repository;

import java.util.List;
import java.util.Optional;

import com.retailpos.model.Session;

public interface SessionRepository {

    void save(Session session);

    Optional<Session> findById(String id);

    List<Session> findActiveSessions();

    List<Session> findByUsername(String username);

    void update(Session session);

    void deleteById(String id);
}