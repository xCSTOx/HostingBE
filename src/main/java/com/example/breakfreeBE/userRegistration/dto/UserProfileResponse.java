package com.example.breakfreeBE.userRegistration.dto;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.challenge.dto.ChallengeCompletedResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String username;
    private String avatarUrl;
    private List<AchievementResponse> latestAchievements;
    private List<ChallengeCompletedResponse> latestCompletedChallenges;
}