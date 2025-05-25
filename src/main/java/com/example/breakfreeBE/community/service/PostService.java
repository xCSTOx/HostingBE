package com.example.breakfreeBE.community.service;

import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.addiction.repository.AddictionDataRepository;
import com.example.breakfreeBE.addiction.repository.AddictionRepository;
import com.example.breakfreeBE.avatar.repository.AvatarRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeDataRepository;
import com.example.breakfreeBE.challenge.repository.ChallengeRepository;
import com.example.breakfreeBE.community.entity.Post;
import com.example.breakfreeBE.community.repository.BookmarkedPostRepository;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.community.repository.CommentRepository;
import com.example.breakfreeBE.community.repository.PostRepository;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import com.example.breakfreeBE.community.dto.PostDTO;
import com.example.breakfreeBE.community.dto.PostRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final String FIRST_POST_ACHIEVEMENT_ID = "AC0010";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final AvatarRepository avatarRepository;
    private final AddictionRepository addictionRepository;
    private final AddictionDataRepository addictionDataRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeDataRepository challengeDataRepository;
    private final BookmarkedPostRepository bookmarkedPostRepository;

    @Autowired
    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            CommentService commentService,
            AvatarRepository avatarRepository,
            AddictionRepository addictionRepository,
            AddictionDataRepository addictionDataRepository,
            AchievementRepository achievementRepository,
            AchievementUserRepository achievementUserRepository,
            ChallengeRepository challengeRepository,
            ChallengeDataRepository challengeDataRepository,
            BookmarkedPostRepository bookmarkedPostRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.avatarRepository = avatarRepository;
        this.addictionRepository = addictionRepository;
        this.addictionDataRepository = addictionDataRepository;
        this.achievementRepository = achievementRepository;
        this.achievementUserRepository = achievementUserRepository;
        this.challengeRepository = challengeRepository;
        this.challengeDataRepository = challengeDataRepository;
        this.bookmarkedPostRepository = bookmarkedPostRepository;
    }

    public Map<String, Object> createPost(PostRequestDTO postRequestDTO) {
        String postId = generatePostId();
        String userId = postRequestDTO.getUserId();

        Post post = new Post();
        post.setPostId(postId);
        post.setUserId(userId);
        post.setPostText(postRequestDTO.getPostText());
        post.setPostDate(postRequestDTO.getPostDate() != null ? postRequestDTO.getPostDate() : Instant.now().toEpochMilli());

        if (postRequestDTO.getAchievementId() != null) {
            post.setAchievementId(postRequestDTO.getAchievementId());
        }

        if (postRequestDTO.getAddictionId() != null) {
            post.setAddictionId(postRequestDTO.getAddictionId());
        }

        if (postRequestDTO.getChallengeDataId() != null) {
            post.setChallengeDataId(postRequestDTO.getChallengeDataId());
        }

        Post savedPost = postRepository.save(post);

        Map<String, Object> result = new HashMap<>();
        result.put("post", convertToDTOP(savedPost, userId));

        PostAchievement(userId, result);

        return result;
    }

    private void PostAchievement(String userId, Map<String, Object> result) {

        long postCount = postRepository.countByUserId(userId);

        if (postCount == 1) {

            boolean hasAchievement = achievementUserRepository.existsByIdUserIdAndIdAchievementId(userId, FIRST_POST_ACHIEVEMENT_ID);

            if (!hasAchievement) {

                Optional<Achievement> achievementOpt = achievementRepository.findById(FIRST_POST_ACHIEVEMENT_ID);

                if (achievementOpt.isPresent()) {
                    Achievement achievement = achievementOpt.get();

                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        AchievementUserId achievementUserId = new AchievementUserId();
                        achievementUserId.setUserId(userId);
                        achievementUserId.setAchievementId(FIRST_POST_ACHIEVEMENT_ID);

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
    public void deletePost(String postId, String userId) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();

            if (post.getUserId().equals(userId)) {
                commentRepository.deleteByPostId(postId);
                postRepository.deleteById(postId);
            } else {
                throw new RuntimeException("User is not authorized to delete this post");
            }
        } else {
            throw new RuntimeException("Post not found with id: " + postId);
        }
    }

    public PostDTO updatePost(PostRequestDTO postRequestDTO) {
        String postId = postRequestDTO.getPostId();
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();

            if (post.getUserId().equals(postRequestDTO.getUserId())) {
                // Update post text
                post.setPostText(postRequestDTO.getPostText());

                // Handle achievementId - set to null if not provided in request
                if (postRequestDTO.getAchievementId() != null && !postRequestDTO.getAchievementId().isBlank()) {
                    post.setAchievementId(postRequestDTO.getAchievementId());
                } else {
                    post.setAchievementId(null);
                }

                // Handle addictionId - set to null if not provided in request
                if (postRequestDTO.getAddictionId() != null && !postRequestDTO.getAddictionId().isBlank()) {
                    post.setAddictionId(postRequestDTO.getAddictionId());
                } else {
                    post.setAddictionId(null);
                }

                // Handle challengeId - set to null if not provided in request
                if (postRequestDTO.getChallengeDataId() != null && !postRequestDTO.getChallengeDataId().isBlank()) {
                    post.setChallengeDataId(postRequestDTO.getChallengeDataId());
                } else {
                    post.setChallengeDataId(null);
                }

                Post updatedPost = postRepository.save(post);
                return convertToDTOP(updatedPost, postRequestDTO.getUserId());
            } else {
                throw new RuntimeException("User is not authorized to update this post");
            }
        } else {
            throw new RuntimeException("Post not found with id: " + postId);
        }
    }

    public PostDTO getPostDetailsById(String postId, String userId) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            return convertToDTOP(post, userId);
        } else {
            throw new RuntimeException("Post not found with id: " + postId);
        }
    }

    public List<PostDTO> getAllPosts(String userId) {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "postId"));

        return posts.stream()
                .map(post -> convertToDTOP(post, userId))
                .collect(Collectors.toList());
    }

    public List<PostDTO> getPostsByUserId(String userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream()
                .map(post -> convertToDTOP(post, userId))
                .collect(Collectors.toList());
    }

    private String generatePostId() {
        Optional<String> lastPostIdOpt = postRepository.findLastPostId();
        String lastPostId = lastPostIdOpt.orElse("P00000");

        try {
            int numericPart = Integer.parseInt(lastPostId.substring(1));
            return String.format("P%05d", numericPart + 1);
        } catch (NumberFormatException e) {
            System.err.println("Invalid lastPostId format: " + lastPostId);
            return "P00001";
        }
    }

    public PostDTO convertToDTOP(Post post, String currentUserId) {
        PostDTO dto = new PostDTO();
        dto.setPostId(post.getPostId());
        dto.setPostText(post.getPostText());
        dto.setPostDate(post.getPostDate());

        // Check if the current user has bookmarked this post
        boolean isBookmarked = bookmarkedPostRepository.existsByUserIdAndPostId(currentUserId, post.getPostId());
        dto.setBookmarked(isBookmarked);

        userRepository.findById(post.getUserId()).ifPresent(user -> {
            PostDTO.ProfileDTO profileDTO = new PostDTO.ProfileDTO();
            profileDTO.setUserId(user.getUserId());
            profileDTO.setUsername(user.getUsername());

            if (user.getAvatarId() != null) {
                avatarRepository.findById(user.getAvatarId())
                        .ifPresent(avatar -> profileDTO.setAvatarUrl(avatar.getAvatarUrl()));
            }

            dto.setProfile(profileDTO);
        });

        if (post.getAddictionId() != null) {
            addictionRepository.findByUserIdAndAddictionId(post.getUserId(), post.getAddictionId())
                    .ifPresent(addiction -> {
                        PostDTO.StreakDataDTO streakData = new PostDTO.StreakDataDTO();
                        streakData.setAddictionId(addiction.getAddictionId());

                        // Nama addiksi dari addiction_data
                        addictionDataRepository.findById(addiction.getAddictionId())
                                .ifPresent(addictionData -> streakData.setAddictionName(addictionData.getAddictionName()));

                        long now = System.currentTimeMillis();
                        long startDate = addiction.getStartDate();
                        long calculatedStreak = (now - startDate) / (1000 * 60 * 60 * 24); // dalam hari
                        Long streaks = addiction.getStreaks();

                        if (streaks != null && streaks > calculatedStreak) {
                            streakData.setStreakDays(streaks);
                        } else {
                            streakData.setStreakDays(calculatedStreak);
                        }

                        dto.setStreakData(streakData);
                    });
        }

        if (post.getAchievementId() != null) {
            achievementRepository.findById(post.getAchievementId())
                    .ifPresent(achievement -> {
                        PostDTO.AchievementDataDTO achievementData = new PostDTO.AchievementDataDTO();
                        achievementData.setAchievementId(achievement.getAchievementId());
                        achievementData.setAchievementName(achievement.getAchievementName());
                        achievementData.setAchievementUrl(achievement.getAchievementUrl());
                        dto.setAchievementData(achievementData);
                    });
        }

        if (post.getChallengeDataId() != null) {
            challengeDataRepository.findById(post.getChallengeDataId())
                    .ifPresent(challengeData -> {
                        PostDTO.ChallengeDataDTO challengeDataDTO = new PostDTO.ChallengeDataDTO();
                        challengeDataDTO.setChallengeDataId(challengeData.getChallengeDataId());
                        challengeDataDTO.setChallengeName(challengeData.getChallengeName());
                        challengeDataDTO.setChallengeUrl(challengeData.getChallengeUrl());
                        challengeDataDTO.setColor(challengeData.getColor());
                        dto.setChallengeData(challengeDataDTO);
                    });
        }

        return dto;
    }

    // Method overload untuk backward compatibility (jika ada yang masih menggunakan tanpa userId)
    public PostDTO convertToDTOP(Post post) {
        return convertToDTOP(post, post.getUserId());
    }
}