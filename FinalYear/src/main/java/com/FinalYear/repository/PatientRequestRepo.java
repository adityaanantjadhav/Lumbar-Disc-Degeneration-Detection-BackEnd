package com.FinalYear.repository;


import com.FinalYear.entity.PatientRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRequestRepo extends JpaRepository<PatientRequest,Long> {

    PatientRequest findByEmail(String email);
}
