package com.saihou.minitomcat.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.saihou.minitomcat.catalina.Context;
import com.saihou.minitomcat.classloader.JspClassLoader;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.util.Constant;
import com.saihou.minitomcat.util.JspUtil;
import com.saihou.minitomcat.util.WebXmlUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * JSP解析した後のServletを処理する
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/12/10 12:00
 */
public class JspServlet extends HttpServlet {

    private static final JspServlet JSP_SERVLET = new JspServlet();

    private JspServlet() {

    }

    public static JspServlet getInstance() {
        return JSP_SERVLET;
    }

    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getUri();

            if (uri.equals("/")) {
                uri = WebXmlUtil.getWelcomeFile(request.getContext());
            }

            String filename = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(request.getRealPath(filename));

            if (file.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;

                if (path.equals("/")) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, "/", false);
                }

                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);

                // 既に存在しているかを判断する
                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, file);
                } else if (file.lastModified() > jspServletClassFile.lastModified()) {
                    JspUtil.compileJsp(context, file);
                    // 古いクラスファイルとJspClassLoaderの関連を削除する
                    JspClassLoader.invalidateClassLoader(uri, context);
                }

                String extName = FileUtil.extName(file);
                String mimeType = WebXmlUtil.getMimeType(extName);
                response.setContentType(mimeType);

                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);
                Class<?> jspServletClass = jspClassLoader.loadClass(jspServletClassName);

                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request, response);
                response.setStatus(Constant.CODE_200);
            } else {
                response.setStatus(Constant.CODE_404);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
