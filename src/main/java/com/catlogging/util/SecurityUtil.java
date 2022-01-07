package com.catlogging.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

public class SecurityUtil {

//    public static UserInfo getUserInfo() {
//        SecurityContext context = SecurityContextHolder.getContext();
//        if (context == null) {
//            return null;
//        }
//
//        Authentication authentication = context.getAuthentication();
//        return (UserInfo) authentication.getPrincipal();
//    }
//
//    public static UserInfo getUserInfo(HttpSession session) {
//        Authentication authentication = (Authentication) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
//
//        if (authentication == null) {
//            return null;
//        }
//
//        return (UserInfo) authentication.getPrincipal();
//    }

    public static boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return false;
        }

        Authentication authentication = context.getAuthentication();
        return authentication.isAuthenticated();
    }

}
