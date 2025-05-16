package com.example.breakfreeBE.challenge.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "challenge_progress")
@Data
public class ChallengeProgress {

    @Id
    @Column(name = "progress_id", length = 6)
    private String progressId;

    @Column(name = "challenge_id", length = 6)
    private String challengeId;

    @Column(name = "user_id", length = 6)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", insertable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "progress_date", nullable = false)
    private Long progressDate;
}
