package com.FinalYear.entity;

import com.FinalYear.Dto.Prediction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long resultId;

    private String imageName;

    private LocalDateTime timestamp;

    @ElementCollection
    private List<Prediction> predictions=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



}
