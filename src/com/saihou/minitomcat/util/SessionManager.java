package com.saihou.minitomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sessionの管理クラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/12/07 23:27
 */
public class SessionManager {

    // すべてのsessionを保存する
    // private static final Map<String, StandardSession> sessionMap = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, StandardSession> sessionMap = new ConcurrentHashMap<>();
    // sessionデフォールト有効時間
    private static final int defaultTimeout = getTimeout();

    static {
        // 立ち上がる同時に、有効性を検査する
        startSessionOutDateCheckThread();
    }

    /**
     * sessionIdを作成する
     */
    public static synchronized String generateSessionId() {
        String result;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();

        return result;
    }

    /**
     * sessionを取得する
     */
    public static HttpSession getSession(String jsessionid, Request request, Response response) {
        if (jsessionid == null) {
            return newSession(request, response);
        } else {
            StandardSession currentSession = sessionMap.get(jsessionid);

            if (currentSession == null) {
                return newSession(request, response);
            } else {
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, request, response);

                return currentSession;
            }
        }
    }

    /**
     * sessionを作成する
     */
    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String sessionId = generateSessionId();

        StandardSession session = new StandardSession(sessionId, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sessionId, session);
        createCookieBySession(session, request, response);

        return session;
    }

    /**
     * jsessionidを基にcookieを作成する
     */
    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    /**
     * web.xmlから有効時間を取得する
     */
    private static int getTimeout() {
        int defaultResult = 30;

        try {
            Document document = Jsoup.parse(Constant.WEB_XML_FILE,
                    String.valueOf(StandardCharsets.UTF_8));
            Elements elements = document.select("session-config session-timeout");

            return elements.isEmpty() ? defaultResult : Convert.toInt(elements.get(0).text());
        } catch (IOException e) {
            return defaultResult;
        }
    }

    /**
     * 30sごとにsessionをチェックする
     */
    private static void startSessionOutDateCheckThread() {
        new Thread(() -> {
            while (true) {
                checkOutDateSession();
                ThreadUtil.sleep(1000 * 30);
            }
        }).start();
    }

    /**
     * sessionをチェックする
     * 有効時間をすぎたsessionを削除する
     */
    private static void checkOutDateSession() {
        Set<String> jsessionids = sessionMap.keySet();
        List<String> outDateJsessionids = new ArrayList<>();

        for (String jsessionid : jsessionids) {
            StandardSession standardSession = sessionMap.get(jsessionid);
            long interval = System.currentTimeMillis() - standardSession.getLastAccessedTime();

            if (interval > (long) standardSession.getMaxInactiveInterval() * 1000 * 60) {
                outDateJsessionids.add(jsessionid);
            }
        }

        for (String outDateJsessionid : outDateJsessionids) {
            sessionMap.remove(outDateJsessionid);
        }
    }
}
