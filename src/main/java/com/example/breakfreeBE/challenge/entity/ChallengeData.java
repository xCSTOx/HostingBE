package com.example.breakfreeBE.challenge.entity;

import com.example.breakfreeBE.addiction.entity.Addiction;
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
    @Column(name = "challenge_data_id")
    private String challengeDataId;

    @Column(name = "challenge_name")
    private String challengeName;

    @Column(name = "challenge_desc")
    private String challengeDesc;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "color")
    private String color;

    @Column(name = "challenge_url")
    private String challengeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "addiction_id", referencedColumnName = "addiction_id"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private Addiction addiction;


}
