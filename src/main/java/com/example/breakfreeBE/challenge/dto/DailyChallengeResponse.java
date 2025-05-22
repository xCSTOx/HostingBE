package com.example.breakfreeBE.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyChallengeResponse {
    private String challengeId;
    private String challengeName;
    private String challengeDesc;
    private Integer totalDays;
    private String color;
    private String challengeUrl;
}
