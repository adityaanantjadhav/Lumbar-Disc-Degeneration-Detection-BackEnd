package com.FinalYear.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    private int pincode;
    private String city;

    @Enumerated(EnumType.STRING)
    private Role role; // DOCTOR or PATIENT


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Result> results = new ArrayList<>();


    // Only for patient users
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;

    // Only for doctor users
    // In User entity, add orphanRemoval to patients
    @OneToMany(mappedBy = "doctor")
    private List<User> patients = new ArrayList<>();


    //Only for doctors
    @OneToMany(mappedBy="doctor",orphanRemoval = true, cascade=CascadeType.ALL)
    private List<PatientRequest> incomingRequests = new ArrayList<>();;
    // getters and setters
}

