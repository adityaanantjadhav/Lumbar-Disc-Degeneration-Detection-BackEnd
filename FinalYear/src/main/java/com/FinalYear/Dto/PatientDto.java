package com.FinalYear.Dto;


import com.FinalYear.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private String name;
    private String email;
    private int pincode;
    private String city;

    public PatientDto(User user){
        this.id= user.getId();
        this.name=user.getName();
        this.email=user.getEmail();
        this.city=user.getCity();
        this.pincode=user.getPincode();
    }
}
