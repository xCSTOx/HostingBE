package com.example.breakfreeBE.userRegistration.entity;

import com.example.breakfreeBE.achievement.entity.AchievementUser;
import com.example.breakfreeBE.addiction.entity.Addiction;
import com.example.breakfreeBE.avatar.entity.Avatar;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.community.entity.BookmarkedPost;
import com.example.breakfreeBE.community.entity.Comment;
import com.example.breakfreeBE.community.entity.Post;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "\"user\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "user_id", length = 6, nullable = false, unique = true)
    private String userId;

    @Column(name = "avatar_id", length = 6, unique = true, insertable = false, updatable = false)
    private String avatarId;

    @Column(name = "username", length = 64, unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, message = "Username must be at least 3 characters")
    private String username;

    @Column(name = "password", length = 64, nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Addiction> addictions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Post> post;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comment;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BookmarkedPost> bookmarked_post;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Challenge> challenges;

    @ManyToOne
    @JoinColumn(name = "avatar_id")
    private Avatar avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<AchievementUser> achievements;

    public User(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public Addiction getAddiction() {
        return (addictions != null && !addictions.isEmpty()) ? addictions.get(0) : null;
    }


}
