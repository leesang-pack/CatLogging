package com.catlogging.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
public class ThymeleafUtil {

    public void removeSession(String sessionAttributeKey) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession();
            session.removeAttribute(sessionAttributeKey);
        } catch (Exception ex) {
            log.error("Thymeleaf removeSession error: ", ex);
        }
    }


    public void removeSessionLastSecurityException() {
        removeSession("SPRING_SECURITY_LAST_EXCEPTION");
    }

}
