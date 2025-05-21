package com.example.breakfreeBE.community.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommentDTO {
    private String commentId;
    private String postId;
    private String userId;
    private String commentText;
    private Long commentDate;
    private String username;
    private String avatarUrl;
}