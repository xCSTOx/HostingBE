package com.example.breakfreeBE.challenge.controller;

import com.example.breakfreeBE.achievement.dto.AchievementResponse;
import com.example.breakfreeBE.challenge.dto.*;
import com.example.breakfreeBE.challenge.entity.Challenge;
import com.example.breakfreeBE.challenge.entity.ChallengeData;
import com.example.breakfreeBE.challenge.service.ChallengeService;
import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @PostMapping("/detail")
    public ResponseEntity<BaseResponse<ChallengeDetailResponse>> getChallengeDetail(
            @Valid @RequestBody ChallengeUserRequest request) {

        ChallengeDetailResponse response = challengeService.getChallengeDetail(request);
        return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "Challenge detail retrieved"), response));
    }

    @GetMapping("/data")
    public ResponseEntity<BaseResponse<List<ChallengeData>>> getAllDailyChallenges() {
        List<ChallengeData> challenges = challengeService.getAllDailyChallenges();
        return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "All challenges retrieved"), challenges));
    }

    @GetMapping("/users/{userId}/ongoing")
    public ResponseEntity<BaseResponse<List<ChallengeOngoingResponse>>> getOngoingChallenges(@PathVariable String userId) {
        List<ChallengeOngoingResponse> challenges = challengeService.getOngoingChallenges(userId);
        return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "Ongoing challenges retrieved"), challenges));
    }

    @PostMapping("/participate")
    public ResponseEntity<BaseResponse<?>> participateChallenge(@Valid @RequestBody ChallengeUserRequest request) {
        BaseResponse<?> response = challengeService.participateChallenge(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/stop")
    public ResponseEntity<BaseResponse<String>> stopChallenge(@Valid @RequestBody ChallengeUserRequest request) {
        challengeService.stopChallenge(request);
        return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "Challenge stopped"), "Challenge removed"));
    }

    @PostMapping("/progress")
    public BaseResponse<Map<String, Object>> updateProgress(@RequestBody ChallengeUserRequest request) {
        return challengeService.updateProgress(request);
    }

    @GetMapping("/users/{userId}/completed")
    public ResponseEntity<BaseResponse<List<ChallengeCompletedResponse>>> getCompletedChallenges(@PathVariable String userId) {
        List<ChallengeCompletedResponse> challenges = challengeService.getCompletedChallenges(userId);
        return ResponseEntity.ok(new BaseResponse<>(new MetaResponse(true, "Completed challenges retrieved"), challenges));
    }


}