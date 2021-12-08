package com.saihou.minitomcat.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.saihou.minitomcat.catalina.Context;
import com.saihou.minitomcat.catalina.Engine;
import com.saihou.minitomcat.catalina.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * リクエスト
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/19 16:00
 */
public class Request extends BaseRequest {

    private String requestString; // リクエスト（全部）
    private String uri;
    private final Socket socket;

    private Context context; // アプリコンテキスト
    private final Service service;

    private String method; // リクエストメソッド、GET/POST
    private String queryString; // リクエストパラメーター
    private final Map<String, String[]> parameterMap;
    private final Map<String, String> headerMap; // リクエストヘッドのメッセージ
    private Cookie[] cookies;
    private HttpSession session;

    public Request(Socket socket, Service service) {
        this.socket = socket;
        this.service = service;
        this.parameterMap = new HashMap<>();
        this.headerMap = new HashMap<>();

        parseHttpRequest();

        if (StrUtil.isEmpty(requestString)) {
            return;
        }

        parseUri();
        parseContext();
        parseMethod();

        // uriを修正する
        if (!"/".equals(context.getPath())) {
            // uri = /a/index.html -> /index.html
            uri = StrUtil.removePrefix(uri, context.getPath());

            // e.g. /a -> /
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }

        parseParameters();
        parseHeaders();
        parseCookies();
    }

    /**
     * HTTPリクエストの文字列を解析する
     */
    private void parseHttpRequest() {
        try {
            InputStream in = socket.getInputStream();
            // bufferSizeより小さいなら、読み込みが停止
            // HTTPの持続的接続なので、自動的に閉じない
            // リクエストからのデータの読み込みが止まってしまう
            byte[] bytes = readBytes(in, false);
            requestString = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 入力ストリームからすべてのデータを読み込む
     *
     * @param in    入力ストリーム
     * @param fully すべてのデータか
     * @return バイト配列
     */
    public static byte[] readBytes(InputStream in, boolean fully) {
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();

        try {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            while (true) {
                int length = in.read(buffer);
                if (-1 == length) {
                    break;
                }

                byteArrayOut.write(buffer, 0, length);
                // fully == trueの時、実際のデータはbufferSizeより小さいでも読込み続ける
                if (!fully && length != bufferSize) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOut.toByteArray();
    }

    /**
     * アプリコンテキストを解析する
     * 請求のURIを分析して、パスを取得して、それをもとにContextを作成する
     * デフォールト“/”、ROOT Context
     */
    private void parseContext() {
        Engine engine = service.getEngine();
        // 先にuriから解析、/aでも請求できる
        context = engine.getDefaultHost().getContext(uri);

        if (context != null) {
            return;
        }

        String path = StrUtil.subBetween(uri, "/", "/");

        if (path == null) {
            path = "/";
        } else {
            path = "/" + path;
        }

        context = engine.getDefaultHost().getContext(path);

        if (context == null) {
            context = engine.getDefaultHost().getContext("/");
        }
    }

    /**
     * URIを解析して、請求パスを取得する
     * GET /index.html?name=green HTTP/1.1 -> /index.html?name=green
     */
    private void parseUri() {
        String temp;
        temp = StrUtil.subBetween(requestString, " ", " ");

        if (!StrUtil.contains(temp, '?')) {
            uri = temp;
            return;
        }

        temp = StrUtil.subBefore(temp, '?', false);
        uri = temp;
    }

    /**
     * リクエストメソッドを解析する
     * e.g. GET /XX/XX HTTP/1.1
     */
    private void parseMethod() {
        this.method = StrUtil.subBefore(requestString, " ", false);
    }

    /**
     * GET/POSTメソッドのパラメーターを解析する
     */
    private void parseParameters() {
        if ("GET".equals(this.getMethod())) {
            String url = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(url, '?')) {
                queryString = StrUtil.subAfter(url, '?', false);
            }
        }

        if ("POST".equals(this.getMethod())) {
            queryString = StrUtil.subAfter(requestString, "\r\n\r\n", false);
        }

        if (queryString == null) {
            return;
        }

        // %の16進数のデータを解析する
        queryString = URLUtil.decode(queryString);
        String[] parameters = queryString.split("&");

        // parameterMapに入れる
        for (String parameter : parameters) {
            String[] nameValue = parameter.split("=");
            String name = nameValue[0];
            String value = nameValue[1];

            String[] values = parameterMap.get(name);

            // name -> value
            // name -> value1, value2...
            if (values == null) {
                values = new String[]{value};
            } else {
                values = ArrayUtil.append(values, value);
            }

            parameterMap.put(name, values);
        }
    }

    /**
     * リクエストヘッドを解析する
     */
    private void parseHeaders() {
        StringReader reader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(reader, lines);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            // 読み込む操作を終了する
            // リクエストヘッドとボディー間に空行がある
            if (line.length() == 0) {
                break;
            }

            String[] nameValue = line.split(":");
            String headerName = nameValue[0].toLowerCase();
            String headerValue = nameValue[1];

            headerMap.put(headerName, headerValue);
        }
    }

    /**
     * リクエストからのCookieを解析する
     */
    private void parseCookies() {
        List<Cookie> cookieList = new ArrayList<>();
        String cookies = headerMap.get("cookie");

        if (cookies != null) {
            List<String> pairs = StrUtil.split(cookies, ":");

            for (String pair : pairs) {
                if (StrUtil.isBlank(pair)) {
                    continue;
                }

                List<String> nameValue = StrUtil.split(pair, "=");
                String name = nameValue.get(0).trim();
                String value = nameValue.get(1).trim();

                Cookie cookie = new Cookie(name, value);
                cookieList.add(cookie);
            }
        }

        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    @Override
    public String getRealPath(String path) {
        return getServletContext().getRealPath(path);
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);

        if (values != null && values.length != 0) {
            return values[0];
        }

        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String getHeader(String name) {
        if (name == null) {
            return null;
        }

        return headerMap.get(name.toLowerCase());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headerMap.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        return Convert.toInt(headerMap.get(name), 0);
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getServerPort() {
        return getLocalPort();
    }

    @Override
    public String getContextPath() {
        String path = this.context.getPath();

        return "/".equals(path) ? "" : path;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();

        if (port < 0) {
            port = 80;
        }

        url.append(scheme);
        url.append("://");
        url.append(getServerName());

        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && (port != 443))) {
            url.append(":");
            url.append(port);
        }

        url.append(getRequestURI());

        return url;
    }

    @Override
    public String getServletPath() {
        return uri;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    /**
     * Cookieの中にsessionIdを取得する
     */
    public String getJSessionIdFromCookie() {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
