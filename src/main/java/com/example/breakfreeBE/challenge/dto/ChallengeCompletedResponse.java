package com.example.breakfreeBE.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeCompletedResponse {
    private String challengeId;
    private String challengeName;
    private String challengeDesc;
    private String challengeUrl;
    private String color;
    private int totalDays;
    private long startDate;
    private int timesComplete;
}