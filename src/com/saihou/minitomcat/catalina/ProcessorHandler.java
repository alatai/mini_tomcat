package com.saihou.minitomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.servlet.JspServlet;
import com.saihou.minitomcat.servlet.StaticServlet;
import com.saihou.minitomcat.servlet.DynamicServlet;
import com.saihou.minitomcat.util.Constant;
import com.saihou.minitomcat.util.SessionManager;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 接続処理
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 21:56
 */
public class ProcessorHandler {

    public void execute(Socket socket, Request request, Response response) {
        try {
            String uri = request.getUri();

            if (uri == null) {
                return;
            }

            prepareSession(request, response);

            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);

            // Servlet処理
            if (servletClassName != null) {
                DynamicServlet.getInstance().service(request, response);
            } else if (uri.endsWith(".jsp")) { // jsp処理
                JspServlet.getInstance().service(request, response);
            } else { // 静的なリソース
                StaticServlet.getInstance().service(request, response);
            }

            // HTTP状態コードで静的な資源を処理する
            switch (response.getStatus()) {
                case Constant.CODE_200:
                    handle200(socket, response);
                    break;
                case Constant.CODE_404:
                    handle404(socket, uri);
                    break;
            }
        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(socket, e);
        }
    }

    /**
     * sessionの処理、requestに入れる
     */
    private void prepareSession(Request request, Response response) {
        String jsessionid = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jsessionid, request, response);
        request.setSession(session);
    }

    /**
     * HTTPの200レスポンス
     */
    private static void handle200(Socket socket, Response response) {
        try (OutputStream out = socket.getOutputStream()) {
            String contentType = response.getContentType();
            String cookiesHeader = response.getCookiesHeader();
            // レスポンスボディーのバイト配列
            byte[] responseBodyBytes = response.getBody();

            String headText = Constant.RESPONSE_HEADER_200;
            headText = StrUtil.format(headText, contentType, cookiesHeader);

            // レスポンスヘッドのバイト配列
            byte[] headBytes = headText.getBytes();
            // レスポンスバイト配列
            byte[] responseBytes = new byte[headBytes.length + responseBodyBytes.length];

            ArrayUtil.copy(headBytes, 0, responseBytes, 0, headBytes.length);
            ArrayUtil.copy(responseBodyBytes, 0, responseBytes, headBytes.length, responseBodyBytes.length);

            // 請求先にレスポンスする
            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * HTTPの404レスポンス
     */
    private void handle404(Socket socket, String uri) {
        try (OutputStream out = socket.getOutputStream()) {
            String responseContent = StrUtil.format(Constant.TEXT_FORMAT_404, uri, uri);
            responseContent = Constant.RESPONSE_HEADER_404 + responseContent;

            byte[] responseBytes = responseContent.getBytes();

            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * HTTPの500レスポンス
     */
    private void handle500(Socket socket, Exception exception) {
        try (OutputStream out = socket.getOutputStream()) {
            // 例外スタック
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(exception);
            sb.append("\r\n");

            for (StackTraceElement element : stackTraceElements) {
                sb.append("\t");
                sb.append(element.toString());
                sb.append("\r\n");
            }

            String msg = exception.getMessage();
            if (msg != null && msg.length() > 20) {
                msg = msg.substring(0, 19);
            }

            String responseContent = StrUtil.format(Constant.TEXT_FORMAT_500, msg, exception.toString(), sb.toString());
            responseContent = Constant.RESPONSE_HEADER_500 + responseContent;
            byte[] responseBytes = responseContent.getBytes();

            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
