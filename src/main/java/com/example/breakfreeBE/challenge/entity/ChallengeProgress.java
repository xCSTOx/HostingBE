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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "progress_date")
    private Long progressDate;
}
