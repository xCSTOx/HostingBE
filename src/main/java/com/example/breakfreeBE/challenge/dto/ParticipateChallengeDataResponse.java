package com.example.breakfreeBE.challenge.dto;

import com.example.breakfreeBE.achievement.dto.AchievementSimpleResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipateChallengeDataResponse {
    private AchievementSimpleResponse achievement;// bisa null

    public ParticipateChallengeDataResponse(AchievementSimpleResponse achievement) {
        this.achievement = achievement;
    }

    private String challengeId;
}
