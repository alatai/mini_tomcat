package com.saihou.minitomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * 定数クラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/19 13:52
 */
public class Constant {

    // HTTP状態コード
    public static final int CODE_200 = 200;
    public static final int CODE_302 = 302;
    public static final int CODE_404 = 404;
    public static final int CODE_500 = 500;

    // 状態の内容：プロトコル/バージョン 状態コード デスクリプション
    // レスポンス型：text/html、text/plain...
    public final static String RESPONSE_HEADER_200 = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: {}{}\r\n\r\n";
    public final static String RESPONSE_HEADER_200_GZIP = "HTTP/1.1 200 OK\r\n"
            + "Content-Encoding:gzip\r\n\r\n";

    public final static String RESPONSE_HEADER_302 = "HTTP/1.1 302 Found\r\nLocation: {}\r\n\r\n";

    public final static String RESPONSE_HEADER_404 = "HTTP/1.1 404 Not Found\r\n"
            + "Content-Type: text/html\r\n\r\n";
    public final static String TEXT_FORMAT_404 = "<html><head><title>MiniServletContainer/1.0.0 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 404 - {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> "
            + "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>MiniServletContainer 1.0.0</h3>" + "</body></html>";

    public final static String RESPONSE_HEADER_500 = "HTTP/1.1 500 Internal Server Error\r\n"
            + "Content-Type: text/html\r\n\r\n";
    public final static String TEXT_FORMAT_500 = "<html><head><title>MiniServletContainer/1.0.0 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>MiniServletContainer 1.0.0</h3>"
            + "</body></html>";

    // webapps パス
    public final static File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");

    // ROOT パス
    public final static File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");
    public final static String ROOT = "ROOT";

    // server.xmlロケーション用
    public final static File CONF_FOLDER = new File(SystemUtil.get("user.dir"), "conf");
    public final static File SERVER_XML_FILE = new File(CONF_FOLDER, "server.xml");

    // web.xmlロケーション用（Tomcatの部分デフォールト設定，e.g. mime-type、index page..）
    public static final File WEB_XML_FILE = new File(CONF_FOLDER, "web.xml");

    // context.xmlロケーション用（Tomcatの部分デフォールト設定，e.g. servletの設定ファイル、WEB-INF/web.xml）
    public static final File CONTEXT_XML_FILE = new File(CONF_FOLDER, "context.xml");

    // %TOMCAT%/workフォルダ，JSPファイルコンパイルした後のJavaファイルを保存する用
    // File.separator：“/”（Linux），“\”（Windows）
    public static final String WORK_FOLDER = SystemUtil.get("user.dir") + File.separator + "work";
}
