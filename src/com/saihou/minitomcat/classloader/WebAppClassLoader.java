package com.saihou.minitomcat.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * ウェブアプリクラスロード
 * 各ウェブアプリには独自のウェブアプリクラスロードがある
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/27 16:06
 */
public class WebAppClassLoader extends URLClassLoader {

    public WebAppClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);

        try {
            // ContextのdocBase下のclassesとlib
            File webInfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webInfFolder, "classes");
            File libFolder = new File(webInfFolder, "lib");

            // classesファイル
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);

            // jarファイル
            List<File> jarFiles = FileUtil.loopFiles(libFolder);

            for (File jarFile : jarFiles) {
                url = new URL("file:" + jarFile.getAbsolutePath());
                this.addURL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
