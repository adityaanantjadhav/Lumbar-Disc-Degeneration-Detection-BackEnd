package com.FinalYear.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlaskResponse {
    public String imageName;
    public List<Prediction> predictions;
}


