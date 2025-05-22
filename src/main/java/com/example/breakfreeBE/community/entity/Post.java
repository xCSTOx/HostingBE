package com.example.breakfreeBE.community.entity;

import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.userRegistration.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post")
public class Post {
    @Id
    @Column(name = "post_id", length = 6, nullable = false, unique = true)
    private String postId;

    @Column(name = "user_id", length = 6, nullable = false, unique = true)
    private String userId;

    @Column(name = "post_text",  nullable = false)
    private String postText;

    @Column(name = "post_date", nullable = false)
    private Long postDate;

    @Column(name = "achievement_id", length = 6)
    private String achievementId;

    @Column(name = "addiction_id", length = 6)
    private String addictionId;

    @Column(name = "challenge_id", length = 6)
    private String challengeId;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BookmarkedPost> bookmarkedPosts;

}
