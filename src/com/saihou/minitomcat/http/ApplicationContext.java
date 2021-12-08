package com.saihou.minitomcat.http;

import com.saihou.minitomcat.catalina.Context;

import java.io.File;
import java.util.*;

/**
 * アプリケーションContext
 * ServletContextはServletとコンテナーの間の通信をサポートしている
 * サーバ（コンテナー）はアプリをロードして起動する時、ServletContextインスタンスを作成する
 * ウェブアプリには唯一のServletContextインスタンスがある
 * コンテナーの資源を訪問する様々なメソッドがある
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/27 17:09
 */
public class ApplicationContext extends BaseServletContext {

    private final Map<String, Object> attributesMap;
    private final Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    @Override
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }

    @Override
    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributesMap.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();

        return Collections.enumeration(keys);
    }
}
