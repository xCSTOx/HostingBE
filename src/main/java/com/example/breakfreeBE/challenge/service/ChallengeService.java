package com.example.breakfreeBE.challenge.service;

import com.example.breakfreeBE.challenge.dto.ChallengeCompletedResponse;
import com.example.breakfreeBE.challenge.dto.ChallengeDetailResponse;
import com.example.breakfreeBE.challenge.dto.ChallengeOngoingResponse;
import com.example.breakfreeBE.challenge.dto.ChallengeUserRequest;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeData;
import com.example.breakfreeBE.challenge.entity.*;
import com.example.breakfreeBE.challenge.repository.ChallengeDataRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeProgressRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.exception.ResourceNotFoundException;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeDataRepository challengeDataRepository;

    @Autowired
    private ChallengeProgressRepository challengeProgressRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ChallengeData> getAllDailyChallenges() {
        return challengeDataRepository.findAll();
    }

    public List<ChallengeOngoingResponse> getOngoingChallenges(String userId) {
        List<Challenge> challenges = challengeRepository.findByUser_UserIdAndStatus(userId, "ongoing");

        return challenges.stream().map(c -> {
            return new ChallengeOngoingResponse(
                    c.getChallengeId(),
                    c.getChallengeData().getChallengeName(),
                    c.getChallengeData().getChallengeDesc(),
                    c.getChallengeData().getColor(),
                    c.getChallengeData().getChallengeUrl(),
                    c.getStartDate(),
                    c.getChallengeData().getTotalDays(),
                    c.getTimesComplete(),
                    c.getStatus()
            );
        }).toList();
    }


    public void participateChallenge(ChallengeUserRequest request) {
        String challengeDataId = request.getChallengeDataId();  // gunakan ini untuk ambil ChallengeData
        String userId = request.getUserId();

        ChallengeData challengeData = challengeDataRepository.findById(challengeDataId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge data not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean alreadyJoined = challengeRepository.findByChallengeData_ChallengeDataIdAndUser_UserId(challengeDataId, userId).isPresent();
        if (alreadyJoined) {
            throw new IllegalStateException("User already joined this challenge");
        }

        Challenge challenge = new Challenge();
        challenge.setChallengeId(UUID.randomUUID().toString().substring(0, 6)); // sesuai ERD
        challenge.setUser(user);
        challenge.setChallengeData(challengeData);
        challenge.setStatus("ongoing");
        challenge.setStartDate(System.currentTimeMillis());
        challenge.setTimesComplete(0);
        challengeRepository.save(challenge);
    }


    @Transactional
    public void stopChallenge(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUser_UserId(challengeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        challengeRepository.delete(challenge); // ini menghapus dengan benar berdasarkan ID
    }

    public void updateProgress(ChallengeUserRequest request) {
        String challengeDataId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUser_UserId(request.getChallengeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        ChallengeProgress progress = new ChallengeProgress();
        progress.setProgressId(UUID.randomUUID().toString().substring(0, 6));
        progress.setChallenge(challenge);
        progress.setProgressDate(System.currentTimeMillis());
        challengeProgressRepository.save(progress);

        int count = challengeProgressRepository.countByChallenge_ChallengeId(challenge.getChallengeId());
        challenge.setTimesComplete(count);

        ChallengeData data = challenge.getChallengeData();

        if (count >= data.getTotalDays()) {
            challenge.setStatus("completed");
        }

        challengeRepository.save(challenge);
    }

    public List<ChallengeCompletedResponse> getCompletedChallenges(String userId) {
        List<Challenge> challenges = challengeRepository.findByUser_UserIdAndStatus(userId, "completed");

        return challenges.stream().map(ch -> {
            ChallengeData data = ch.getChallengeData();
            return new ChallengeCompletedResponse(
                    ch.getChallengeId(),
                    data.getChallengeName(),
                    data.getChallengeDesc(),
                    data.getChallengeUrl(),
                    data.getColor(),
                    data.getTotalDays(),
                    ch.getStartDate(),
                    ch.getTimesComplete()
            );
        }).collect(Collectors.toList());
    }


    public ChallengeDetailResponse getChallengeDetail(ChallengeUserRequest request) {
        Challenge challenge = challengeRepository.findByChallengeIdAndUser_UserId(
                request.getChallengeId(),
                request.getUserId()
        ).orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        ChallengeData data = challenge.getChallengeData();

        return new ChallengeDetailResponse(
                challenge.getChallengeId(),
                request.getUserId(),
                data.getChallengeName(),
                data.getChallengeDesc(),
                data.getTotalDays(),
                data.getColor(),
                data.getChallengeUrl(),
                challenge.getStartDate(),
                challenge.getTimesComplete(),
                challenge.getStatus()
        );
    }

}
