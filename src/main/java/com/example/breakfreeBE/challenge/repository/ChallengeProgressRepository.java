package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, String> {
    int countByChallenge_ChallengeId(String challengeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChallengeProgress cp WHERE cp.challengeId = :challengeId")
    void deleteByChallengeId(@Param("challengeId") String challengeId);

    @Query("SELECT cp FROM ChallengeProgress cp WHERE cp.challenge.challengeId = :challengeId AND cp.progressDate BETWEEN :start AND :end")
    List<ChallengeProgress> findLogsInRange(
            @Param("challengeId") String challengeId,
            @Param("start") Long start,
            @Param("end") Long end);

}
