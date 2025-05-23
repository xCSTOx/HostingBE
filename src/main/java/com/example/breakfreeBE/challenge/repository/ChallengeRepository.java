package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.userRegistration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {

    List<Challenge> findByUser_UserIdAndStatus(String userId, String status);

    Optional<Challenge> findByChallengeIdAndUser_UserId(String challengeId, String userId);

    Optional<Challenge> findByChallengeData_ChallengeDataIdAndUser_UserId(String challengeDataId, String userId);

    @Query("SELECT COUNT(DISTINCT c.challengeData.challengeDataId) FROM Challenge c WHERE c.user.userId = :userId")
    long countDistinctChallengeDataByUser(@Param("userId") String userId);

    long countByUserAndStatus(User user, String status);

    boolean existsByUser(User user);

    List<Challenge> findByUserAndStatus(User user, String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Challenge c WHERE c.challengeId = :challengeId AND c.user.userId = :userId")
    void deleteByChallengeIdAndUserId(@Param("challengeId") String challengeId, @Param("userId") String userId);

    List<Challenge> findByUser_UserId(String userId);

    boolean existsByChallengeIdAndUserId(String challengeId, String userId);

    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.user.userId = :userId AND c.challengeData.challengeDataId = :challengeDataId AND c.status = 'completed'")
    int countCompletedByUserAndChallenge(@Param("userId") String userId, @Param("challengeDataId") String challengeDataId);

    List<Challenge> findByUser_UserIdAndChallengeData_ChallengeDataIdAndStatus(String userId, String challengeDataId, String status);



}

