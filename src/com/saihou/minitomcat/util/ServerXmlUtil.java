package com.saihou.minitomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.saihou.minitomcat.catalina.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * server.xmlファイルを解析するツールクラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 14:45
 */
public class ServerXmlUtil {

    private static final String xml;
    private static final Document document;

    static {
        xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        document = Jsoup.parse(xml);
    }

    /**
     * Context要素を分析してContextを作成する
     */
    public static List<Context> getContexts() {
        List<Context> contexts = new ArrayList<>();
        Elements elements = document.select("Context");

        for (Element element : elements) {
            String path = element.attr("path");
            String docBase = element.attr("docBase");

            Context context = new Context(path, docBase);
            contexts.add(context);
        }

        return contexts;
    }

    /**
     * Engine要素を分析してデフォールトHostを取得する
     */
    public static String getEngineDefaultHost() {
        Element host = document.select("Engine").first();

        return host != null ? host.attr("defaultHost") : null;
    }

    /**
     * Host要素を分析してすべてのHostを取得する
     */
    public static List<Host> getHosts(Engine engine) {
        List<Host> hosts = new ArrayList<>();
        Elements elements = document.select("Host");

        for (Element element : elements) {
            String name = element.attr("name");
            Host host = new Host(name, engine);
            hosts.add(host);
        }

        return hosts;
    }

    /**
     * Service要素を分析してServiceの名前を取得する
     */
    public static String getServiceName() {
        Element service = document.select("Service").first();

        return service != null ? service.attr("name") : null;
    }

    /**
     * Connector要素を分析してすべてのConnectorを取得する
     */
    public static List<Connector> getConnectors(Service service) {
        List<Connector> connectors = new ArrayList<>();
        Elements elements = document.select("Connector");

        for (Element element : elements) {
            int port = Convert.toInt(element.attr("port"));
            String compression = element.attr("compression");
            int compressionMinSize = Convert.toInt(element.attr("compressionMinSize"), 0);
            String noCompressibleUserAgents = element.attr("noCompressibleUserAgents");
            String compressibleMimeType = element.attr("compressibleMimeType");

            Connector connector = new Connector(service);
            connector.setPort(port);
            connector.setCompression(compression);
            connector.setCompressionMinSize(compressionMinSize);
            connector.setNoCompressibleUserAgents(noCompressibleUserAgents);
            connector.setCompressibleMimeType(compressibleMimeType);

            connectors.add(connector);
        }

        return connectors;
    }
}
