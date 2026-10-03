package com.retailpos.service;

import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.retailpos.model.User;
import com.retailpos.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(
            String username,
            String password,
            String fullName,
            String role
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters."
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );

        User user = new User(
                username,
                passwordHash,
                fullName,
                role,
                "ACTIVE"
        );

        userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean verifyPassword(
            String password,
            String passwordHash
    ) {

        return BCrypt.checkpw(password, passwordHash);
    }

    public void updateUser(User user) {
        userRepository.update(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
   public void changePassword(
        String username,
        String oldPassword,
        String newPassword
) {

    if (username == null ||
            username.isBlank()) {

        throw new IllegalArgumentException(
                "Username is required."
        );
    }

    if (oldPassword == null ||
            oldPassword.isBlank()) {

        throw new IllegalArgumentException(
                "Current password is required."
        );
    }

    if (newPassword == null ||
            newPassword.length() < 6) {

        throw new IllegalArgumentException(
                "New password must contain at least 6 characters."
        );
    }

    User user =
            userRepository
                    .findByUsername(
                            username
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "User not found."
                            )
                    );

    if (!BCrypt.checkpw(
            oldPassword,
            user.getPasswordHash()
    )) {

        throw new IllegalArgumentException(
                "Current password is incorrect."
        );
    }

    String newPasswordHash =
            BCrypt.hashpw(
                    newPassword,
                    BCrypt.gensalt()
            );

    user.setPasswordHash(
            newPasswordHash
    );

    userRepository.update(
            user
    );
}
}