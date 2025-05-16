package com.example.breakfreeBE.challenge.entity;

import com.example.breakfreeBE.addiction.entity.AddictionData;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "challenge_data")
@Data
public class ChallengeData {

    @Id
    @Column(name = "challenge_data_id", length = 6)
    private String challengeDataId;

    @Column(name = "addiction_id", length = 6, insertable = false, updatable = false)
    private String addictionId;

    @Column(name = "challenge_name", length = 255)
    private String challengeName;

    @Column(name = "challenge_desc", length = 255)
    private String challengeDesc;

    @Column(name = "total_days")
    private int totalDays;

    @Column(name = "color", length = 64)
    private String color;

    @Column(name = "challenge_url", length = 255)
    private String challengeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_id")
    private AddictionData addictionData;
}
