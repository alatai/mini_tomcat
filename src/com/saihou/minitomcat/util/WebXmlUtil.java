package com.saihou.minitomcat.util;

import cn.hutool.core.io.FileUtil;
import com.saihou.minitomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * web.xmlファイルを解析するツールクラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 19:36
 */
public class WebXmlUtil {

    private static final Map<String, String> mimeTypeMap;
    private static final String xml;
    private static final Document document;
    private static volatile boolean isInitialized = false;

    static {
        mimeTypeMap = new HashMap<>();
        xml = FileUtil.readUtf8String(Constant.WEB_XML_FILE);
        document = Jsoup.parse(xml);
    }

    /**
     * ContextのdocBaseを基にweb.xml中にwelcomeFileを探す
     * なければデフォールトのindex.htmlをリターンする
     */
    public static String getWelcomeFile(Context context) {
        Elements elements = document.select("welcome-file");

        for (Element element : elements) {
            String filename = element.text();
            File file = new File(context.getDocBase(), filename);

            if (file.exists()) {
                return file.getName();
            }
        }

        return "index.html";
    }

    /**
     * 拡張子でmimeTypeを取得する
     */
    public static String getMimeType(String extName) {
        if (mimeTypeMap.isEmpty()) {
            initMimeType();
        }

        String mimeType = mimeTypeMap.get(extName);

        return mimeType == null ? "text/html" : mimeType;
    }

    /**
     * mimeTypeMapを初期化する
     */
    private static void initMimeType() {
        Elements elements = document.select("mime-mapping");

        for (Element element : elements) {
            String extName = element.select("extension").first().text();
            String mimeType = element.select("mime-type").first().text();
            mimeTypeMap.put(extName, mimeType);
        }
    }
}
