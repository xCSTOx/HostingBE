package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.userRegistration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    boolean existsByUserAndStatus(User user, String status);

    boolean existsByUserAndTimesComplete(User user, int timesComplete);
}
