package com.FinalYear.filter;

import com.FinalYear.service.JwtService;
import com.FinalYear.service.MyUserDetailsService;


import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {


    @Autowired
    JwtService jwtService;

    @Autowired
    ApplicationContext applicationContext;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader("Authorization");
        String token=null;
        String username=null;


        if(authHeader!=null && authHeader.startsWith("Bearer ")) {		//Headers have bearer,basic,digest etc. authentication types and they are written at the start of token so we need to remove them. JWT is bearer token
            token=authHeader.substring(7);
            username=jwtService.extractUsername(token);
            System.out.println("username:"+username);
        }

        System.out.println("username:"+username);

        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {//If username is not null and the request is not already authorised then execute below

            //We could have used @Autowired userDetails but the tutor said it will cause cyclic dependency problem
            UserDetails userDetails=applicationContext.getBean(MyUserDetailsService.class).loadUserByUsername(username);	//We want to get the userDetails of user whose in token so that we can compare the password so created MyUserDetailsService class object and called its loadUserByUsername method
            if(jwtService.validateToken(token,userDetails)) {
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                //Above three lines set the information of token in SecurityContext So that next authentication filters can skip authenticating again
            }
        }
        filterChain.doFilter(request,response);
    }
}
