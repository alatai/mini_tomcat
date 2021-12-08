package com.saihou.minitomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletConfig
 * Servlet初期化のパラメーターなどが含まれている
 * コンテナーはServlet対象を作成する時、
 * そのServletに対するServletConfig対象を作成する
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/27 20:30
 */
public class MiniServletConfig implements ServletConfig {

    private final ServletContext servletContext;
    private final String servletName;
    private Map<String, String> initParameters;

    public MiniServletConfig(ServletContext servletContext, String servletName, Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.servletName = servletName;
        this.initParameters = initParameters;

        if (this.initParameters == null) {
            this.initParameters = new HashMap<>();
        }
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }
}
