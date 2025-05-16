package com.example.breakfreeBE.userRegistration.service;

import com.example.breakfreeBE.avatar.entity.Avatar;
import com.example.breakfreeBE.avatar.repository.AvatarRepository;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AvatarRepository avatarRepository;

    public UserService(@Qualifier("userRepository") UserRepository userRepository, AvatarRepository avatarRepository) {
        this.userRepository = userRepository;
        this.avatarRepository = avatarRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);
        String newUserId = generateNextUserId();

        User newUser = new User(newUserId, username, hashedPassword);
        return userRepository.save(newUser);
    }

    private String generateNextUserId() {
        Optional<String> lastUserIdOpt = userRepository.findLastUserId();

        if (lastUserIdOpt.isPresent()) {
            String lastUserId = lastUserIdOpt.get();
            int nextId = Integer.parseInt(lastUserId.substring(2)) + 1;
            return String.format("US%04d", nextId);
        } else {
            return "US0001";
        }
    }

    @Transactional
    public void updateUserAvatar(String userId, String avatarId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Avatar> avatarOpt = avatarRepository.findById(avatarId);

        if (userOpt.isPresent() && avatarOpt.isPresent()) {
            User user = userOpt.get();
            user.setAvatar(avatarOpt.get());
            userRepository.save(user);
        } else {
            throw new RuntimeException("User or Avatar not found");
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public String updateUsername(String userId, String newUsername) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUsername(newUsername);
            userRepository.save(user);
            return "Username successfully updated";
        }
        return "User not found";
    }

    public Avatar getUserAvatar(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAvatar();
    }

    @Transactional
    public String updatePassword(String userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            return "Password successfully updated";
        }
        throw new RuntimeException("User not found");
    }

}
