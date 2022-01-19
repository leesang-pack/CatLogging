/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.catlogging.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
public class AjaxAndHttpAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {


    public AjaxAndHttpAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String ajaxAndHttpHeader = ((HttpServletRequest) request).getHeader("X-Requested-With");
        log.debug("=============>> IS check Js ajaxAndHttpHeader Request?? path:{} header:{} Error:{}", request.getRequestURI(), ajaxAndHttpHeader, authException.getMessage());

        boolean isAjaxAndHttp = "XMLHttpRequest".equals(ajaxAndHttpHeader);
        if (isAjaxAndHttp) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "session invalid");
        } else {
            super.commence(request, response, authException);
        }
    }
}
