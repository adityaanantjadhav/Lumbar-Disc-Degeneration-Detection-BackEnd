package com.FinalYear.Dto;


import com.FinalYear.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultResponseDto {


    private Long resultId;

    private String imageName;

    private int predictedLocationCount;
    private LocalDateTime timestamp;

    private Map<String,String> predictedConditions;

    private List<Prediction> predictions=new ArrayList<>();

    public ResultResponseDto(Long resultId, String imageName, LocalDateTime timestamp) {
        this.resultId = resultId;
        this.imageName = imageName;
        this.timestamp = timestamp;
    }


}
