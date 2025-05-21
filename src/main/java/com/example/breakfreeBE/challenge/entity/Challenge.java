package com.example.breakfreeBE.challenge.entity;

import com.example.breakfreeBE.userRegistration.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "challenge")
@Data
public class Challenge {

    @Id
    @Column(name = "challenge_id", length = 6)
    private String challengeId;

    @Column(name = "user_id", length = 6, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private User user;

    @Column(name = "challenge_data_id", length = 6, nullable = false)
    private String challengeDataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_data_id", referencedColumnName = "challenge_data_id", insertable = false, updatable = false)
    private ChallengeData challengeData;

    @Column(name = "start_date", nullable = false)
    private Long startDate;

    @Column(name = "times_complete", nullable = false)
    private int timesComplete;

    @Column(name = "status", length = 255, nullable = false)
    private String status;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeProgress> progressList;
}
