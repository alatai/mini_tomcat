package com.saihou.minitomcat.catalina;

import cn.hutool.log.LogFactory;
import com.saihou.minitomcat.http.Request;
import com.saihou.minitomcat.http.Response;
import com.saihou.minitomcat.util.ThreadPoolUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Connector、複数のポートの設定は可能にある
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/20 20:54
 */
public class Connector implements Runnable {

    private int port;
    private final Service service;

    // 圧縮設定
    // 圧縮するか、onはOK
    private String compression;
    // 圧縮のバイト数（一般的には1024)
    private int compressionMinSize;
    // 圧縮できないブラウザ
    private String noCompressibleUserAgents;
    // 圧縮するmimeType
    private String compressibleMimeType;

    public Connector(Service service) {
        this.service = service;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();
                Runnable task = () -> {
                    try (socket) {
                        // リクエスト
                        Request request = new Request(socket, service);
                        // レスポンス
                        Response response = new Response();
                        // 接続処理
                        ProcessorHandler processorHandler = new ProcessorHandler();
                        processorHandler.execute(socket, request, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                // スレッドポールで任務を実行する、同時に複数の接続が可能
                ThreadPoolUtil.run(task);
            }
        } catch (Exception e) {
            LogFactory.get().error(e);
        }
    }

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]", port);
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]", port);
        new Thread(this).start();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Service getService() {
        return service;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public String getNoCompressibleUserAgents() {
        return noCompressibleUserAgents;
    }

    public void setNoCompressibleUserAgents(String noCompressibleUserAgents) {
        this.noCompressibleUserAgents = noCompressibleUserAgents;
    }

    public String getCompressibleMimeType() {
        return compressibleMimeType;
    }

    public void setCompressibleMimeType(String compressibleMimeType) {
        this.compressibleMimeType = compressibleMimeType;
    }
}
