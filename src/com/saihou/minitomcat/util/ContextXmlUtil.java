package com.saihou.minitomcat.util;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * context.xmlファイルを解析するツールクラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/21 14:08
 */
public class ContextXmlUtil {

    /**
     * Servletの設定ファイルweb.xmlを取得する
     */
    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(Constant.CONTEXT_XML_FILE);
            Document document = Jsoup.parse(xml);
            Element element = document.select("WatchedResource").first();

            return element != null ? element.text() : null;
        } catch (Exception e) {
            e.printStackTrace();

            return "WEB-INF/web.xml";
        }
    }
}
