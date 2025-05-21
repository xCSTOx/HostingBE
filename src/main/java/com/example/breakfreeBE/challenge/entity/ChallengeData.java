package com.example.breakfreeBE.challenge.entity;

import com.example.breakfreeBE.addiction.entity.Addiction;
import com.example.breakfreeBE.addiction.entity.AddictionData;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeData {

    @Id
    @Column(name = "challenge_data_id", length = 6)
    private String challengeDataId;

    @Column(name = "addiction_id", length = 6)
    private String addictionId;

    @Column(name = "challenge_name", length = 255)
    private String challengeName;

    @Column(name = "challenge_desc", length = 255)
    private String challengeDesc;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "color", length = 64)
    private String color;

    @Column(name = "challenge_url", length = 255)
    private String challengeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_id", insertable = false, updatable = false)
    private AddictionData addictionData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "addiction_id", referencedColumnName = "addiction_id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private Addiction addiction;

}
