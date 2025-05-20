package com.example.breakfreeBE.challenge.service;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.achievement.dto.AchievementSimpleResponse;
import com.example.breakfreeBE.achievement.service.AchievementService;
import com.example.breakfreeBE.challenge.dto.*;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeData;
import com.example.breakfreeBE.challenge.entity.*;
import com.example.breakfreeBE.challenge.repository.ChallengeDataRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeProgressRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import com.example.breakfreeBE.exception.ResourceNotFoundException;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Autowired
    private AchievementService achievementService;

    public List<ChallengeData> getAllDailyChallenges() {
        return challengeDataRepository.findAll();
    }

    private boolean isToday(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        int year = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        Calendar now = Calendar.getInstance();
        return year == now.get(Calendar.YEAR) && dayOfYear == now.get(Calendar.DAY_OF_YEAR);
    }

    public List<ChallengeOngoingResponse> getOngoingChallenges(String userId) {
        List<Challenge> challenges = challengeRepository.findByUser_UserIdAndStatus(userId, "ongoing");

        return challenges.stream().map(c -> {
            List<Long> weeklyLogs = getWeeklyLogs(userId, c.getChallengeId());

            boolean todayLogged = c.getProgressList() != null &&
                    c.getProgressList().stream().anyMatch(p -> isToday(p.getProgressDate()));

            return new ChallengeOngoingResponse(
                    c.getChallengeId(),
                    c.getChallengeData().getChallengeName(),
                    c.getChallengeData().getChallengeDesc(),
                    c.getChallengeData().getColor(),
                    c.getChallengeData().getChallengeUrl(),
                    c.getStartDate(),
                    c.getChallengeData().getTotalDays(),
                    c.getTimesComplete(),
                    c.getStatus(),
                    weeklyLogs,
                    todayLogged
            );
        }).toList();
    }

    public BaseResponse<?> participateChallenge(ChallengeUserRequest request) {
        String challengeDataId = request.getChallengeDataId();
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

        // Cek dan unlock achievement yang terkait
        List<AchievementResponse> unlockedAchievements = achievementService.checkAndUnlockChallengeAchievements(user);

        MetaResponse meta;
        Object dataResponse;

        if (!unlockedAchievements.isEmpty()) {
            AchievementResponse first = unlockedAchievements.get(0);
            AchievementSimpleResponse achievementSimple = new AchievementSimpleResponse(
                    first.getAchievementId(),
                    first.getAchievementName(),
                    first.getAchievementUrl()
            );
            dataResponse = new ParticipateChallengeDataResponse(achievementSimple, challenge.getChallengeId());
            meta = new MetaResponse(true, "Challenge joined successfully and achievement earned");
        } else {
            dataResponse = "Participated successfully";
            meta = new MetaResponse(true, "Challenge participation successful");
        }

        return new BaseResponse<>(meta, dataResponse);
    }

    public BaseResponse<Map<String, Object>> updateProgress(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUser_UserId(challengeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        // Cek apakah sudah log hari ini
        boolean alreadyLoggedToday = challenge.getProgressList().stream()
                .anyMatch(p -> isToday(p.getProgressDate()));
        if (alreadyLoggedToday) {
            throw new IllegalStateException("Already logged today");
        }

        // Simpan progress baru
        ChallengeProgress progress = new ChallengeProgress();
        progress.setProgressId(UUID.randomUUID().toString().substring(0, 6));
        progress.setChallenge(challenge);
        progress.setProgressDate(System.currentTimeMillis());
        challengeProgressRepository.save(progress);

        int count = challengeProgressRepository.countByChallenge_ChallengeId(challenge.getChallengeId());
        challenge.setTimesComplete(count);

        ChallengeData data = challenge.getChallengeData();
        boolean completed = false;
        if (count >= data.getTotalDays()) {
            challenge.setStatus("completed");
            completed = true;
        }

        challengeRepository.save(challenge);

        // Cek dan ubah achievement ke format simple
        List<AchievementResponse> newAchievements = achievementService.checkAndUnlockChallengeAchievements(challenge.getUser());
        List<AchievementSimpleResponse> simpleAchievements = achievementService.toSimpleResponseList(newAchievements);

        // Siapkan response
        Map<String, Object> result = new LinkedHashMap<>();

        if (!simpleAchievements.isEmpty()) {
            result.put("Achievements", simpleAchievements);
        }

        result.put("challengeId", challenge.getChallengeId());

        // Buat message dinamis
        StringBuilder messageBuilder = new StringBuilder("Progress updated");
        if (completed) messageBuilder.append(" and challenge completed");
        if (!simpleAchievements.isEmpty()) messageBuilder.append(" – Achievement Earned!");

        return BaseResponse.success(messageBuilder.toString(), result);
    }


    @Transactional
    public void stopChallenge(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        Challenge challenge = challengeRepository.findByChallengeIdAndUser_UserId(challengeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        challengeRepository.delete(challenge); // ini menghapus dengan benar berdasarkan ID
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

    public List<Long> getWeeklyLogs(String userId, String challengeId) {
        long now = System.currentTimeMillis();
        long millisPerDay = 24 * 60 * 60 * 1000;

        // Hitung waktu mulai dari 7 hari terakhir (termasuk hari ini)
        long startOfToday = now - (now % millisPerDay); // reset ke pukul 00:00 hari ini
        long startWindow = startOfToday - (millisPerDay * 6); // 6 hari ke belakang + hari ini = 7 hari

        // Ambil progress dari 7 hari terakhir
        List<ChallengeProgress> logs = challengeProgressRepository.findLogsInRange(challengeId, startWindow, now);

        // Map: index 0–6 → progress timestamp atau null
        Map<Integer, Long> dailyLogMap = new HashMap<>();
        for (ChallengeProgress log : logs) {
            long progressDate = log.getProgressDate();
            int dayIndex = (int) ((progressDate - startWindow) / millisPerDay);
            dailyLogMap.putIfAbsent(dayIndex, progressDate); // 1 log per hari
        }

        // Hasil akhir: urut dari 6 hari lalu → hari ini
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(dailyLogMap.getOrDefault(i, null));
        }

        return result;
    }
}
