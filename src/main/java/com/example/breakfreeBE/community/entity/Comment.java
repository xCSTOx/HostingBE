package com.example.breakfreeBE.community.entity;

import com.example.breakfreeBE.userRegistration.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @Column(name = "comment_id",length = 6, nullable = false, unique = true )
    private String commentId;

    @Column(name = "post_id", length = 6, nullable = false, unique = true )
    private String postId;

    @Column(name = "user_id", length = 6, nullable = false, unique = true)
    private String userId;

    @Column(name = "comment_text",  nullable = false)
    private String commentText;

    @Column(name = "comment_date",  nullable = false)
    private Long commentDate;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    @JsonBackReference
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private User user;
}