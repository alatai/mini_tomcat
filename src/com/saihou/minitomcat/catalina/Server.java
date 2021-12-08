package com.saihou.minitomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * サーバ自体
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 17:32
 */
public class Server {

    private final Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        TimeInterval timeInterval = DateUtil.timer();
        logJVM();
        init();

        LogFactory.get().info("Server startup in {} ms", timeInterval.intervalMs());
    }

    private void init() {
        service.start();
    }

    /**
     * ログ（Tomcat起動のように)
     */
    private static void logJVM() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Server version", "MiniTomcat/1.0");
        info.put("Server built", "2021-11-19 23:04:16");
        info.put("Server number", "1.0.0");
        info.put("OS Name\t", SystemUtil.get("os.name"));
        info.put("OS Version", SystemUtil.get("os.version"));
        info.put("Architecture", SystemUtil.get("os.arch"));
        info.put("Java Home", SystemUtil.get("java.home"));
        info.put("JVM Version", SystemUtil.get("java.runtime.version"));
        info.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keySet = info.keySet();

        for (String key : keySet) {
            LogFactory.get().info(key + "\t\t" + info.get(key));
        }
    }
}
