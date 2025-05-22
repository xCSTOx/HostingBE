package com.example.breakfreeBE.community.repository;

import com.example.breakfreeBE.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    List<Post> findByUserId(String userId);

    @Query("SELECT p FROM Post p JOIN BookmarkedPost bp ON p.postId = bp.postId WHERE bp.userId = :userId")
    List<Post> findBookmarkedPostsByUserId(@Param("userId") String userId);

    @Query("SELECT p, COUNT(c) FROM Post p LEFT JOIN Comment c ON p.postId = c.postId GROUP BY p")
    List<Object[]> findPostsWithCommentCount();

    @Query("SELECT p.postId FROM Post p ORDER BY p.postId DESC")
    Optional<String> findLastPostId();

    @Query("SELECT COUNT(a) FROM Post a JOIN a.user u WHERE u.userId = :userId")
    long countByUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.challengeId = :challengeId")
    void deleteByChallengeId(@Param("challengeId") String challengeId);

}
