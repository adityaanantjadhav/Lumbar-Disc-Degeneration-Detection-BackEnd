    package com.FinalYear.Dto;

    import jakarta.persistence.Embeddable;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public class Prediction{
        String label;
        double confidence;
    }
