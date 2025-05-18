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

    private String avatarId;
}
