package com.saihou.minitomcat.catalina;

import com.saihou.minitomcat.util.ServerXmlUtil;

import java.util.List;

/**
 * Servletのエンジン、Servletの請求を処理する
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 16:06
 */
public class Engine {

    private final String defaultHost;
    private final List<Host> hosts;
    private final Service service;

    public Engine(Service service) {
        this.defaultHost = ServerXmlUtil.getEngineDefaultHost();
        this.hosts = ServerXmlUtil.getHosts(this);
        this.service = service;

        checkDefault();
    }

    /**
     * デフォールトホストがあるかをチェックする
     */
    private void checkDefault() {
        if (getDefaultHost() == null) {
            throw new RuntimeException("The defaultHost " + defaultHost + " does not exist!");
        }
    }

    public Host getDefaultHost() {
        for (Host host : hosts) {
            if (host.getName().equals(defaultHost)) {
                return host;
            }
        }

        return null;
    }

    public Service getService() {
        return service;
    }
}
