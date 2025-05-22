package com.example.breakfreeBE.addiction.service;

import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.addiction.dto.AddictionDTO;
import com.example.breakfreeBE.addiction.entity.Addiction;
import com.example.breakfreeBE.addiction.entity.AddictionId;
import com.example.breakfreeBE.addiction.repository.AddictionRepository;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AddictionService {

    private AddictionRepository addictionRepository;
    private UserRepository userRepository;
    private AchievementUserRepository achievementUserRepository;
    private AchievementRepository achievementRepository;

    @Autowired
    public AddictionService(AddictionRepository addictionRepository, AchievementRepository achievementRepository, AchievementUserRepository achievementUserRepository, UserRepository userRepository) {
        this.addictionRepository = addictionRepository;
        this.achievementRepository = achievementRepository;
        this.achievementUserRepository = achievementUserRepository;
        this.userRepository = userRepository;
    }

    private AddictionDTO convertToDTO(Addiction addiction) {
        AddictionDTO dto = new AddictionDTO();
        dto.setUserId(addiction.getUserId());
        dto.setAddictionId(addiction.getAddictionId());
        dto.setSaver(addiction.getSaver());
        dto.setStreaks(addiction.getStreaks());
        dto.setMotivation(addiction.getMotivation());
        dto.setStartDate(addiction.getStartDate());

        if (addiction.getAddictionData() != null) {
            dto.setAddictionName(addiction.getAddictionData().getAddictionName()); // misal field `name` di AddictionData
        }

        return dto;
    }

    public static class StreakFunFact {
        public String fact;
        public String imageUrl;
        public String bgColor;

        public StreakFunFact(String fact, String imageUrl, String bgColor) {
            this.fact = fact;
            this.imageUrl = imageUrl;
            this.bgColor = bgColor;
        }
    }

    public static StreakFunFact getStreakFunFact(long streak) {
        if (streak >= 1095) {
            return new StreakFunFact(
                    "In 3 years, you breathe about 31 million times, but how many times do you truly notice it?",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/threeyears.png", "#f5e98c");
        } else if (streak >= 730) {
            return new StreakFunFact(
                    "In 2 years, you spend 730 hours in the bathroom, enough time to master a musical instrument!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/twoyears.png", "#4B7BE5");
        } else if (streak >= 365) {
            return new StreakFunFact(
                    "In 1 year, you spend 1,460 hours (60 full days) just scrolling through social media!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/oneyear.png", "#FF6B6B");
        } else if (streak >= 90) {
            return new StreakFunFact(
                    "In 3 months, you look at your phone an average of 14,000 times. Imagine if each glance was a new idea!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/threemonths.png", "#c1e8e6");
        } else if (streak >= 60) {
            return new StreakFunFact(
                    "In 2 months, your hair grows about 2.5 cm, silent proof that change is always happening!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/twomonths.png", "#d5cd2e");
        } else if (streak >= 30) {
            return new StreakFunFact(
                    "In 1 month, you spend an average of 720 hours, but only 26 hours are truly productive!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/onemonth.png", "#ecf547");
        } else if (streak >= 14) {
            return new StreakFunFact(
                    "In 2 weeks, your skin cells completely renew themselves, giving you a chance to start fresh!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/twoweeks.png", "#e86666");
        } else if (streak >= 7) {
            return new StreakFunFact(
                    "In 1 week, you swallow about 7 liters of saliva, enough to fill a large sports drink bottle!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/oneweek.png", "#A29BFE");
        } else if (streak >= 5) {
            return new StreakFunFact(
                    "In 5 days, you spend an average of 5 hours looking for misplaced items, time that could be used to learn something new!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/fivedays.png", "#E17055");
        } else if (streak >= 3) {
            return new StreakFunFact(
                    "In 3 days, your heart beats about 312,000 times, working tirelessly for you!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/threedays.png", "#F8C291");
        } else if (streak >= 1) {
            return new StreakFunFact(
                    "In 1 day, your heart pumps blood for 19,000 km, equivalent to half the Earth's circumference!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/oneday.png", "#00B894");
        } else {
            return new StreakFunFact(
                    "Today alone, you will blink about 15,000 times without even realizing it!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactstreak/today.png", "#bbedaf");
        }
    }

    public StreakFunFact getSavingFunFact(String userId) {
        List<AddictionDTO> addictions = getAddictionsByUser(userId);
        if (addictions.isEmpty()) {
            return null;
        }
        long totalSaved = 0;

        for (AddictionDTO addiction : addictions) {
            if (addiction.getStartDate() != null && addiction.getSaver() != null) {
                LocalDate startDate = Instant.ofEpochMilli(addiction.getStartDate())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                long daysPassed = ChronoUnit.DAYS.between(startDate, LocalDate.now());
                totalSaved += daysPassed * addiction.getSaver();
            }
        }

        if (totalSaved >= 100_000_000) {
            return new StreakFunFact(
                    "IDR 100 million wisely invested could generate financial freedom enjoyed by only 0.1% of the world's population!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/100m.png", "#92d19e"
            );
        } else if (totalSaved >= 50_000_000) {
            return new StreakFunFact(
                    "IDR 50 million is enough to buy 1,000 trees that will absorb 500 tons of carbon during their lifetime!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/50m.png", "#36ba36"
            );
        } else if (totalSaved >= 20_000_000) {
            return new StreakFunFact(
                    "IDR 20 million is enough to buy advanced electronic equipment to make your life easier",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/20m.png", "#4682B4"
            );
        } else if (totalSaved >= 10_000_000) {
            return new StreakFunFact(
                    "IDR 10 million used to start a small business has the potential to create income streams that could support your family for generations!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/10m.png", "#FF8C00"
            );
        } else if (totalSaved >= 5_000_000) {
            return new StreakFunFact(
                    "With IDR 5 million, you can start investing that could grow into a retirement fund of IDR 100 million in 30 years!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/5m.png", "#FFD700"
            );
        } else if (totalSaved >= 3_000_000) {
            return new StreakFunFact(
                    "IDR 3 million spent on vacation creates memories that will be remembered 10,000 times longer than items of the same price!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/3m.png", "#FF69B4"
            );
        } else if (totalSaved >= 1_000_000) {
            return new StreakFunFact(
                    "IDR 1 million invested in online courses can give you skills that increase your income by up to 300% in the next 5 years!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/1m.png", "#7c68fc"
            );
        } else if (totalSaved >= 500_000) {
            return new StreakFunFact(
                    "With IDR 500,000, you can buy running shoes that will last 1,000 km, equivalent to a Jakarta-Surabaya round trip!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/500k.png", "#5caff2"
            );
        } else if (totalSaved >= 100_000) {
            return new StreakFunFact(
                    "IDR 100,000 is enough to buy ingredients that produce 20 times more energy than fast food of the same price!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/100k.png", "#f7af28"
            );
        } else if (totalSaved >= 50_000) {
            return new StreakFunFact(
                    "IDR 50,000 can buy used books containing knowledge worth millions of rupiah for your future!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/50k.png", "#ed8a42"
            );
        } else if (totalSaved >= 10_000) {
            return new StreakFunFact(
                    "With IDR 10,000, you can buy a notebook capable of holding more than 5,000 brilliant ideas that might change your life!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/10k.png", "#00CED1"
            );
        } else {
            return new StreakFunFact(
                    "IDR 0 is the same amount Jeff Bezos and Bill Gates had when they started their first businesses!",
                    "https://sveclbpormdxqkyxblhh.supabase.co/storage/v1/object/public/funfact/funfactsaving/0k.png", "#A9A9A9"
            );
        }
    }

    private Addiction convertToEntity(AddictionDTO dto) {
        Addiction addiction = new Addiction();
        addiction.setUserId(dto.getUserId());
        addiction.setAddictionId(dto.getAddictionId());
        addiction.setSaver(dto.getSaver());
        addiction.setStreaks(dto.getStreaks());
        addiction.setMotivation(dto.getMotivation());
        addiction.setStartDate(dto.getStartDate());
        return addiction;
    }

    public Optional<AddictionDTO> getAddictionByUserIdAndAddictionId(String userId, String addictionId) {
        AddictionId id = new AddictionId(userId, addictionId);
        return addictionRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<AddictionDTO> getAddictionsByUser(String userId) {
        List<Addiction> addictions = addictionRepository.findAddictionsByUserId(userId);
        return addictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> processStreakAchievements(String userId, List<AddictionDTO> addictions) {
        List<Map<String, Object>> allNewAchievements = new ArrayList<>();

        for (AddictionDTO addiction : addictions) {
            if (addiction.getStartDate() != null) {
                LocalDate startDate = Instant.ofEpochMilli(addiction.getStartDate())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                long currentStreak = ChronoUnit.DAYS.between(startDate, LocalDate.now());
                addiction.setCurrentStreak(currentStreak);

                // Check streak achievements
                if (currentStreak >= 365) {
                    Map<String, Object> achievementData = checkAndAddAchievement(userId, "AC0009"); // Legendary Self-Control
                    if (achievementData != null) {
                        allNewAchievements.add(achievementData);
                    }
                }

                if (currentStreak >= 100) {
                    Map<String, Object> achievementData = checkAndAddAchievement(userId, "AC0012"); // Unbreakable Streak
                    if (achievementData != null) {
                        allNewAchievements.add(achievementData);
                    }
                }

                if (currentStreak >= 50) {
                    Map<String, Object> achievementData = checkAndAddAchievement(userId, "AC0015"); // Marathon Mode
                    if (achievementData != null) {
                        allNewAchievements.add(achievementData);
                    }
                }

                if (currentStreak >= 10) {
                    Map<String, Object> achievementData = checkAndAddAchievement(userId, "AC0004"); // Momentum Master
                    if (achievementData != null) {
                        allNewAchievements.add(achievementData);
                    }
                }
            }
        }

        return getHighestAchievement(allNewAchievements);
    }

    public Map<String, Object> getHighestAchievement(List<Map<String, Object>> achievements) {
        if (achievements == null || achievements.isEmpty()) {
            return null;
        }

        return achievements.stream()
                .min(Comparator.comparing(achievement -> {
                    String achievementId = (String) achievement.get("achievementId");
                    switch (achievementId) {
                        case "AC0009": return 1; // Legendary Self-Control (highest)
                        case "AC0012": return 2; // Unbreakable Streak
                        case "AC0015": return 3; // Marathon Mode
                        case "AC0004": return 4; // Momentum Master (lowest)
                        default: return Integer.MAX_VALUE;
                    }
                }))
                .orElse(null);
    }

    public Map<String, Object> checkAndAddAchievement(String userId, String achievementId) {
        boolean hasAchievement = achievementUserRepository.existsByIdUserIdAndIdAchievementId(userId, achievementId);

        if (!hasAchievement) {
            Optional<Achievement> achievementOpt = achievementRepository.findById(achievementId);
            if (achievementOpt.isPresent()) {
                Achievement achievement = achievementOpt.get();

                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    AchievementUserId achievementUserId = new AchievementUserId();
                    achievementUserId.setUserId(userId);
                    achievementUserId.setAchievementId(achievementId);

                    AchievementUser achievementUser = new AchievementUser();
                    achievementUser.setId(achievementUserId);
                    achievementUser.setUser(user);
                    achievementUser.setAchievement(achievement);
                    achievementUser.setAchievementDate(System.currentTimeMillis());

                    achievementUserRepository.save(achievementUser);

                    Map<String, Object> achievementData = new HashMap<>();
                    achievementData.put("achievementId", achievement.getAchievementId());
                    achievementData.put("achievementName", achievement.getAchievementName());
                    achievementData.put("achievementUrl", achievement.getAchievementUrl());
                    achievementData.put("achievementDesc", achievement.getAchievementDesc());

                    return achievementData;
                }
            }
        }
        return null;
    }

    // Menyimpan Addiction baru dan mengembalikan AddictionDTO
    public Optional<AddictionDTO> saveAddiction(AddictionDTO dto) {
        Addiction addiction = convertToEntity(dto);

        // Cek jika sudah pernah ada addiction untuk user ini
        boolean isFirstAddiction = !addictionRepository.existsByUserId(dto.getUserId());

        if (addictionRepository.existsById(new AddictionId(addiction.getUserId(), addiction.getAddictionId()))) {
            return Optional.empty();
        }

        Addiction savedAddiction = addictionRepository.save(addiction);

        if (isFirstAddiction) {
            String achievementId = "AC0003";
            boolean alreadyHas = achievementUserRepository.existsByIdUserIdAndIdAchievementId(dto.getUserId(), achievementId);

            if (!alreadyHas) {
                User user = userRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Achievement achievement = achievementRepository.findById(achievementId)
                        .orElseThrow(() -> new RuntimeException("Achievement not found"));
                AchievementUserId achievementUserId = new AchievementUserId();
                achievementUserId.setUserId(dto.getUserId());
                achievementUserId.setAchievementId(achievementId);

                AchievementUser achievementUser = new AchievementUser();
                achievementUser.setId(achievementUserId);
                achievementUser.setUser(user);
                achievementUser.setAchievement(achievement);
                achievementUser.setAchievementDate(System.currentTimeMillis());

                achievementUserRepository.save(achievementUser);
            }
        }

        return Optional.of(convertToDTO(savedAddiction));
    }

    // Mengupdate Addiction dan mengembalikan AddictionDTO
    public Optional<AddictionDTO> updateAddiction(AddictionDTO dto) {
        Addiction addiction = convertToEntity(dto); // convert DTO ke Addiction entity
        return addictionRepository.findById(new AddictionId(addiction.getUserId(), addiction.getAddictionId()))
                .map(existingAddiction -> {
                    if (addiction.getSaver() != null) {
                        existingAddiction.setSaver(addiction.getSaver());
                    }
                    if (addiction.getMotivation() != null) {
                        existingAddiction.setMotivation(addiction.getMotivation());
                    }
                    Addiction updatedAddiction = addictionRepository.save(existingAddiction);
                    return convertToDTO(updatedAddiction);
                });
    }

    // Mereset Addiction dan mengembalikan AddictionDTO
    public Map<String, Object> resetAddiction(AddictionDTO dto) {
        Addiction addiction = convertToEntity(dto);
        Map<String, Object> result = new HashMap<>();

        return addictionRepository.findById(new AddictionId(addiction.getUserId(), addiction.getAddictionId()))
                .map(existingAddiction -> {
                    long oldStartTimestamp = existingAddiction.getStartDate();

                    LocalDate oldStartDate = Instant.ofEpochMilli(oldStartTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    LocalDate now = LocalDate.now();
                    long calculatedStreak = ChronoUnit.DAYS.between(oldStartDate, now);

                    Long previousStreak = existingAddiction.getStreaks();
                    long previous = previousStreak != null ? previousStreak : 0;

                    if (calculatedStreak > previous) {
                        existingAddiction.setStreaks(calculatedStreak);
                    }

                    existingAddiction.setStartDate(System.currentTimeMillis());

                    Addiction resetAddiction = addictionRepository.save(existingAddiction);

                    result.put("addiction", convertToDTO(resetAddiction));

                    Map<String, Object> achievementData = checkAndAddAchievement(addiction.getUserId(), "AC0005");
                    if (achievementData != null) {
                        result.put("achievement", achievementData);
                    }

                    return result;
                })
                .orElse(null);
    }

    public boolean deleteAddiction(AddictionDTO dto) {
        Addiction addiction = convertToEntity(dto);
        AddictionId addictionId = new AddictionId(addiction.getUserId(), addiction.getAddictionId());
        if (!addictionRepository.existsById(addictionId)) {
            return false;
        }
        addictionRepository.deleteById(addictionId);
        return true;
    }
}