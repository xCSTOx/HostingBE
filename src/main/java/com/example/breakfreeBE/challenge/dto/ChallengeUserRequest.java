package com.example.breakfreeBE.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChallengeUserRequest {

    private String challengeId;

    private String challengeDataId;

    @NotBlank(message = "User ID must not be blank")
    private String userId;

    public ChallengeUserRequest() {}

    public ChallengeUserRequest(String challengeId, String challengeDataId, String userId) {
        this.challengeId = challengeId;
        this.challengeDataId = challengeDataId;
        this.userId = userId;
    }
}
