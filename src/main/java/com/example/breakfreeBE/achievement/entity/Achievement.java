package com.example.breakfreeBE.achievement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @Column(name = "achievement_id")
    private String achievementId;

    @Column(name = "achievement_name", nullable = false)
    private String achievementName;

    @Column(name = "achievement_url")
    private String achievementUrl;

    @Column(name= "achievement_desc")
    private String achievementDesc;
}
