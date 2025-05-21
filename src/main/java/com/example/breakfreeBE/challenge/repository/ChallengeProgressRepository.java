package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, String> {
    int countByChallenge_ChallengeId(String challengeId);

    @Modifying
    @Query("DELETE FROM Challenge c WHERE c.challengeId = :challengeId AND c.user.userId = :userId")
    void deleteByChallengeIdAndUserId(@Param("challengeId") String challengeId, @Param("userId") String userId);


    @Query("SELECT cp FROM ChallengeProgress cp WHERE cp.challenge.challengeId = :challengeId AND cp.progressDate BETWEEN :start AND :end")
    List<ChallengeProgress> findLogsInRange(
            @Param("challengeId") String challengeId,
            @Param("start") Long start,
            @Param("end") Long end);

}
