package com.catlogging.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("============================== AuthenticationFailureHandler ==============================");

//        String errorCode = AdCode.AD.C_AD_STATUS_49.CdStr();
        AuthenticationException authFailException = exception;

        if(exception instanceof BadCredentialsException) {                              //비밀번호가 일치하지 않을 때 던지는 예외
            authFailException = new BadCredentialsException("ID/비밀번호가 일치하지 않습니다.");
        } else if(exception instanceof InternalAuthenticationServiceException) {        //존재하지 않는 아이디일 때 던지는 예외
            authFailException = new InternalAuthenticationServiceException("ID/비밀번호가 일치하지 않습니다.");
        } else if(exception instanceof DisabledException) {                             //인증 거부 - 계정 비활성화
            authFailException = new DisabledException("ID/비밀번호가 일치하지 않습니다.");
        } else if(exception instanceof CredentialsExpiredException) {                   //인증 거부 - 비밀번호 유효기간 만료
            authFailException = new CredentialsExpiredException("계정이 만료되었습니다.");
//            errorCode = AdCode.AD.C_AD_STATUS_775.CdStr();
        } else if (exception instanceof SessionAuthenticationException) {
            authFailException = new SessionAuthenticationException("이미 로그인한 사용자가 있습니다.");
        }


        try {
            Map<String, Object> param = new HashMap<>();
//            String userId = request.getParameter("username");
//            String tenantId = request.getParameter("tenantId");
//            param.put("userId", userId);
//            param.put("tenantId", tenantId);
//            param.put("adDomain", commonService.selectAdInfo(userId, tenantId).getAdDomain());

//            commonService.updateUserLoginFailCount(param);
        } catch (Exception ex) {
            log.error("update user login fail count error : {}", ex);
        }


        // 로그인 실패후 이동할 url
//        setDefaultFailureUrl("/login?error=true");
        super.onAuthenticationFailure(request, response, authFailException);


    }
}
