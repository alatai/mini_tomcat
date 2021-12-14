package com.saihou.minitomcat.classloader;

import cn.hutool.core.util.StrUtil;
import com.saihou.minitomcat.catalina.Context;
import com.saihou.minitomcat.util.Constant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * JSP classloader
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/12/14 17:24
 */
public class JspClassLoader extends URLClassLoader {

    // JSPファイルのマッピング
    private static final Map<String, JspClassLoader> map = new HashMap<>();

    /**
     * WebClassLoaderを基にJspClassLoaderを作成する
     * contextから%TOMCAT_HOMO%/workのパスを取得する
     * そのパスをURLとして、ClassLoaderに入れる
     * jspファイルをロードする時、実際のパスを探すことができる
     */
    private JspClassLoader(Context context) {
        super(new URL[]{}, context.getWebClassLoader());

        try {
            String path = context.getPath();
            String subFolder;

            if (path.equals("/")) {
                subFolder = "-";
            } else {
                subFolder = StrUtil.subAfter(path, "/", false);
            }

            File classesFolder = new File(Constant.WORK_FOLDER, subFolder);
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * JSPに対するJspClassLoaderを取得する
     */
    public static JspClassLoader getJspClassLoader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        JspClassLoader jspClassLoader = map.get(key);

        if (jspClassLoader == null) {
            jspClassLoader = new JspClassLoader(context);
            map.put(key, jspClassLoader);
        }

        return jspClassLoader;
    }

    /**
     * クラスファイルとJspClassLoaderの関連を削除する
     */
    public static void invalidateClassLoader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        map.remove(key);
    }
}
