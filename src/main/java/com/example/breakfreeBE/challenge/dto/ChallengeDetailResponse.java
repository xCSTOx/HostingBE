package com.example.breakfreeBE.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChallengeDetailResponse {
    private String challengeId;
    private String userId;
    private String challengeName;
    private String challengeDesc;
    private int totalDays;
    private String color;
    private String challengeUrl;
    private Long startDate;
    private int timesComplete;
    private String status;
    private String addictionName;
}
