//package com.catlogging.config.web;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//
//@Slf4j
//@Component
//public class CustomAuthenticationProvider implements AuthenticationProvider {
//
////    @Autowired
////    @Qualifier("ADAuthentication")
////    IAuthentication auth;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        log.debug("============================== CustomAuthenticationProvider ==============================");
//        String username = (String) authentication.getPrincipal();
//        String password = (String) authentication.getCredentials();
//
//        UserDetails userInfo = userDetailsService.loadUserByUsername(username);
//
//        return new UsernamePasswordAuthenticationToken(userInfo, passwordEncoder.encode(password), new ArrayList<>());
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return authentication.equals(UsernamePasswordAuthenticationToken.class);
//    }
//}
