package com.example.breakfreeBE.community.controller;

import com.example.breakfreeBE.community.service.PostService;
import com.example.breakfreeBE.community.dto.PostDTO;
import com.example.breakfreeBE.community.dto.PostRequestDTO;
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
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/view")
    public ResponseEntity<BaseResponse<List<PostDTO>>> getAllPosts() {
        try {
            List<PostDTO> posts = postService.getAllPosts();
            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Posts retrieved successfully"), posts)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve posts: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<Map<String, Object>>> createPost(@RequestBody PostRequestDTO postRequestDTO) {
        try {
            if (postRequestDTO.getUserId() == null || postRequestDTO.getPostText() == null
                    || postRequestDTO.getUserId().isBlank() || postRequestDTO.getPostText().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Invalid request: userId or postText missing"), null)
                );
            }

            Map<String, Object> result = postService.createPost(postRequestDTO);
            PostDTO createdPost = (PostDTO) result.get("post");

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("postId", createdPost.getPostId());

            // If a new achievement was earned, include it in the response
            if (result.containsKey("achievement")) {
                responseData.put("achievement", result.get("achievement"));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new BaseResponse<>(new MetaResponse(true, "Post created successfully and achievement earned!"), responseData)
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new BaseResponse<>(new MetaResponse(true, "Post created successfully"), responseData)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to create post: " + e.getMessage()), null)
            );
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<Void>> deletePost(@RequestBody Map<String, String> requestBody) {
        try {
            String postId = requestBody.get("postId");
            String userId = requestBody.get("userId");

            if (postId == null || userId == null || postId.isBlank() || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Invalid request: postId or userId missing"), null)
                );
            }

            postService.deletePost(postId, userId);

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Post deleted successfully"), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to delete post: " + e.getMessage()), null)
            );
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<Void>> updatePost(@RequestBody PostRequestDTO postRequestDTO) {
        try {
            if (postRequestDTO.getUserId() == null || postRequestDTO.getPostText() == null
                    || postRequestDTO.getUserId().isBlank() || postRequestDTO.getPostText().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Post Not Found"), null)
                );
            }

            PostDTO updatedPost = postService.updatePost(postRequestDTO);

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Post updated successfully"), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to update post: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/viewdetail")
    public ResponseEntity<BaseResponse<PostDTO>> viewPostDetail(@RequestBody Map<String, String> requestBody) {
        try {
            String postId = requestBody.get("postId");
            String userId = requestBody.get("userId");

            if (postId == null || userId == null || postId.isBlank() || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Invalid request: postId or userId missing"), null)
                );
            }

            PostDTO postDetail = postService.getPostDetailsById(postId, userId);

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Post details retrieved successfully"), postDetail)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve post details: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/viewbyuser")
    public ResponseEntity<BaseResponse<List<PostDTO>>> getPostsByUserId(@RequestBody Map<String, String> requestBody) {
        try {
            String userId = requestBody.get("userId");

            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new BaseResponse<>(new MetaResponse(false, "Invalid request: userId missing"), null)
                );
            }

            List<PostDTO> posts = postService.getPostsByUserId(userId);

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "User posts retrieved successfully"), posts)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve user posts: " + e.getMessage()), null)
            );
        }
    }
}