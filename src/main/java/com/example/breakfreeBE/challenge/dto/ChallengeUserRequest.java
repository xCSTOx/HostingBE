package com.example.breakfreeBE.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChallengeUserRequest {

    @NotBlank(message = "Challenge ID must not be blank")
    private String challengeId;

    @NotBlank(message = "User ID must not be blank")
    private String userId;

    public ChallengeUserRequest(String challengeId, String userId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    public ChallengeUserRequest() {}
}
