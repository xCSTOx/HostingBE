package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, String> {

    int countByChallengeIdAndUserId(String challengeId, String userId);

    void deleteByChallengeIdAndUserId(String challengeId, String userId);
}
