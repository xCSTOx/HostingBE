package com.example.breakfreeBE.challenge.service;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.achievement.dto.AchievementSimpleResponse;
import com.example.breakfreeBE.achievement.service.AchievementService;
import com.example.breakfreeBE.addiction.entity.Addiction;
import com.example.breakfreeBE.challenge.dto.*;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeData;
import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import com.example.breakfreeBE.challenge.repository.ChallengeDataRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeProgressRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.community.repository.PostRepository;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import com.example.breakfreeBE.exception.ResourceNotFoundException;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private UserRepository userRepository; // Ditambahkan dari file pertama

    @Autowired
    private AchievementService achievementService; // Ditambahkan dari file pertama

    @Autowired
    private PostRepository postRepository;

    public List<DailyChallengeResponse> getAllDailyChallenges(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Ambil semua addictionId user
        List<String> addictionIds = user.getAddictions()
                .stream()
                .map(Addiction::getAddictionId)
                .collect(Collectors.toList());

        // Jika user tidak punya addiction, return list kosong
        if (addictionIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Ambil semua challenges yang terkait dengan addictionId user (multiple addiction)
        List<ChallengeData> allChallengesByAddiction = challengeDataRepository.findByAddictionData_AddictionIdIn(addictionIds);

        // Ambil list challengeDataId yang sudah diikuti user
        List<String> joinedChallengeDataIds = challengeRepository.findByUser_UserId(userId)
                .stream()
                .map(challenge -> challenge.getChallengeData().getChallengeDataId())
                .collect(Collectors.toList());

        // Filter challenge yang belum diikuti user dan mapping ke response
        return allChallengesByAddiction.stream()
                .filter(cd -> !joinedChallengeDataIds.contains(cd.getChallengeDataId()))
                .map(cd -> new DailyChallengeResponse(
                        cd.getChallengeDataId(),
                        cd.getChallengeName(),
                        cd.getChallengeDesc(),
                        cd.getTotalDays(),
                        cd.getColor(),
                        cd.getChallengeUrl()
                ))
                .collect(Collectors.toList());
    }

    // --- Menggabungkan getOngoingChallenges ---
    // Versi file pertama return DTO ChallengeOngoingResponse, versi kedua return entity Challenge
    // Dipilih versi pertama yang lebih lengkap (return DTO)
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
                    weeklyLogs,
                    todayLogged
            );
        }).toList();
    }

    // --- participateChallenge ---
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
        challenge.setUserId(userId);
        challenge.setUser(user);
        challenge.setChallengeDataId(challengeDataId);
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
                    first.getAchievementDesc(),
                    first.getAchievementId(),
                    first.getAchievementName(),
                    first.getAchievementUrl()
            );
            dataResponse = new ParticipateChallengeDataResponse(achievementSimple);
            meta = new MetaResponse(true, "Challenge joined successfully and achievement earned");
        } else {
            dataResponse = null;
            meta = new MetaResponse(true, "Challenge participation successful");
        }

        return new BaseResponse<>(meta, dataResponse);
    }

    // --- updateProgress ---
    // Menggunakan versi file pertama yang lebih lengkap dengan validasi, response achievement, dan status challenge
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
        progress.setChallengeId(challenge.getChallengeId());
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
            result.put("achievement", simpleAchievements.get(0));
        }

        result.put("completed", completed);

        // Buat message dinamis
        StringBuilder messageBuilder = new StringBuilder("Progress updated");
        if (completed) messageBuilder.append(" and challenge completed");
        if (!simpleAchievements.isEmpty()) messageBuilder.append(" – Achievement Earned!");

        return BaseResponse.success(messageBuilder.toString(), result);
    }

    // --- stopChallenge ---
    // Gabungkan cara file kedua yang lebih lengkap dengan delete progress dulu
    @Transactional
    public void stopChallenge(ChallengeUserRequest request) {
        String challengeId = request.getChallengeId();
        String userId = request.getUserId();

        boolean exists = challengeRepository.existsByChallengeIdAndUserId(challengeId, userId);
        if (!exists) throw new EntityNotFoundException("Challenge not found");
        // Perbaikan delete progress
        challengeProgressRepository.deleteByChallengeId(challengeId);

        postRepository.deleteByChallengeId(challengeId);

        challengeRepository.deleteByChallengeIdAndUserId(challengeId, userId);
    }


    // --- getCompletedChallenges ---
    // Pilih versi pertama yang return DTO response yang lebih kaya
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

    // --- getWeeklyLogs ---
    // Metode tambahan dari file pertama
    public List<Long> getWeeklyLogs(String userId, String challengeId) {
        long now = System.currentTimeMillis();
        long millisPerDay = 24 * 60 * 60 * 1000;

        long startOfToday = now - (now % millisPerDay);
        long startWindow = startOfToday - (millisPerDay * 6);

        List<ChallengeProgress> logs = challengeProgressRepository.findLogsInRange(challengeId, startWindow, now);

        Map<Integer, Long> dailyLogMap = new HashMap<>();
        for (ChallengeProgress log : logs) {
            long progressDate = log.getProgressDate();
            int dayIndex = (int) ((progressDate - startWindow) / millisPerDay);
            dailyLogMap.putIfAbsent(dayIndex, progressDate);
        }

        List<Long> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(dailyLogMap.getOrDefault(i, null));
        }

        return result;
    }

    // --- Helper method isToday dari file pertama ---
    private boolean isToday(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        int year = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        Calendar now = Calendar.getInstance();
        return year == now.get(Calendar.YEAR) && dayOfYear == now.get(Calendar.DAY_OF_YEAR);
    }
}
