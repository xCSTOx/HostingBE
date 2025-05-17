package com.example.breakfreeBE.addiction.entity;

import com.example.breakfreeBE.userRegistration.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@IdClass(AddictionId.class)
@Data
@NoArgsConstructor
public class Addiction {

    @Id
    @Column(name = "user_id", length = 6)
    private String userId;

    @Id
    @Column(name = "addiction_id", length = 6)
    private String addictionId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference //karena relasi dua arah, dan bisa tetap serialisasi satu arah. (Pake ini untuk tidak loop datanya)
    private User user;

    @ManyToOne
    @JoinColumn(name = "addiction_id", insertable = false, updatable = false)
    private AddictionData addictionData;

    @Column(name = "saver", nullable = false)
    private Integer saver;

    @Column(name = "streaks")
    private Long streaks;

    @Column(name = "motivation", nullable = false)
    private String motivation;

    @Column(name = "start_date", nullable = false)
    private Long startDate;

    @Column(name = "addiction_name")
    private String addictionName;

}