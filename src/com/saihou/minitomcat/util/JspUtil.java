package com.saihou.minitomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.saihou.minitomcat.catalina.Context;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;

import java.io.File;

/**
 * JSPを解析するツールクラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/12/14 16:00
 */
public class JspUtil {

    /**
     * JSPを解析する
     *
     * @param context アプリコンテクスト
     * @param file    目標ファイル
     */
    public static void compileJsp(Context context, File file) throws JasperException {
        String subFolder;
        String path = context.getPath();

        if ("/".equals(path)) {
            subFolder = "_";
        } else {
            subFolder = StrUtil.subAfter(path, "/", false);
        }

        String workPath = new File(Constant.WORK_FOLDER, subFolder).getAbsolutePath() + File.separator;
        String[] args = {"-webapp", context.getDocBase().toLowerCase(), "-d", workPath.toLowerCase(), "-compile"};

        JspC jspC = new JspC();
        // Apply command-line arguments.
        jspC.setArgs(args);
        // Executes the compilation.
        jspC.execute(file);
    }

    public static String getServletPath(String uri, String subFolder) {
        String tempPath = "org/apache/jsp/" + uri;
        File tempFile = FileUtil.file(Constant.WORK_FOLDER, subFolder, tempPath);

        String fileNameOnly = tempFile.getName();
        String classFileName = fileNameOnly.replace(".", "_");
        File servletFile = new File(tempFile.getParent(), classFileName);

        return servletFile.getAbsolutePath();
    }

    public static String getServletClassPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".class";
    }

    public static String getServletJavaPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".java";
    }

    public static String getJspServletClassName(String uri, String subFolder) {
        File tempFile = FileUtil.file(Constant.WORK_FOLDER, subFolder);
        String tempPath = tempFile.getAbsolutePath() + File.separator;
        String servletPath = getServletPath(uri, subFolder);
        String jsServletClassPath = StrUtil.subAfter(servletPath, tempPath, false);

        return StrUtil.replace(jsServletClassPath, File.separator, ".");
    }
}
