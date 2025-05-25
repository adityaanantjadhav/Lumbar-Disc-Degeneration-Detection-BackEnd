package com.FinalYear.service;


import com.FinalYear.Dto.Prediction;
import com.FinalYear.entity.Result;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class EmailService {


    @Autowired
    private JavaMailSender javaMailSender;

    public boolean sendResultOnEmail(String email, Result result){

        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart


            List<Prediction> predictions=result.getPredictions();
            String details="Label  ::  Confiedence Score";
            for(Prediction i:predictions) {
                details = i.getLabel() + " ::  " + i.getConfidence() + "\n";
            }


            helper.setFrom("yj26102003@gmail.com");
            helper.setTo(email);
            helper.setSubject("Prediction Result: MRI Image Analysis");
            helper.setText("Time: " + result.getTimestamp() +
                    "\nPrediction:\n" + details, false); // false = not HTML

            // Path where the image is stored
            String imagePath = "C:/yolo11/flask_app/static/processed_images/" + result.getImageName();
            FileSystemResource file = new FileSystemResource(new File(imagePath));
            helper.addAttachment(result.getImageName(), file);

            javaMailSender.send(message);
            return true;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
