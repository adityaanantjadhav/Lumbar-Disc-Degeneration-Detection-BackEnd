package com.FinalYear.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class PatientRequest {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;

    @ManyToOne
    @JoinColumn(name="doctor_id")
    private User doctor;

    @ManyToOne
    @JoinColumn(name="patient_id")
    private User patient;

    private Instant requestedAt;
    private boolean approved;

}
