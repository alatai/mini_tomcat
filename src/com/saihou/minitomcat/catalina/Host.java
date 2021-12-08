package com.saihou.minitomcat.catalina;

import com.saihou.minitomcat.util.Constant;
import com.saihou.minitomcat.util.ServerXmlUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バーチャルホスト
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 15:11
 */
public class Host {

    // 仮想マシン名
    private String name;
    // Servletのエンジン、Servletの請求を処理する
    private final Engine engine;
    // パスとContextのマッピング
    private final Map<String, Context> contextMap;

    public Host(String name, Engine engine) {
        this.name = name;
        this.engine = engine;
        this.contextMap = new HashMap<>();

        // Contextを作成する
        scanContextsInWebapps();
        scanContextsInServerXml();
    }

    /**
     * server.xmlファイルをチェックして、Contextを作成する
     */
    private void scanContextsInServerXml() {
        List<Context> contexts = ServerXmlUtil.getContexts();

        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * webapps下のフォルダチェックして、Contextを作成する
     */
    private void scanContextsInWebapps() {
        File[] files = Constant.WEBAPPS_FOLDER.listFiles();
        assert files != null;

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            loadContext(file);
        }
    }

    /**
     * Contextを作成する
     * e.g.
     * ROOTの場合、path = “/”
     * aの場合、path = “/a”
     * それを基づいてContextを作成する
     */
    private void loadContext(File file) {
        String path = file.getName();

        if (Constant.ROOT.equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }

        String docBase = file.getAbsolutePath();
        Context context = new Context(path, docBase);

        contextMap.put(path, context);
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Context> getContextMap() {
        return contextMap;
    }
}
