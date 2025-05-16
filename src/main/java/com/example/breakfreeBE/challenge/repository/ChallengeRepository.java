package com.example.breakfreeBE.challenge.repository;

import com.example.breakfreeBE.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, String> {

    Optional<Challenge> findByChallengeIdAndUserId(String challengeId, String userId);

    List<Challenge> findByUserIdAndStatus(String userId, String status);

    void deleteByChallengeIdAndUserId(String challengeId, String userId);
}
