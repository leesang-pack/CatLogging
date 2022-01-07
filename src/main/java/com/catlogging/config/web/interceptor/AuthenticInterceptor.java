package com.catlogging.config.web.interceptor;


import com.catlogging.interceptor.HttpUtil;
import com.catlogging.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthenticInterceptor implements HandlerInterceptor {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean authenticated = SecurityUtil.isAuthenticated();
        if (authenticated == false) {
            log.debug(request.getRequestURL().toString());
            log.debug("================== AuthenticInterceptor preHandle Failed ==================");
            return false;
        }
        log.debug("================== AuthenticInterceptor preHandle Suc. ==================");

        /*
         * url 확장자 체크
         *
         */

        /*
        String url = request.getRequestURL().toString();
        int offset = url.lastIndexOf("/");
        String path = url.substring(offset, url.length());

        if (path.indexOf('.') > -1) {
            throw new ResourceNotFoundException();
        }
        */



        /*
         * TODO menu 권한 check
         */

        /*
         * TODO 실행 url 권한?? check
         */

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            ModelMap modelMap = modelAndView.getModelMap();
            MediaType mediaType = HttpUtil.getMediaType(request.getContentType());

            if (mediaType == MediaType.APPLICATION_JSON  || mediaType == MediaType.APPLICATION_FORM_URLENCODED) {
                modelMap.addAttribute("ajaxSuccess", true);
            } else if (mediaType == MediaType.TEXT_HTML) {
                modelMap.addAttribute("CONTEXT", request.getContextPath());
                modelMap.addAttribute("SERVLET_PATH", request.getServletPath());
                modelMap.addAttribute("URI", request.getRequestURI());

                log.debug("url ::::: {}", request.getRequestURL());
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
