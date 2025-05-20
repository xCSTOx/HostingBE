package com.example.breakfreeBE.achievement.service;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.achievement.dto.AchievementSimpleResponse;
import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import com.example.breakfreeBE.challenge.repository.ChallengeProgressRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final UserRepository userRepository;

    public List<AchievementResponse> getAllAchievements() {
        List<Achievement> achievements = achievementRepository.findAll();
        return achievements.stream()
                .map(ach -> toAchievementResponse(ach, false)) // unlocked: false karena ini global
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getAllAchievementsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getAllAchievementsForUser(user); // method ini sudah ada di service kamu
    }

    // 1. Convert Achievement entity jadi AchievementResponse dengan status unlock
    public AchievementResponse toAchievementResponse(Achievement achievement, boolean unlocked) {
        return new AchievementResponse(
                achievement.getAchievementId(),
                achievement.getAchievementName(),
                achievement.getAchievementUrl(),
                unlocked
        );
    }

    public List<AchievementSimpleResponse> toSimpleResponseList(List<AchievementResponse> unlockedList) {
        return unlockedList.stream()
                .map(unlocked -> new AchievementSimpleResponse(
                        unlocked.getAchievementId(),
                        unlocked.getAchievementName(),
                        unlocked.getAchievementUrl()
                ))
                .collect(Collectors.toList());
    }

    // 2. Cek dan unlock achievement terkait challenge, return list achievement yang baru unlocked
    public List<AchievementResponse> checkAndUnlockChallengeAchievements(User user) {
        List<AchievementResponse> unlockedAchievements = new ArrayList<>();

        // Ambil semua achievement user yang sudah dimiliki
        Set<String> ownedAchievementIds = achievementUserRepository.findByUser(user).stream()
                .map(au -> au.getAchievement().getAchievementId())
                .collect(Collectors.toSet());

        // AC0001 - Challenge Addict (selesaikan minimal 2 challenge)
        long completeCount = challengeRepository.countByUserAndStatus(user, "completed");
        if (completeCount >= 2 && !ownedAchievementIds.contains("AC0001")) {
            AchievementResponse res = unlockAchievement("AC0001", user);
            if (res != null) unlockedAchievements.add(res);
        }


        // AC0002 - First Step Taken (ikuti challenge pertama kali)
        boolean hasFirstChallenge = challengeRepository.existsByUser(user);
        if (hasFirstChallenge && !ownedAchievementIds.contains("AC0002")) {
            AchievementResponse res = unlockAchievement("AC0002", user);
            if (res != null) unlockedAchievements.add(res);
        }

        // AC0006 - Unlocked Freedom (selesai challenge 30 hari)
        List<Challenge> full30DayChallenges = challengeRepository.findByUserAndStatus(user, "completed").stream()
                .filter(ch -> ch.getChallengeData().getTotalDays() == 30)
                .toList();
        for (Challenge ch : full30DayChallenges) {
            if (isChallengeProgressComplete(ch) && !ownedAchievementIds.contains("AC0006")) {
                AchievementResponse res = unlockAchievement("AC0006", user);
                if (res != null) unlockedAchievements.add(res);
                break;
            }
        }

        // AC0008 - New Habits (ikuti 2 challenge berbeda)
        long uniqueChallengeData = challengeRepository.countDistinctChallengeDataByUser(user.getUserId());
        if (uniqueChallengeData >= 2 && !ownedAchievementIds.contains("AC0008")) {
            AchievementResponse res = unlockAchievement("AC0008", user);
            if (res != null) unlockedAchievements.add(res);
        }

        // AC0011 - Iron Will (selesai challenge pertama kali)
        boolean completedOnce = challengeRepository.existsByUserAndTimesComplete(user, 1);
        if (completedOnce && !ownedAchievementIds.contains("AC0011")) {
            AchievementResponse res = unlockAchievement("AC0011", user);
            if (res != null) unlockedAchievements.add(res);
        }

        return unlockedAchievements;
    }

    // 3. Unlock achievement dan simpan ke DB
    private AchievementResponse unlockAchievement(String achievementId, User user) {
        boolean alreadyUnlocked = achievementUserRepository.existsById(
                new AchievementUserId(user.getUserId(), achievementId)
        );
        if (alreadyUnlocked) return null;

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        AchievementUserId id = new AchievementUserId(user.getUserId(), achievementId);
        Long now = System.currentTimeMillis();
        AchievementUser achievementUser = new AchievementUser(id, achievement, user, now, now);
        achievementUserRepository.save(achievementUser);

        return toAchievementResponse(achievement, true);
    }

    // 4. Cek apakah progress sudah lengkap sesuai total hari
    private boolean isChallengeProgressComplete(Challenge challenge) {
        int progressCount = challengeProgressRepository.countByChallenge_ChallengeId(challenge.getChallengeId());
        return progressCount == challenge.getChallengeData().getTotalDays();
    }

    // Optional: method untuk ambil semua achievement user (termasuk yang belum unlocked)
    public List<AchievementResponse> getAllAchievementsForUser(User user) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        Set<String> unlockedIds = achievementUserRepository.findByUser(user).stream()
                .map(au -> au.getAchievement().getAchievementId())
                .collect(Collectors.toSet());

        return allAchievements.stream()
                .map(ach -> toAchievementResponse(ach, unlockedIds.contains(ach.getAchievementId())))
                .collect(Collectors.toList());
    }
}




