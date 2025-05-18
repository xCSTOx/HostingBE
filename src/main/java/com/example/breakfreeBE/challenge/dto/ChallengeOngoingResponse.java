package com.example.breakfreeBE.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.cache.spi.entry.StructuredCacheEntry;

import java.time.LocalDate;
import java.util.List;

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
    private List<Long> weeklyLogs;
    private boolean todayLogged;
}
