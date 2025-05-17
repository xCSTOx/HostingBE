package com.example.breakfreeBE.addiction.dto;

import com.example.breakfreeBE.addiction.service.AddictionService;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AddictionDTO {
    private String userId;
    private String addictionId;
    private Integer saver;
    private Long streaks;
    private String motivation;
    private Long startDate;
    private AddictionService.StreakFunFact streakFunFact; // getter dan setter disediakan
    private String addictionName; // dari relasi AddictionData
}