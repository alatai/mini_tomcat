package com.saihou.minitomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import com.saihou.minitomcat.util.ServerXmlUtil;

import java.util.List;

/**
 * Tomcatのサービス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 17:17
 */
public class Service {

    private final String name;
    private final Engine engine;
    private final Server server;
    private final List<Connector> connectors;

    public Service(Server server) {
        this.name = ServerXmlUtil.getServiceName();
        this.engine = new Engine(this);
        this.server = server;
        this.connectors = ServerXmlUtil.getConnectors(this);
    }

    public void start() {
        init();
    }

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();

        for (Connector connector : connectors) {
            connector.init();
        }

        LogFactory.get().info("Initializing processed in {} ms", timeInterval.intervalMs());

        for (Connector connector : connectors) {
            connector.start();
        }
    }

    public String getName() {
        return name;
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }
}
