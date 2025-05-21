package com.example.breakfreeBE.community.repository;

import com.example.breakfreeBE.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    // Delete comments by post ID
    void deleteByPostId(String postId);

    List<Comment> findByPostIdOrderByCommentIdDesc(String postId);

    @Query("SELECT c.commentId FROM Comment c ORDER BY c.commentId DESC LIMIT 1")
    Optional<String> findLastCommentId();

    @Query("SELECT COUNT(a) FROM Comment a JOIN a.user u WHERE u.userId = :userId")
    long countByUserId(String userId);
}