package com.example.breakfreeBE.community.service;

import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.community.dto.BookmarkedRequestDTO;
import com.example.breakfreeBE.community.dto.PostDTO;
import com.example.breakfreeBE.community.entity.BookmarkedPost;
import com.example.breakfreeBE.community.entity.Post;
import com.example.breakfreeBE.community.repository.BookmarkedPostRepository;
import com.example.breakfreeBE.community.repository.PostRepository;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookmarkedPostService {

    private static final String BOOKMARK_ACHIEVEMENT_ID = "AC0013";
    private static final int BOOKMARK_THRESHOLD = 3;

    private final BookmarkedPostRepository bookmarkedPostRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final AchievementRepository achievementRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookmarkedPostService(BookmarkedPostRepository bookmarkedPostRepository, PostRepository postRepository, PostService postService, AchievementRepository achievementRepository, AchievementUserRepository achievementUserRepository, UserRepository userRepository
    ) {
        this.bookmarkedPostRepository = bookmarkedPostRepository;
        this.postRepository = postRepository;
        this.postService = postService;
        this.achievementRepository = achievementRepository;
        this.achievementUserRepository = achievementUserRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> bookmarkPost(BookmarkedRequestDTO bookmarkRequest) {
        Map<String, Object> result = new HashMap<>();

        if (bookmarkedPostRepository.existsByUserIdAndPostId(
                bookmarkRequest.getUserId(), bookmarkRequest.getPostId())) {
            result.put("success", false);
            checkBookmarkAchievement(bookmarkRequest.getUserId(), result);
            return result;
        }

        BookmarkedPost bookmark = new BookmarkedPost();
        bookmark.setUserId(bookmarkRequest.getUserId());
        bookmark.setPostId(bookmarkRequest.getPostId());

        bookmarkedPostRepository.save(bookmark);
        result.put("success", true);

        checkBookmarkAchievement(bookmarkRequest.getUserId(), result);

        return result;
    }

    private void checkBookmarkAchievement(String userId, Map<String, Object> result) {

        long bookmarkCount = bookmarkedPostRepository.countByUserId(userId);

        if (bookmarkCount >= BOOKMARK_THRESHOLD) {
            boolean hasAchievement = achievementUserRepository.existsByIdUserIdAndIdAchievementId(userId, BOOKMARK_ACHIEVEMENT_ID);

            if (!hasAchievement) {
                Optional<Achievement> achievementOpt = achievementRepository.findById(BOOKMARK_ACHIEVEMENT_ID);

                if (achievementOpt.isPresent()) {
                    Achievement achievement = achievementOpt.get();

                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        AchievementUserId achievementUserId = new AchievementUserId();
                        achievementUserId.setUserId(userId);
                        achievementUserId.setAchievementId(BOOKMARK_ACHIEVEMENT_ID);

                        AchievementUser achievementUser = new AchievementUser();
                        achievementUser.setId(achievementUserId);
                        achievementUser.setUser(user);
                        achievementUser.setAchievement(achievement);
                        achievementUser.setAchievementDate(System.currentTimeMillis());

                        achievementUserRepository.save(achievementUser);

                        Map<String, String> achievementInfo = new HashMap<>();
                        achievementInfo.put("achievementId", achievement.getAchievementId());
                        achievementInfo.put("achievementName", achievement.getAchievementName());
                        achievementInfo.put("achievementUrl", achievement.getAchievementUrl());
                        achievementInfo.put("achievementDesc", achievement.getAchievementDesc());

                        result.put("achievement", achievementInfo);
                    }
                }
            }
        }
    }

    @Transactional
    public boolean unbookmarkPost(BookmarkedRequestDTO bookmarkRequest) {
        if (!bookmarkedPostRepository.existsByUserIdAndPostId(
                bookmarkRequest.getUserId(), bookmarkRequest.getPostId())) {
            return false;
        }

        bookmarkedPostRepository.deleteByUserIdAndPostId(
                bookmarkRequest.getUserId(), bookmarkRequest.getPostId());
        return true;
    }

    public List<PostDTO> viewBookmarkedPosts(String userId) {
        List<Post> bookmarks = postRepository.findBookmarkedPostsByUserId(userId);
        List<PostDTO> result = new ArrayList<>();

        for (Post bookmark : bookmarks) {
            postRepository.findById(bookmark.getPostId()).ifPresent(post -> {
                PostDTO dto = postService.convertToDTOP(post, userId);
                result.add(dto);
            });
        }

        return result;
    }
}