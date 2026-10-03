package com.retailpos.security;

import com.retailpos.model.User;
import com.retailpos.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> optionalUser =
                userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();

        if (!"ACTIVE".equals(user.getStatus())) {
            return Optional.empty();
        }

        boolean passwordMatches = BCrypt.checkpw(
                password,
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
}