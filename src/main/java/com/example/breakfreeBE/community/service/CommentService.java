package com.example.breakfreeBE.community.service;

import com.example.breakfreeBE.achievement.entity.Achievement;
import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.achievement.entity.AchievementUserId;
import com.example.breakfreeBE.achievement.repository.AchievementRepository;
import com.example.breakfreeBE.achievement.repository.AchievementUserRepository;
import com.example.breakfreeBE.avatar.entity.Avatar;
import com.example.breakfreeBE.avatar.repository.AvatarRepository;
import com.example.breakfreeBE.community.dto.CommentDTO;
import com.example.breakfreeBE.community.entity.Comment;
import com.example.breakfreeBE.community.entity.Post;
import com.example.breakfreeBE.community.repository.CommentRepository;
import com.example.breakfreeBE.community.repository.PostRepository;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.example.breakfreeBE.userRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private static final String FIRST_COMMENT_ACHIEVEMENT_ID = "AC0007";

    private UserRepository userRepository;
    private AchievementUserRepository achievementUserRepository;
    private AchievementRepository achievementRepository;
    private CommentRepository commentRepository;
    private AvatarRepository avatarRepository;
    private PostRepository postRepository;

    @Autowired
    public CommentService(AchievementRepository achievementRepository, AchievementUserRepository achievementUserRepository, UserRepository userRepository, CommentRepository commentRepository, AvatarRepository avatarRepository, PostRepository postRepository
    ){
        this.achievementRepository = achievementRepository;
        this.achievementUserRepository = achievementUserRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.avatarRepository = avatarRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public Map<String, Object> createComment(CommentDTO commentDTO) {
        Optional<Post> postOptional = postRepository.findById(commentDTO.getPostId());
        String commentId = generateCommentId();

        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setPostId(commentDTO.getPostId());
        comment.setUserId(commentDTO.getUserId());
        comment.setCommentText(commentDTO.getCommentText());
        comment.setCommentDate(Instant.now().toEpochMilli());

        Comment savedComment = commentRepository.save(comment);

        Map<String, Object> result = new HashMap<>();
        // Convert to DTO with complete user information (username, avatar)
        CommentDTO newCommentDTO = convertToDTO(savedComment);
        result.put("comment", newCommentDTO);

        commentAchievement(commentDTO.getUserId(), result);

        return result;
    }

    private void commentAchievement(String userId, Map<String, Object> result) {
        long commentCount = commentRepository.countByUserId(userId);

        if (commentCount == 1) {
            boolean hasAchievement = achievementUserRepository.existsByIdUserIdAndIdAchievementId(userId, FIRST_COMMENT_ACHIEVEMENT_ID);

            if (!hasAchievement) {
                Optional<Achievement> achievementOpt = achievementRepository.findById(FIRST_COMMENT_ACHIEVEMENT_ID);

                if (achievementOpt.isPresent()) {
                    Achievement achievement = achievementOpt.get();
                    Optional<User> userOpt = userRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        AchievementUserId achievementUserId = new AchievementUserId();
                        achievementUserId.setUserId(userId);
                        achievementUserId.setAchievementId(FIRST_COMMENT_ACHIEVEMENT_ID);

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

    public List<CommentDTO> getCommentsByPost(String postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCommentIdDesc(postId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private String generateCommentId() {
        Optional<String> lastCommentIdOpt = commentRepository.findLastCommentId();

        if (lastCommentIdOpt.isPresent()) {
            String lastCommentId = lastCommentIdOpt.get();
            int nextId = Integer.parseInt(lastCommentId.substring(1)) + 1;
            return String.format("C%05d", nextId);
        } else {
            return "C00001";
        }
    }

    public CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setCommentText(comment.getCommentText());
        dto.setCommentDate(comment.getCommentDate());

        Optional<User> user = userRepository.findById(comment.getUserId());
        if (user.isPresent()) {
            User userEntity = user.get();
            dto.setUsername(userEntity.getUsername());

            String avatarId = userEntity.getAvatarId();
            if (avatarId != null) {
                Optional<Avatar> avatar = avatarRepository.findById(avatarId);
                avatar.ifPresent(a -> dto.setAvatarUrl(a.getAvatarUrl()));
            }
        }

        return dto;
    }
}