package com.example.breakfreeBE.achievement.entity;

import com.example.breakfreeBE.userRegistration.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievement_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUser {

    @EmbeddedId
    private AchievementUserId id;

    @ManyToOne
    @MapsId("achievementId")
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "achievement_date")
    private Long achievementDate;

    @Column(name = "achieved_at")
    private Long achievedAt;

}
