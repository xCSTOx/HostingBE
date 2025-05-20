package com.example.breakfreeBE.achievement.repository;

import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.userRegistration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementUserRepository extends JpaRepository<AchievementUser, AchievementUserId> {

    List<AchievementUser> findByIdUserId(String userId);

    List<AchievementUser> findByUser(User user);

    boolean existsById(AchievementUserId id);

    boolean existsByIdUserIdAndIdAchievementId(String userId, String achievementId);


}