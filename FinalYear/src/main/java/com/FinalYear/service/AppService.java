package com.FinalYear.service;


import com.FinalYear.Dto.*;
import com.FinalYear.entity.PatientRequest;
import com.FinalYear.entity.Result;
import com.FinalYear.entity.Role;
import com.FinalYear.entity.User;
import com.FinalYear.repository.PatientRequestRepo;
import com.FinalYear.repository.ResultRepository;
import com.FinalYear.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AppService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepo;

     @Autowired
     private ResultRepository resultRepo;

     @Autowired
     private PatientRequestRepo patientRequestRepo;


     @Autowired
     private EmailService emailService;


     @Autowired
     private BCryptPasswordEncoder bCryptPasswordEncoder;

    public long analyseImage(String userEmail, File file, String viewType){

        String url="http://localhost:5000/predict";

        HttpHeaders headers=new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        String userEmailWithoutExtension =removeAtFromMailId(userEmail);


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new FileSystemResource(file));
        body.add("userId", userEmailWithoutExtension);
        body.add("viewType", viewType);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send POST request
        ResponseEntity<FlaskResponse> response = restTemplate.postForEntity(url, requestEntity, FlaskResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            FlaskResponse responseBody = response.getBody();

            if(responseBody==null) return -1;

            Result result=new Result();


            result.setPredictions(responseBody.getPredictions());

            result.setImageName(responseBody.getImageName());


            User user=userRepo.findByEmail(userEmail);
            result.setUser(user);

            result.setTimestamp(LocalDateTime.now());



            result=resultRepo.save(result);
            user.getResults().add(result);
            userRepo.save(user);
            //Saving Result in Database and getting its id to be sent to frontend so that it can be used to send email
            return result.getResultId();
        }
        else {
            System.out.println("Flask API error: " + response.getStatusCode());
            return -1;
        }
    }


    private String removeAtFromMailId(String mailId){
        int index=mailId.indexOf('@');
        return mailId.substring(0,index);
    }

    public List<DoctorResponseDto> getDoctorsByCity(String city){
        city=city.toLowerCase();
        return userRepo.findDoctorsByCity(city);
    }

    public List<DoctorResponseDto> getDoctorsByPincode(int pincode) {
        return userRepo.findDoctorsByPincode(pincode-10,pincode+10);
    }


    public void mailResult(String email,Long resultId){

        Result result=resultRepo.findResultByResultId(resultId);
        emailService.sendResultOnEmail(email,result);
    }


    public ResultResponseDto getResult(Long resultId,String userEmail){

        User user=userRepo.findByEmail(userEmail);
        Result result=resultRepo.findResultByResultId(resultId);

        if(!result.getUser().getId().equals(user.getId()) && !user.getPatients().contains(result.getUser())){
            throw new RuntimeException("Unauthorized");
        }

        List<Prediction> prediction=result.getPredictions();
        int noOfLabels=prediction.size();

        ArrayList<String> labels=new ArrayList<>(getUniqueLabels(prediction));
        HashMap<String,String> predictedConditionDetails=enterConditionDetails(labels);



        ResultResponseDto resultResponseDto=new ResultResponseDto();

        resultResponseDto.setPredictedConditions(predictedConditionDetails);
        resultResponseDto.setPredictedLocationCount(noOfLabels);
        resultResponseDto.setResultId(result.getResultId());
        resultResponseDto.setTimestamp(result.getTimestamp());
        resultResponseDto.setPredictions(result.getPredictions());
        resultResponseDto.setImageName(result.getImageName());

            return resultResponseDto;
    }

    private HashMap<String, String> enterConditionDetails(ArrayList<String> labels) {
        HashMap<String,String> map=new HashMap<>();

        for (String i:labels){
            if(i.equals("SCN")){
                map.put("Spinal Canal Narrowing","This condition narrows the space within your spine, which can put pressure on the nerves.");
            }
            else if(i.equals("LNFN")){
                map.put("Left Neural Foraminal Narrowing","Narrowing of the space on the left side where nerves exit the spinal canal, causing possible nerve compression.");
            }
            else if(i.equals("RNFN")){
                map.put("Right Neural Foraminal Narrowing","Narrowing of the space on the right side where nerves exit, potentially causing pain or numbness.");
            }
            else if(i.equals("LSS")){
                map.put("Left Subarticular Stenosis","Narrowing occurring just inside the spinal canal on the left, often leading to nerve pinching.");
            }
            else{
                map.put("Right Subarticular Stenosis","Narrowing occurring just inside the spinal canal on the right, often leading to nerve pinching.");
            }
        }
        return map;
    }

    private HashSet<String> getUniqueLabels(List<Prediction> splited) {
        HashSet<String> set=new HashSet<>();
        for (Prediction i:splited){
            set.add(i.getLabel());
        }
        return set;
    }

    public ResponseEntity<?> getImage(Long resultId,String userEmail) throws IOException {

        User user=userRepo.findByEmail(userEmail);

        Result result=resultRepo.findResultByResultId(resultId);

        if(!result.getUser().getId().equals(user.getId()) && !user.getPatients().contains(result.getUser())){
            throw new RuntimeException("Unauthorized");
        }

        String imagePath = "C:/yolo11/frontend and flask/static/processed_images/" + result.getImageName();


        // Read image into byte array
        File imgFile = new File(imagePath);

        if (!imgFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(imgFile.toPath());

        // Detect content type
        String contentType = Files.probeContentType(imgFile.toPath());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageBytes);

    }


    public List<ResultResponseDto> getPreviousResults(String userEmail) {
        User user=userRepo.findByEmail(userEmail);

        List<ResultResponseDto> list=resultRepo.findByUserId(user.getId());

        return list;
    }

    public User createUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public Boolean isDoctor(String userEmail){
        User user=userRepo.findByEmail(userEmail);
        if(user.getRole().equals(Role.DOCTOR)){
            return true;
        }
        return false;
    }


    public List<PatientDto> getPatientList(String userEmail){
        User user=userRepo.findByEmail(userEmail);
        if(user.getRole().equals(Role.PATIENT)){
            throw new RuntimeException("Unauthorized Access! You are not a doctor");
        }
        List<PatientDto> patientList=userRepo.findAllPatients(user.getPatients().stream().map((a)->a.getId()).collect(Collectors.toList()));

        return patientList;
    }

    public List<ResultResponseDto> getPatientPreviousResults(String userEmail, String patientId) {

        User user=userRepo.findByEmail(userEmail);
        Optional<User> patientOpt = user.getPatients().stream()
                .filter(p -> p.getEmail().equals(patientId))
                .findFirst();

        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found under this user.");
        }

        return getPreviousResults(patientOpt.get().getEmail());
    }

    @Transactional
    public PatientDto addPatient(String doctorEmail, String patientEmail) {

        User doctor=userRepo.findByEmail(doctorEmail);
        User patient=userRepo.findByEmail(patientEmail);
        doctor.getPatients().add(patient);

        PatientRequest p=patientRequestRepo.findByEmail(patientEmail);
        doctor.getIncomingRequests().remove(p);
        p.setApproved(true);
        patientRequestRepo.save(p);

        patient.setDoctor(doctor);
        userRepo.save(doctor);
        userRepo.save(patient);
        PatientDto patientDto=new PatientDto(patient);
        return patientDto;
    }

    @Transactional
    public String rejectPatient(String doctorEmail, String patientEmail) {

        User user=userRepo.findByEmail(doctorEmail);
        List<PatientRequest>patientRequestList=user.getIncomingRequests();

        PatientRequest p=patientRequestList.stream().filter(a->a.getEmail().equals(patientEmail)).findFirst().get();

        patientRequestList.remove(p);
        user.setIncomingRequests(patientRequestList);
        userRepo.save(user);
        return "Request reject";
    }

    @Transactional
    public String removePatient(String doctorEmail, String patientEmail) {
        User doctor = userRepo.findByEmail(doctorEmail);
        User patient = userRepo.findByEmail(patientEmail); // Directly fetch patient

        // Verify relationship
        if(patient == null || !patient.getDoctor().getEmail().equals(doctorEmail)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found under your care");
        }

        // Remove bidirectional relationship
        doctor.getPatients().remove(patient);
        patient.setDoctor(null);

        // No need to save - transactional handles persistence
        return "Patient removed";
    }

    public String requestDoctor(String userEmail, String doctorEmail) {

        User user=userRepo.findByEmail(userEmail);
        User doctor=userRepo.findByEmail(doctorEmail);

        PatientRequest pr=new PatientRequest();
        pr.setName(user.getName());
        pr.setEmail(user.getEmail());
        pr.setDoctor(doctor);
        pr.setPatient(user);
        pr.setRequestedAt(Instant.now());
        pr.setApproved(false);

        pr=patientRequestRepo.save(pr);
        doctor.getIncomingRequests().add(pr);
        userRepo.save(doctor);

        return "Request Successfully sent";
    }

    public PatientDto getPatientInfo(String userEmail, String id) {
        User doctor=userRepo.findByEmail(userEmail);
        User patient=userRepo.findByEmail(id);
        if(!doctor.getPatients().contains(patient)) throw new RuntimeException("Unauthorized");

        return new PatientDto(patient);

    }

    public List<PatientRequest> getPatientRequests(String userEmail) {
        User doctor=userRepo.findByEmail(userEmail);
        return doctor.getIncomingRequests();
    }


    public PatientDto getDoctorDetails(String userEmail) {
        User patient = userRepo.findByEmail(userEmail);
        User doctor = patient.getDoctor();
        if(doctor==null) return null;
        PatientDto doctorDto = new PatientDto(doctor);
        return doctorDto;
    }

    public String mailResultToDoctor(String email,Long resultId){
        User patient=userRepo.findByEmail(email);
        if(patient.getDoctor()==null) return null;
        String doctorMail=patient.getDoctor().getEmail();
        mailResult(doctorMail,resultId);
        return "success";
    }
}
