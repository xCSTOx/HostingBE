package com.example.breakfreeBE.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChallengeOngoingResponse {
    private String challengeId;
    private String challengeName;
    private String challengeDesc;
    private String color;
    private String challengeUrl;
    private Long startDate;
    private int totalDays;
    private int timesComplete;
    private String status;
}
