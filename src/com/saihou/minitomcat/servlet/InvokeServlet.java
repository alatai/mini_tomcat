package com.saihou.minitomcat.servlet;

import cn.hutool.core.util.ReflectUtil;
import com.saihou.minitomcat.catalina.Context;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.util.Constant;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet処理-ダイナミックリソース
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/21 15:37
 */
public class InvokeServlet extends HttpServlet {

    private static final InvokeServlet DYNAMIC_SERVLET = new InvokeServlet();

    // singleton
    private InvokeServlet() {

    }

    public static InvokeServlet getInstance() {
        return DYNAMIC_SERVLET;
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        try {
            Class<?> servletClass = context.getWebAppClassLoader().loadClass(servletClassName);
            Object servletObject = context.getServlet(servletClass);
            ReflectUtil.invoke(servletObject, "service", request, response);

            response.setStatus(Constant.CODE_200);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
