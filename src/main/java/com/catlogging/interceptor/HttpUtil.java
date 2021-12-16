package com.catlogging.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtil {

    public static HttpHeaders getHttpHeaders(String contentType) {
        HttpHeaders headers = new HttpHeaders();


        if (contentType == null) {
            headers.set("Content-Type", MediaType.TEXT_HTML_VALUE);
            return headers;
        }

        contentType = contentType.toLowerCase();

        if (contentType.indexOf("json") > -1 || contentType.indexOf("multipart") > -1) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        } else if (contentType.indexOf("xml") > -1) {
            headers.setContentType(MediaType.APPLICATION_XML);
        } else {
            headers.setContentType(MediaType.TEXT_HTML);
        }

        return headers;
    }


    public static MediaType getMediaType(String contentType) {
        if (contentType == null) {
            return MediaType.TEXT_HTML;
        }
        contentType = contentType.toLowerCase();

        if (contentType.indexOf("json") > -1 || contentType.indexOf("multipart") > -1) {
            return MediaType.APPLICATION_JSON;
        } else if (contentType.indexOf("x-www-form-urlencoded") > -1) {
            return MediaType.APPLICATION_FORM_URLENCODED;
        } else if (contentType.indexOf("xml") > -1) {
            return MediaType.APPLICATION_XML;
        } else {
            return MediaType.TEXT_HTML;
        }

    }

}
