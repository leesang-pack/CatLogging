package com.catlogging.web;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

public class CustomServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        // DEBUG xzpluszone
        // http://localhost:8082/c/sources
        //res.getWriter().print("hello");
    }
}