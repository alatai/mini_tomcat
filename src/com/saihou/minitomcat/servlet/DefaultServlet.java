package com.saihou.minitomcat.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.saihou.minitomcat.catalina.Context;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.util.Constant;
import com.saihou.minitomcat.util.WebXmlUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 静的な資源（HTML、CSS..）
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/21 18:09
 */
public class DefaultServlet extends HttpServlet {

    private static final DefaultServlet STATIC_SERVLET = new DefaultServlet();

    private DefaultServlet() {

    }

    public static DefaultServlet getInstance() {
        return STATIC_SERVLET;
    }

    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        Context context = request.getContext();
        String uri = request.getUri();

        if ("/500.html".equals(uri)) {
            throw new RuntimeException("This is a deliberately created exception.");
        }

        if ("/".equals(uri)) {
            uri = WebXmlUtil.getWelcomeFile(request.getContext());
        }

        if (uri.endsWith(".jsp")) {
            JspServlet.getInstance().service(request, response);
            return;
        }

        String filename = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(request.getRealPath(filename));

        // 複数のフォルダの場合
        if (!file.isFile()) {
            uri = uri + "/" + WebXmlUtil.getWelcomeFile(request.getContext());
            filename = StrUtil.removePrefix(uri, "/");
            file = new File(context.getDocBase(), filename);
        }

        if (file.exists()) {
            String extName = FileUtil.extName(file);
            String mimeType = WebXmlUtil.getMimeType(extName);
            response.setContentType(mimeType);

            byte[] body = FileUtil.readBytes(file);
            response.setBody(body);
            response.setStatus(Constant.CODE_200);
        } else {
            response.setStatus(Constant.CODE_404);
        }
    }
}
