package com.example.breakfreeBE.community.controller;

import com.example.breakfreeBE.community.dto.BookmarkedRequestDTO;
import com.example.breakfreeBE.community.dto.PostDTO;
import com.example.breakfreeBE.community.service.BookmarkedPostService;
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
@RequestMapping("/bookmarks")
public class BookmarkedPostController {

    @Autowired
    private BookmarkedPostService bookmarkedPostService;

    @PostMapping("/bookmark")
    public ResponseEntity<BaseResponse<Map<String, Object>>> bookmarkPost(@RequestBody BookmarkedRequestDTO bookmarkRequest) {
        try {
            // Validasi field-field yang diperlukan
            if (bookmarkRequest.getUserId() == null || bookmarkRequest.getPostId() == null ||
                    bookmarkRequest.getUserId().isBlank() || bookmarkRequest.getPostId().isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new BaseResponse<>(new MetaResponse(false, "Bookmark not found"), null)
                );
            }

            Map<String, Object> result = bookmarkedPostService.bookmarkPost(bookmarkRequest);
            boolean success = (boolean) result.get("success");

            Map<String, Object> responseData = new HashMap<>();

            if (result.containsKey("achievement")) {
                responseData.put("achievement", result.get("achievement"));

                if (success) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(
                            new BaseResponse<>(new MetaResponse(true, "Post bookmarked successfully and achievement earned!"), responseData)
                    );
                } else {
                    return ResponseEntity.ok(
                            new BaseResponse<>(new MetaResponse(true, "Post is already bookmarked but achievement earned!"), responseData)
                    );
                }
            }

            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new BaseResponse<>(new MetaResponse(true, "Post bookmarked successfully"), responseData)
                );
            } else {
                return ResponseEntity.ok(
                        new BaseResponse<>(new MetaResponse(true, "Post is already bookmarked"), responseData)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to bookmark post: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/unbookmark")
    public ResponseEntity<BaseResponse<Void>> unbookmarkPost(@RequestBody BookmarkedRequestDTO bookmarkRequest) {
        try {
            // Validasi field-field yang diperlukan
            if (bookmarkRequest.getUserId() == null || bookmarkRequest.getPostId() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new BaseResponse<>(new MetaResponse(false, "Bookmark not found"), null)
                );
            }

            boolean success = bookmarkedPostService.unbookmarkPost(bookmarkRequest);

            if (success) {
                return ResponseEntity.ok(
                        new BaseResponse<>(new MetaResponse(true, "Post unbookmarked successfully"), null)
                );
            } else {
                return ResponseEntity.ok(
                        new BaseResponse<>(new MetaResponse(true, "Post was not bookmarked"), null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to unbookmark post: " + e.getMessage()), null)
            );
        }
    }

    @PostMapping("/view")
    public ResponseEntity<BaseResponse<List<PostDTO>>> viewBookmarkedPosts(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");

            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new BaseResponse<>(new MetaResponse(false, "Bookmark not found: userId is missing"), null)
                );
            }

            List<PostDTO> bookmarkedDTOs = bookmarkedPostService.viewBookmarkedPosts(userId);

            return ResponseEntity.ok(
                    new BaseResponse<>(new MetaResponse(true, "Bookmarked posts retrieved successfully"), bookmarkedDTOs)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BaseResponse<>(new MetaResponse(false, "Failed to retrieve bookmarked posts: " + e.getMessage()), null)
            );
        }
    }
}