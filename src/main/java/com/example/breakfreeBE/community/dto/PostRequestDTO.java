package com.example.breakfreeBE.community.dto;

import lombok.Data;

@Data
public class PostRequestDTO {
    private String userId;
    private String postId;
    private String achievementId;
    private String addictionId;
    private String challengeId;
    private String postText;
    private Long postDate;
}