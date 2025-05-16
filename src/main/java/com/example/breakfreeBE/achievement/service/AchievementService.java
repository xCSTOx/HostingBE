package com.example.breakfreeBE.achievement.service;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementUserRepository achievementUserRepository;


    public List<AchievementResponse> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(a -> new AchievementResponse(
                        a.getAchievementId(),
                        a.getAchievementName(),
                        a.getAchievementUrl(),
                        false
                ))
                .collect(Collectors.toList());
    }

    // Menampilkan semua achievement dan tandai apakah achievement sudah didapat user
    public List<AchievementResponse> getAllAchievementsByUserId(String userId) {
        // Ambil semua achievement_id yang sudah dimiliki user
        List<String> unlockedAchievementIds = achievementUserRepository.findByIdUserId(userId).stream()
                .map(achievementUser -> achievementUser.getAchievement().getAchievementId())
                .toList();

        // Ambil semua achievement dari DB, dan tandai mana yang sudah di-unlock
        return achievementRepository.findAll().stream()
                .map(a -> new AchievementResponse(
                        a.getAchievementId(),
                        a.getAchievementName(),
                        a.getAchievementUrl(),
                        unlockedAchievementIds.contains(a.getAchievementId())
                ))
                .collect(Collectors.toList());
    }
}
