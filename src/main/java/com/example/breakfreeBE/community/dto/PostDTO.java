package com.example.breakfreeBE.community.dto;

import lombok.Data;

@Data
public class PostDTO {
    private String postId;
    private String postText;
    private Long postDate;
    private boolean isBookmarked;
    private ProfileDTO profile;
    private StreakDataDTO streakData;
    private AchievementDataDTO achievementData;
    private ChallengeDataDTO challengeData;

    @Data
    public static class ProfileDTO {
        private String userId;
        private String username;
        private String avatarUrl;
    }

    @Data
    public static class StreakDataDTO {
        private String addictionId;
        private String addictionName;
        private long streakDays;
    }

    @Data
    public static class AchievementDataDTO {
        private String achievementId;
        private String achievementName;
        private String achievementUrl;
    }

    @Data
    public static class ChallengeDataDTO {
        private String challengeId;
        private String challengeName;
        private String challengeUrl;
        private String color;
    }
}