package com.saihou.minitomcat.test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servletテスト用
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/21 13:02
 */
public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().println("Hello Mini Tomcat from HelloServlet!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
