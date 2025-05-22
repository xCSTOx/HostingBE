package com.example.breakfreeBE.userRegistration.service;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.avatar.entity.Avatar;
import com.example.breakfreeBE.avatar.repository.AvatarRepository;
import com.example.breakfreeBE.challenge.dto.ChallengeCompletedResponse;
import com.example.breakfreeBE.challenge.entity.ChallengeProgress;
import com.example.breakfreeBE.userRegistration.dto.UserProfileResponse;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.userRegistration.dto.UserUpdateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AvatarRepository avatarRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final AchievementRepository achievementRepository;

    public UserService(@Qualifier("userRepository") UserRepository userRepository,
                       AvatarRepository avatarRepository,
                       AchievementUserRepository achievementUserRepository,
                       AchievementRepository achievementRepository) {
        this.userRepository = userRepository;
        this.avatarRepository = avatarRepository;
        this.achievementUserRepository = achievementUserRepository;
        this.achievementRepository = achievementRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);
        String newUserId = generateNextUserId();

        Avatar defaultAvatar = avatarRepository.findById("AR0001")
                .orElseThrow(() -> new RuntimeException("Default avatar not found"));

        User newUser = new User(newUserId, username, hashedPassword);
        newUser.setAvatar(defaultAvatar);

        return userRepository.save(newUser);
    }

    private String generateNextUserId() {
        Optional<String> lastUserIdOpt = userRepository.findLastUserId();

        if (lastUserIdOpt.isPresent()) {
            String lastUserId = lastUserIdOpt.get();
            int nextId = Integer.parseInt(lastUserId.substring(2)) + 1;
            return String.format("US%04d", nextId);
        } else {
            return "US0001";
        }
    }

    @Transactional
    public List<AchievementResponse> updateUserAvatar(String userId, String avatarId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Avatar> avatarOpt = avatarRepository.findById(avatarId);

        if (userOpt.isPresent() && avatarOpt.isPresent()) {
            User user = userOpt.get();
            user.setAvatar(avatarOpt.get());

            List<AchievementResponse> unlocked = unlockAchievementIfFirstProfileUpdate(user);
            userRepository.save(user);
            return unlocked;
        }
        return List.of();
    }

    @Transactional
    public List<AchievementResponse> updateUsername(String userId, String newUsername) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUsername(newUsername);

            List<AchievementResponse> unlocked = unlockAchievementIfFirstProfileUpdate(user);
            userRepository.save(user);
            return unlocked;
        }
        return List.of();
    }

    @Transactional
    public BaseResponse<Object> updateProfile (UserUpdateRequest request) {
        Optional<User> optionalUser = userRepository.findById(request.getUserId());
        Optional<Avatar> optionalAvatar = avatarRepository.findById(request.getAvatarId());

        if (optionalUser.isEmpty() || optionalAvatar.isEmpty()) {
            return BaseResponse.error("Update failed: User or Avatar not found");
        }

        User user = optionalUser.get();
        Avatar avatar = optionalAvatar.get();

        // Cek apakah username sudah dipakai oleh user lain
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            return BaseResponse.error("Username is already taken");
        }

        StringBuilder updateMessages = new StringBuilder();

        // Update username jika berbeda
        if (!user.getUsername().equals(request.getUsername())) {
            user.setUsername(request.getUsername());
            updateMessages.append("Username updated successfully");
        }

        // Update avatar jika berbeda
        if (!user.getAvatar().getAvatarId().equals(avatar.getAvatarId())) {
            user.setAvatar(avatar);
            if (updateMessages.length() > 0) updateMessages.append("; ");
            updateMessages.append("Avatar updated successfully");
        }

        userRepository.save(user);

        // Cek apakah user baru pertama kali update profil dan dapat achievement AC0014
        List<AchievementResponse> unlocked = unlockAchievementIfFirstProfileUpdate(user);

        if (!unlocked.isEmpty()) {
            AchievementResponse unlockedAchievement = unlocked.get(0); // diasumsikan hanya 1 achievement
            Map<String, Object> achievementMap = new LinkedHashMap<>();
            achievementMap.put("achievementDesc", unlockedAchievement.getAchievementDesc());
            achievementMap.put("achievementId", unlockedAchievement.getAchievementId());
            achievementMap.put("achievementName", unlockedAchievement.getAchievementName());
            achievementMap.put("achievementUrl", unlockedAchievement.getAchievementUrl());

            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("achievement", achievementMap);

            return BaseResponse.success("User updated successfully; Achievement Earned!", responseData);
        }

        return BaseResponse.success("User updated successfully", null);
    }

    private List<AchievementResponse> unlockAchievementIfFirstProfileUpdate(User user) {
        String userId = user.getUserId();
        String achievementId = "AC0014";

        boolean alreadyUnlocked = achievementUserRepository.existsByIdUserIdAndIdAchievementId(userId, achievementId);
        if (alreadyUnlocked) return List.of();

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found: " + achievementId));

        AchievementUser achievementUser = new AchievementUser();
        AchievementUserId id = new AchievementUserId(achievementId, userId);
        achievementUser.setId(id);
        achievementUser.setAchievement(achievement);
        achievementUser.setUser(user);

        long now = System.currentTimeMillis();
        achievementUser.setAchievementDate(now);
        achievementUser.setAchievedAt(now);

        achievementUserRepository.save(achievementUser);

        AchievementResponse response = new AchievementResponse(
                achievement.getAchievementDesc(),
                achievement.getAchievementId(),
                achievement.getAchievementName(),
                achievement.getAchievementUrl(),
                true
        );

        return List.of(response);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Avatar getUserAvatar(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAvatar();
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String avatarUrl = user.getAvatar() != null ? user.getAvatar().getAvatarUrl() : null;

        // Longest streak calculation
        int longestStreak = user.getChallenges().stream()
                .filter(c -> c.getProgressList() != null)
                .mapToInt(c -> calculateLongestStreak(c.getProgressList()))
                .max().orElse(0);

        // 3 latest completed challenges
        List<ChallengeCompletedResponse> completedChallenges = user.getChallenges().stream()
                .filter(c -> "completed".equalsIgnoreCase(c.getStatus()))
                .sorted((a, b) -> Long.compare(b.getStartDate(), a.getStartDate()))
                .limit(3)
                .map(c -> new ChallengeCompletedResponse(
                        c.getChallengeId(),
                        c.getChallengeData().getChallengeName(),
                        c.getChallengeData().getChallengeDesc(),
                        c.getChallengeData().getChallengeUrl(),
                        c.getChallengeData().getColor(),
                        c.getChallengeData().getTotalDays(),
                        c.getStartDate(),
                        c.getTimesComplete()
                )).toList();

        // 3 latest achievements
        List<AchievementResponse> latestAchievements = user.getAchievements().stream()
                .sorted((a, b) -> b.getAchievementDate().compareTo(a.getAchievementDate()))
                .limit(3)
                .map(a -> new AchievementResponse(
                        a.getAchievement().getAchievementDesc(),
                        a.getAchievement().getAchievementId(),
                        a.getAchievement().getAchievementName(),
                        a.getAchievement().getAchievementUrl(),
                        true
                )).toList();

        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                avatarUrl,
                latestAchievements,
                completedChallenges
        );
    }

    private int calculateLongestStreak(List<ChallengeProgress> progresses) {
        if (progresses == null || progresses.isEmpty()) return 0;

        List<Long> sortedDates = progresses.stream()
                .map(ChallengeProgress::getProgressDate)
                .distinct()
                .sorted()
                .toList();

        int longest = 1;
        int current = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            long diffDays = (sortedDates.get(i) - sortedDates.get(i - 1)) / 86400000;
            if (diffDays == 1) {
                current++;
            } else {
                longest = Math.max(longest, current);
                current = 1;
            }
        }
        return Math.max(longest, current);
    }

    public List<AchievementResponse> getUnlockedAchievements(String userId) {
        List<AchievementUser> unlocked = achievementUserRepository.findByIdUserId(userId);

        if (unlocked.isEmpty()) {
            throw new RuntimeException("No unlocked achievements found for user " + userId);
        }

        return unlocked.stream()
                .map(au -> {
                    Achievement a = au.getAchievement();
                    return new AchievementResponse(
                            a.getAchievementDesc(),
                            a.getAchievementId(),
                            a.getAchievementName(),
                            a.getAchievementUrl(),
                            true
                    );
                })
                .collect(Collectors.toList());
    }
}
