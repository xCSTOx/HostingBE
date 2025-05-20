package com.example.breakfreeBE.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementSimpleResponse {
    private String achievementId;
    private String achievementName;
    private String achievementUrl;
}
