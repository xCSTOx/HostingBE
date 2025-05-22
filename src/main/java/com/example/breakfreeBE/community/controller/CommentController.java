package com.example.breakfreeBE.community.controller;

import com.example.breakfreeBE.community.dto.CommentDTO;
import com.example.breakfreeBE.community.service.CommentService;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Create a new comment
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<Map<String, Object>>> createComment(@RequestBody CommentDTO commentDTO) {
        try {
            if (commentDTO.getPostId() == null || commentDTO.getUserId() == null || commentDTO.getCommentText() == null ||
                    commentDTO.getPostId().isBlank() || commentDTO.getUserId().isBlank() || commentDTO.getCommentText().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Required fields cannot be empty"), null)
                );
            }

            Map<String, Object> result = commentService.createComment(commentDTO);
            CommentDTO createdComment = (CommentDTO) result.get("comment");

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("comment", createdComment);

            if (result.containsKey("achievement")) {
                responseData.put("achievement", result.get("achievement"));
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new BaseResponse<>(new MetaResponse(true, "Comment created successfully and achievement earned!"), responseData)
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new BaseResponse<>(new MetaResponse(true, "Comment created successfully"), responseData)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to create comment: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/view")
    public ResponseEntity<BaseResponse<List<CommentDTO>>> getCommentsByPost(@RequestBody CommentDTO commentDTO) {
        try {
            if (commentDTO.getPostId() == null || commentDTO.getPostId().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Post ID cannot be empty"), null)
                );
            }

            List<CommentDTO> comments = commentService.getCommentsByPost(commentDTO.getPostId());

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Comments retrieved successfully"), comments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve comments: " + e.getMessage()), null)
            );
        }
    }
}