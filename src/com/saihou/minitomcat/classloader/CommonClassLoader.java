package com.saihou.minitomcat.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * CommonClassLoader
 * /WEB-INF/classes、/WEB-INF/libのクラスとjarファイルをロードする
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/27 15:36
 */
public class CommonClassLoader extends URLClassLoader {

    /**
     * libフォルダのjarファイルをロードする
     */
    public CommonClassLoader() {
        super(new URL[]{});

        try {
            File workingFolder = new File(System.getProperty("user.dir"));
            File libFolder = new File(workingFolder, "lib");
            File[] jarFiles = libFolder.listFiles();

            for (File jarFile : jarFiles) {
                if (jarFile.getName().endsWith(".jar")) {
                    URL url = new URL("file:" + jarFile.getAbsolutePath());
                    this.addURL(url);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
