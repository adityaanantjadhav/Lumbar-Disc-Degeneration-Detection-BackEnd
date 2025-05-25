package com.FinalYear.service;


import com.FinalYear.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;


@Service
public class JwtService {

    private String secretKey;

    JwtService(){
        try {
            KeyGenerator keygen=KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk=keygen.generateKey();
            secretKey= Base64.getEncoder().encodeToString(sk.getEncoded());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public String generateToken(String username) {
        HashMap<String, Object> claims = new HashMap<>();


        System.out.println("generateToken Username:"+username);

        LocalDateTime ldt = LocalDateTime.now();
        Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        LocalDateTime sldt = ldt.plusMinutes(1);
        Date expiration = Date.from(sldt.atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .and()
                .signWith(getKey())
                .compact();

    }

    public SecretKey getKey(){
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractUsername(String token) {			//This function extracts username using extractClaims function
        return extractClaims(token,Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimResolver) {
        // TODO Auto-generated method stub
        final Claims claims=extractAllClaims(token);

        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }


    public boolean validateToken(String token, UserDetails userDetails) {
        // TODO Auto-generated method stub
        final String username=extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));  //if username in token is same as username in Database and token is not expired
    }

    private boolean isTokenExpired(String token) {
        // TODO Auto-generated method stub
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token,Claims::getExpiration);
    }



}
