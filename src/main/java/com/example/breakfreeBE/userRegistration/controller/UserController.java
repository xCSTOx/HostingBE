package com.example.breakfreeBE.userRegistration.controller;

import com.example.breakfreeBE.avatar.dto.AvatarUpdateRequest;
import com.example.breakfreeBE.avatar.entity.Avatar;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import com.example.breakfreeBE.userRegistration.dto.UserUpdateRequest;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    public static class UserRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, message = "Username must be at least 3 characters")
        public String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String password;
    }

    public static class UserUpdate {
        public String userId;

        @NotBlank(message = "Username is required")
        @Size(min = 3, message = "Username must be at least 3 characters")
        public String username;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "User list retrieved successfully"), users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve users: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Map<String, String>>> registerUser(@RequestBody UserRequest request) {
        if (userService.existsByUsername(request.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new BaseResponse<>(new MetaResponse(false, "Username is already taken"), null)
            );
        }

        if (request.username == null || request.username.trim().isEmpty() ||
                request.password == null || request.password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new BaseResponse<>(new MetaResponse(false, "Username and password must not be empty"), null)
            );
        }

        User newUser = userService.registerUser(request.username, request.password);
        Map<String, String> responseData = new HashMap<>();
        responseData.put("userId", newUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BaseResponse<>(new MetaResponse(true, "User registered successfully"), responseData)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<Map<String, String>>> login(@RequestBody UserRequest request) {
        Optional<User> userOpt = userService.findByUsername(request.username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (BCrypt.checkpw(request.password, user.getPassword())) {
                Map<String, String> responseData = new HashMap<>();
                responseData.put("userId", user.getUserId());

                return ResponseEntity.ok(new BaseResponse<>(
                        new MetaResponse(true, "Login successful"), responseData
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new BaseResponse<>(new MetaResponse(false, "Wrong password"), null)
                );
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new BaseResponse<>(new MetaResponse(false, "User not found"), null)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<User>> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(new BaseResponse<>(
                        new MetaResponse(true, "User found"), user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new BaseResponse<>(new MetaResponse(false, "User not found"), null)));
    }

    @GetMapping("/avatar/view/{userId}")
    public ResponseEntity<BaseResponse<Avatar>> getUserAvatar(@PathVariable String userId) {
        try {
            Avatar avatar = userService.getUserAvatar(userId);

            if (avatar == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new BaseResponse<>(new MetaResponse(false, "Avatar not found"), null)
                );
            }

            return ResponseEntity.ok(new BaseResponse<>(
                    new MetaResponse(true, "Avatar retrieved successfully"), avatar
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Internal Server Error: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/avatar/add")
    public ResponseEntity<BaseResponse<String>> addAvatarToUser(
            @RequestBody AvatarUpdateRequest request
    ) {
        try {
            userService.updateUserAvatar(request.userId, request.avatarId);  // reuse method
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new BaseResponse<>(new MetaResponse(true, "Avatar added to user"), "Avatar added")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new BaseResponse<>(new MetaResponse(false, e.getMessage()), null)
            );
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        try {
            Optional<User> userOpt = userService.getUserById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new BaseResponse<>(new MetaResponse(false, "User not found"), null));
            }

            List<String> messages = new ArrayList<>();

            // Update username
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                if (userService.existsByUsername(request.getUsername())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new BaseResponse<>(new MetaResponse(false, "Username is already taken"), null));
                }
                userService.updateUsername(request.getUserId(), request.getUsername());
                messages.add("Username updated successfully");
            }

            // Update avatar
            if (request.getAvatarId() != null && !request.getAvatarId().isBlank()) {
                userService.updateUserAvatar(request.getUserId(), request.getAvatarId());
                messages.add("Avatar updated successfully");
            }

            if (messages.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new BaseResponse<>(new MetaResponse(false, "No valid fields provided for update"), null)
                );
            }

            return ResponseEntity.ok(new BaseResponse<>(
                    new MetaResponse(true, "User updated successfully"),
                    String.join("; ", messages)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(new MetaResponse(false, "Update failed: " + e.getMessage()), null));
        }
    }

}
