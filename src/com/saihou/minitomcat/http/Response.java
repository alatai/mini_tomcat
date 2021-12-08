package com.saihou.minitomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * レスポンス（HTML文など）
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/19 17:32
 */
public class Response extends BaseResponse {

    private final StringWriter stringWriter;
    private final PrintWriter printWriter; // StringWriterのdecorator
    private String contentType;
    private byte[] body; // 二進数データ用
    private int status; // HTTP状態コード
    private List<Cookie> cookies;

    public Response() {
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    /**
     * Cookieリスト->CookieHeader
     */
    public String getCookiesHeader() {
        if (cookies == null) {
            return "";
        }

        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuilder sb = new StringBuilder();

        for (Cookie cookie : getCookies()) {
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");

            // -1：有効期限（無限）
            if (cookie.getMaxAge() != -1) {
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.SECOND, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append("; ");
            }

            if (cookie.getPath() != null) {
                sb.append("Path=").append(cookie.getPath());
            }
        }

        return sb.toString();
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBody() {
        if (body == null) {
            String content = stringWriter.toString();
            body = content.getBytes(StandardCharsets.UTF_8);
        }

        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }
}
