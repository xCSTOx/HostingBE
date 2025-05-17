package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {

    List<Challenge> findByUser_UserIdAndStatus(String userId, String status);

    Optional<Challenge> findByChallengeIdAndUser_UserId(String challengeId, String userId);

    Optional<Challenge> findByChallengeData_ChallengeDataIdAndUser_UserId(String challengeDataId, String userId);

    void deleteByChallengeData_ChallengeDataIdAndUser_UserId(String challengeDataId, String userId);
}
