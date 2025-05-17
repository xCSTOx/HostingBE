package com.example.breakfreeBE.challenge.entity;

import com.example.breakfreeBE.userRegistration.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

    @Id
    @Column(name = "challenge_id", length = 6)
    private String challengeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenge_data_id", nullable = false)
    private ChallengeData challengeData;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "times_complete")
    private Integer timesComplete;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeProgress> progressList = new ArrayList<>();
}
