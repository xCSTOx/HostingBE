package com.example.breakfreeBE.community.repository;

import com.example.breakfreeBE.community.entity.BookmarkedPost;
import com.example.breakfreeBE.community.entity.BookmarkedPostId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkedPostRepository extends JpaRepository<BookmarkedPost, BookmarkedPostId> {

    // Find bookmarked posts by user ID
    List<BookmarkedPost> findByUserId(String userId);

    // Check if a post is bookmarked by user
    boolean existsByUserIdAndPostId(String userId, String postId);

    // Delete a bookmark by user ID and post ID
    void deleteByUserIdAndPostId(String userId, String postId);

    @Query("SELECT COUNT(a) FROM BookmarkedPost a JOIN a.user u WHERE u.userId = :userId")
    long countByUserId(String userId);
}