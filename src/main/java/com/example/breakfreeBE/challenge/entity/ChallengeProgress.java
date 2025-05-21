package com.example.breakfreeBE.challenge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgress {

    @Id
    @Column(name = "progress_id", length = 6)
    private String progressId;

    @Column(name = "user_id", length = 6)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", referencedColumnName = "challenge_id", insertable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @Column(name = "progress_date", nullable = false)
    private Long progressDate;
}
