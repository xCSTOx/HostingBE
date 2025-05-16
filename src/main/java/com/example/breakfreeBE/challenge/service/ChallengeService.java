package com.example.breakfreeBE.challenge.service;

import com.example.breakfreeBE.challenge.dto.ChallengeDetailResponse;
import com.example.breakfreeBE.challenge.dto.ChallengeUserRequest;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeData;
import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import com.example.breakfreeBE.challenge.repository.ChallengeDataRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeProgressRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeDataRepository challengeDataRepository;

    @Autowired
    private ChallengeProgressRepository challengeProgressRepository;

    public List<ChallengeData> getAllDailyChallenges() {
        return challengeDataRepository.findAll();
    }

    public List<Challenge> getOngoingChallenges(String userId) {
        return challengeRepository.findByUserIdAndStatus(userId, "ongoing");
    }

    public void participateChallenge(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        ChallengeData data = challengeDataRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge data not found"));

        boolean alreadyJoined = challengeRepository.findByChallengeIdAndUserId(challengeId, userId).isPresent();
        if (alreadyJoined) {
            throw new IllegalStateException("User already joined this challenge");
        }

        Challenge challenge = new Challenge();
        challenge.setChallengeId(UUID.randomUUID().toString().substring(0,6));
        challenge.setUserId(userId);
        challenge.setChallengeDataId(data.getChallengeDataId());
        challenge.setStatus("ongoing");
        challenge.setStartDate(System.currentTimeMillis());
        challenge.setTimesComplete(0);
        challengeRepository.save(challenge);
    }

    @Transactional
    public void stopChallenge(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        challengeProgressRepository.deleteByChallengeIdAndUserId(challengeId, userId);
        challengeRepository.deleteByChallengeIdAndUserId(challengeId, userId);
    }

    public void updateProgress(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        ChallengeProgress progress = new ChallengeProgress();
        progress.setProgressId(UUID.randomUUID().toString().substring(0,6)); // Generate 6 char ID
        progress.setChallengeId(challengeId);
        progress.setUserId(userId);
        progress.setProgressDate(System.currentTimeMillis());
        challengeProgressRepository.save(progress);

        int count = challengeProgressRepository.countByChallengeIdAndUserId(challengeId, userId);
        challenge.setTimesComplete(count);

        ChallengeData data = challengeDataRepository.findById(challenge.getChallengeDataId())
                .orElseThrow(() -> new ResourceNotFoundException("Challenge data not found"));

        if (count >= data.getTotalDays()) {
            challenge.setStatus("completed");
        }

        challengeRepository.save(challenge);
    }

    public List<Challenge> getCompletedChallenges(String userId) {
        return challengeRepository.findByUserIdAndStatus(userId, "completed");
    }

    public ChallengeDetailResponse getChallengeDetail(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        ChallengeData data = challengeDataRepository.findById(challenge.getChallengeDataId())
                .orElseThrow(() -> new ResourceNotFoundException("Challenge data not found"));

        return new ChallengeDetailResponse(
                challenge.getChallengeId(),
                challenge.getUserId(),
                data.getChallengeName(),
                data.getChallengeDesc(),
                data.getTotalDays(),
                data.getColor(),
                data.getChallengeUrl(),
                challenge.getStartDate(),
                challenge.getTimesComplete(),
                challenge.getStatus(),
                data.getAddictionData().getAddictionName()
        );
    }
}
