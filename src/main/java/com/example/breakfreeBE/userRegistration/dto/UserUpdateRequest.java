package com.example.breakfreeBE.userRegistration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @Size(min = 3, message = "Username must be at least 3 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String avatarId;
}
