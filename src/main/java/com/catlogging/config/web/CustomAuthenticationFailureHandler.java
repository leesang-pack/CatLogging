package com.catlogging.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    MessageSource messageSource;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("============================== AuthenticationFailureHandler ==============================");

        AuthenticationException authFailException = exception;

        if(exception instanceof BadCredentialsException) {                              //비밀번호가 일치하지 않을 때 던지는 예외
            authFailException = new BadCredentialsException(messageSource.getMessage("catlogging.member.auth.bed", new String[]{}, LocaleContextHolder.getLocale()));
        } else if(exception instanceof InternalAuthenticationServiceException) {        //존재하지 않는 아이디일 때 던지는 예외
            authFailException = new InternalAuthenticationServiceException(messageSource.getMessage("catlogging.member.auth.bed", new String[]{}, LocaleContextHolder.getLocale()));
        } else if(exception instanceof DisabledException) {                             //인증 거부 - 계정 비활성화
            authFailException = new DisabledException(messageSource.getMessage("catlogging.member.auth.bed", new String[]{}, LocaleContextHolder.getLocale()));
        } else if(exception instanceof CredentialsExpiredException) {                   //인증 거부 - 비밀번호 유효기간 만료
            authFailException = new CredentialsExpiredException(messageSource.getMessage("catlogging.member.auth.expired", new String[]{}, LocaleContextHolder.getLocale()));
        } else if (exception instanceof SessionAuthenticationException) {               //인증 거부 - 세션중복 거부
            authFailException = new SessionAuthenticationException(messageSource.getMessage("catlogging.member.auth.sessionDup", new String[]{}, LocaleContextHolder.getLocale()));
        }

        //TODO : 실패 시 DB count

        // 로그인 실패후 이동할 url
        setDefaultFailureUrl("/login?error=true");
        super.onAuthenticationFailure(request, response, authFailException);

    }
}
