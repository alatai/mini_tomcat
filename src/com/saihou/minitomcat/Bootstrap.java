package com.saihou.minitomcat;

import com.saihou.minitomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;

/**
 * サーバを立ち上げる入り口
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/16 15:32
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "com.saihou.minitomcat.catalina.Server";

        // CommonClassLoaderでServerクラスをロードする
        Class<?> serverClz = commonClassLoader.loadClass(serverClassName);
        Object serverObj = serverClz.getDeclaredConstructor().newInstance();
        Method method = serverClz.getMethod("start");
        method.invoke(serverObj);
    }
}
