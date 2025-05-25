package com.FinalYear.repository;


import com.FinalYear.Dto.DoctorResponseDto;
import com.FinalYear.Dto.PatientDto;
import com.FinalYear.entity.Result;
import com.FinalYear.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User ,Long> {
    User findByEmail(String userEmail);

    @Query("SELECT new com.FinalYear.Dto.DoctorResponseDto(u.name, u.email, u.pincode, u.city) " +
            "FROM User u " +
            "WHERE u.role = com.FinalYear.entity.Role.DOCTOR AND u.city = :city")
    List<DoctorResponseDto> findDoctorsByCity(@Param("city") String city);


    @Query("SELECT new com.FinalYear.Dto.DoctorResponseDto(u.name, u.email, u.pincode, u.city) " +
            "FROM User u " +
            "WHERE u.role = com.FinalYear.entity.Role.DOCTOR AND u.pincode BETWEEN :start AND :end")
    List<DoctorResponseDto> findDoctorsByPincode(@Param("start") int start, @Param("end") int end);


    @Query("SELECT new com.FinalYear.Dto.PatientDto(u.id, u.name, u.email, u.pincode, u.city) " +
            "FROM User u WHERE u.id IN :userIdList")
    List<PatientDto> findAllPatients(@Param("userIdList") List<Long> userIdList);
}
