package com.example.breakfreeBE.achievement.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUserId implements Serializable {
    private String achievementId;
    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AchievementUserId)) return false;
        AchievementUserId that = (AchievementUserId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(achievementId, that.achievementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, achievementId);
    }
}
