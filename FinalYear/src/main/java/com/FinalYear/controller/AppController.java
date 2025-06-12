package com.FinalYear.controller;


import com.FinalYear.Dto.DoctorResponseDto;
import com.FinalYear.Dto.LoginRequest;
import com.FinalYear.Dto.PatientDto;
import com.FinalYear.Dto.ResultResponseDto;
import com.FinalYear.entity.PatientRequest;
import com.FinalYear.entity.Result;
import com.FinalYear.service.AppService;
import com.FinalYear.entity.User;
import com.FinalYear.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5501", exposedHeaders = "Authorization")
public class AppController {

    @Autowired
    AppService appService;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private com.FinalYear.service.MyUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        String jwt = jwtService.generateToken(request.getUsername());
        System.out.println("JWT:"+jwt);
        return ResponseEntity.ok().header("Authorization", "Bearer " + jwt).body("Login successful");
    }

    @GetMapping()
    public ResponseEntity<?> getUser(int userId){

        return null;

    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user){

        return new ResponseEntity<User>(appService.createUser(user),HttpStatus.CREATED);
    }


    @PostMapping("/analyse-image")
    public ResponseEntity<?> analyseImage(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String token,
                                          @RequestParam("viewType") String viewType) throws IOException {

        token=token.substring(7);
        String userEmail = jwtService.extractUsername(token);


        File tempFile = File.createTempFile("upload_", file.getOriginalFilename());
        file.transferTo(tempFile);



        Long resultId=appService.analyseImage(userEmail,tempFile,viewType);

        Map<String,Long> map=new HashMap<>();
        map.put("resultId",resultId);
        tempFile.delete();

        if(resultId==-1)
                return new ResponseEntity<>("Some error occured",HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<Map<String, Long>>(map, HttpStatus.OK);
    }

    @GetMapping("/get-result/{resultId}")
    public ResponseEntity<?> getResult(@PathVariable Long resultId,@RequestHeader("Authorization")String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<ResultResponseDto>(appService.getResult(resultId,userEmail),HttpStatus.OK);
    }


    @GetMapping("/get-image/{resultId}")
    public ResponseEntity<?> getImage(@PathVariable Long resultId,@RequestHeader("Authorization")String token) throws IOException {
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return appService.getImage(resultId,userEmail);
    }

    @GetMapping("/get-doctors-city")
    public ResponseEntity<?> getDoctorsByCity(@RequestParam String city){
        return new ResponseEntity<List<DoctorResponseDto>>(appService.getDoctorsByCity(city),HttpStatus.OK);
    }

    @GetMapping("/get-doctors-pincode")
    public ResponseEntity<?> getDoctorsByPincode(@RequestParam int pincode){
        return new ResponseEntity<List<DoctorResponseDto>>(appService.getDoctorsByPincode(pincode),HttpStatus.OK);
    }


    @PostMapping("/mail-results")
    public ResponseEntity<?> mailResults(@RequestParam String email,@RequestParam Long resultId){
        int index=email.indexOf('@');
        if(index==-1 || (!email.substring(index+1).equals("gmail.com")&& !email.substring(index+1).equals("yahoo.com")&&!email.substring(index+1).equals("outlook.com"))){
            return new ResponseEntity<String>("Wrong email entered",HttpStatus.BAD_REQUEST);
        }

        appService.mailResult(email,resultId);
        return new ResponseEntity<String>("Mail Successfully sent",HttpStatus.OK);
    }

    @PostMapping("/mail-doctor-results")
    public ResponseEntity<?> mailDoctorResults(@RequestHeader("Authorization") String token,@RequestParam Long resultId){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);

        if(appService.mailResultToDoctor(userEmail,resultId)==null){
            return null;
        }
        return new ResponseEntity<String>("Mail Successfully sent",HttpStatus.OK);
    }



    @GetMapping("/get-previous-results")
    public ResponseEntity<?> getPreviousResults(@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail = jwtService.extractUsername(token);
        return new ResponseEntity<List<ResultResponseDto>>(appService.getPreviousResults(userEmail),HttpStatus.OK);
    }



    @GetMapping("/is-doctor")
    public ResponseEntity<?> isDoctor(@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<Boolean>(appService.isDoctor(userEmail),HttpStatus.OK);
    }


    @GetMapping("/get-patients")
    public ResponseEntity<?> getPatients(@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<List<PatientDto>>(appService.getPatientList(userEmail),HttpStatus.OK);
    }

    @GetMapping("/get-patient-requests")
    public ResponseEntity<?> getPatientRequests(@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<List<PatientRequest>>(appService.getPatientRequests(userEmail),HttpStatus.OK);
    }



    @GetMapping("/get-patient-result/{id}")
    public ResponseEntity<?> getPatientResult(@PathVariable("id") String id,@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<List<ResultResponseDto>>(appService.getPatientPreviousResults(userEmail,id),HttpStatus.OK);
    }

    @GetMapping("/get-patient-info/{id}")
    public ResponseEntity<?> getPatientInfo(@PathVariable("id") String id,@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<PatientDto>(appService.getPatientInfo(userEmail,id),HttpStatus.OK);
    }


    @PostMapping("/add-patient")
    public ResponseEntity<?> addPatient(@RequestHeader("Authorization") String token,@RequestParam("patientEmail") String patientEmail){
        token=token.substring(7);
        String doctorEmail=jwtService.extractUsername(token);
        return new ResponseEntity<PatientDto>(appService.addPatient(doctorEmail,patientEmail),HttpStatus.OK);
    }

    @DeleteMapping("/reject-patient")
    public ResponseEntity<?> rejectPatient(@RequestHeader("Authorization") String token,@RequestParam("patientEmail") String patientEmail){
        token=token.substring(7);
        String doctorEmail=jwtService.extractUsername(token);
        return new ResponseEntity<String>(appService.rejectPatient(doctorEmail,patientEmail),HttpStatus.OK);
    }

    @DeleteMapping("/remove-patient")
    public ResponseEntity<?> removePatient(
            @RequestHeader("Authorization") String token,
            @RequestParam("patientEmail") String patientEmail) { // Keep param name consistent

        token = token.replace("Bearer ", ""); // More robust than substring(7)
        String doctorEmail = jwtService.extractUsername(token);
        return new ResponseEntity<>(appService.removePatient(doctorEmail, patientEmail), HttpStatus.OK);
    }


    @PostMapping("/request-doctor")
    public ResponseEntity<?> requestDoctor(@RequestHeader("Authorization") String token,@RequestParam("doctorEmail") String doctorEmail){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<String>(appService.requestDoctor(userEmail,doctorEmail),HttpStatus.OK);
    }

    @GetMapping("/get-doctor-details")
    public ResponseEntity<?> getDoctorDetails(@RequestHeader("Authorization") String token){
        token=token.substring(7);
        String userEmail=jwtService.extractUsername(token);
        return new ResponseEntity<PatientDto>(appService.getDoctorDetails(userEmail),HttpStatus.OK);
    }

}
