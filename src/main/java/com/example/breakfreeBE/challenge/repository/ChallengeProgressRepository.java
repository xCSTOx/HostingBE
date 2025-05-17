package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, String> {
    int countByChallenge_ChallengeId(String challengeId);
}
