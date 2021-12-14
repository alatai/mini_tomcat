package com.saihou.minitomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.saihou.minitomcat.classloader.WebAppClassLoader;
import com.saihou.minitomcat.exception.WebConfigDuplicatedException;
import com.saihou.minitomcat.http.ApplicationContext;
import com.saihou.minitomcat.http.MiniServletConfig;
import com.saihou.minitomcat.util.Constant;
import com.saihou.minitomcat.util.ContextXmlUtil;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

/**
 * アプリケーションコンテキスト
 * アプリケーション単位で1つ
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 11:50
 */
public class Context {

    // 請求のURL
    private String path;
    // ウェブアプリケーションのパス、絶対パスで表示する
    private String docBase;

    // XXプロジェクト/WEB-INF/web.xml
    private final File contextWebXmlFile;

    // Servletマッピング
    // urlのServletクラス名
    private final Map<String, String> url4ServletClassName;
    // urlのServletエイリアス
    private final Map<String, String> url4ServletName;
    // Servletエイリアスのクラス名
    private final Map<String, String> servletName4ClassName;
    // Servletクラスのエイリアス
    private final Map<String, String> className4ServletName;
    // Servletの初期化パラメーター
    private final Map<String, Map<String, String>> servletInitParams;

    // 各ウェブアプリには独自のウェブアプリクラスロードがある
    private final WebAppClassLoader webAppClassLoader;

    // ServletContext、Servletとコンテナーの間の通信をサポートする
    private final ServletContext servletContext;

    // Servletプール（Singleton用）
    private final Map<Class<?>, HttpServlet> servletPool;

    public Context(String path, String docBase) {
        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXmlUtil.getWatchedResource());

        this.url4ServletClassName = new HashMap<>();
        this.url4ServletName = new HashMap<>();
        this.servletName4ClassName = new HashMap<>();
        this.className4ServletName = new HashMap<>();
        this.servletInitParams = new HashMap<>();

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webAppClassLoader = new WebAppClassLoader(docBase, commonClassLoader);

        this.servletContext = new ApplicationContext(this);

        this.servletPool = new HashMap<>();

        deploy();
    }

    /**
     * initを呼び出し，ログを記録する
     */
    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        init();
        LogFactory.get().info("Deployment of web application directory {} " +
                "has finished in {} ms", this.docBase, timeInterval.intervalMs());

        // JspRunちめContextの初期化
        // javax.servlet.jsp.JspFactory.getDefaultFactory()を実行する時リターン値がある
        JspC jspC = new JspC();
        new JspRuntimeContext(servletContext, jspC);
    }

    /**
     * 初期化
     * ① web.xmlはあるかを判断する
     * ② web.xmlを解析する
     */
    private void init() {
        if (!contextWebXmlFile.exists()) {
            return;
        }

        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }

        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document document = Jsoup.parse(xml);

        // Servletマッピングを解析する
        parseServletMapping(document);
        // Servlet初期化パラメーターを解析する
        parseServletInitParams(document);
    }


    public void stop() {
        webAppClassLoader.stop();
        destroyServlets();
    }

    /**
     * すべてのServletを停止する
     */
    private void destroyServlets() {
        Collection<HttpServlet> servlets = servletPool.values();

        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }

    /**
     * web.xmlを解析する
     * <servlet>
     * <servlet-name></servlet-name>
     * <servlet-class></servlet-class>
     * </servlet>
     * <servlet-name></servlet-name>
     * <url-pattern></url-pattern>
     * </servlet-mapping>
     */
    private void parseServletMapping(Document document) {
        // url4ServletName
        Elements urlPatternElements = document.select("servlet-mapping url-pattern");

        for (Element element : urlPatternElements) {
            String urlPattern = element.text();
            String servletName = element.parent().select("servlet-name").first().text();
            url4ServletName.put(urlPattern, servletName);
        }

        // servletName4ClassName
        // className4ServletName
        Elements servletElements = document.select("servlet servlet-name");

        for (Element element : servletElements) {
            String servletName = element.text();
            String servletClass = element.parent().select("servlet-class").first().text();
            servletName4ClassName.put(servletName, servletClass);
            className4ServletName.put(servletClass, servletName);
        }

        // url4ServletClassName
        Set<String> urls = url4ServletName.keySet();

        for (String url : urls) {
            String servletName = url4ServletName.get(url);
            String servletClass = servletName4ClassName.get(servletName);
            url4ServletClassName.put(url, servletClass);
        }
    }

    /**
     * Servlet配置をチェックする
     */
    private void checkDuplicated(Document document, String mapping, String desc)
            throws WebConfigDuplicatedException {
        Elements elements = document.select(mapping);
        // ソート用
        List<String> contents = new ArrayList<>();

        for (Element element : elements) {
            contents.add(element.text());
        }

        Collections.sort(contents);

        for (int i = 0; i < contents.size(); i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);

            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void checkDuplicated() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(Constant.CONTEXT_XML_FILE);
        Document document = Jsoup.parse(xml);

        checkDuplicated(document, "servlet-mapping url-pattern", "servlet url 重複，唯一にしてください：{} ");
        checkDuplicated(document, "servlet servlet-name", "servlet エイリアス重複，唯一にしてください：{} ");
        checkDuplicated(document, "servlet servlet-class", "servlet クラス名重複，唯一にしてください：{} ");
    }

    /**
     * web.xmlファイルのServlet初期化パラメーターを解析する
     */
    private void parseServletInitParams(Document document) {
        Elements servletClassNameElements = document.select("servlet-class");

        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initParamElements = servletClassNameElement.parent().select("init-param");

            if (initParamElements.isEmpty()) {
                continue;
            }

            Map<String, String> initParams = new HashMap<>();

            for (Element initParamElement : initParamElements) {
                String name = initParamElement.select("param-name").get(0).text();
                String value = initParamElement.select("param-value").get(0).text();

                initParams.put(name, value);
            }

            servletInitParams.put(servletClassName, initParams);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public String getServletClassName(String uri) {
        return url4ServletClassName.get(uri);
    }

    public WebAppClassLoader getWebAppClassLoader() {
        return webAppClassLoader;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public synchronized HttpServlet getServlet(Class<?> clazz) throws Exception {
        HttpServlet servlet = servletPool.get(clazz);

        if (servlet == null) {
            servlet = (HttpServlet) clazz.newInstance();

            // 在放入ServletPool之前，一系列初始化处理
            ServletContext servletContext = getServletContext();
            String clazzName = clazz.getName();
            String servletName = className4ServletName.get(clazzName);
            Map<String, String> initParams = servletInitParams.get(clazzName);

            ServletConfig servletConfig = new MiniServletConfig(servletContext, servletName, initParams);
            servlet.init(servletConfig);

            servletPool.put(clazz, servlet);
        }

        return servlet;
    }

    public WebAppClassLoader getWebClassLoader() {
        return webAppClassLoader;
    }
}
