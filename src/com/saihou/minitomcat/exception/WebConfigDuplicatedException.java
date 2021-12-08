package com.saihou.minitomcat.exception;

/**
 * 同じ複数のServletを設定したら、例外は発生する
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/21 14:13
 */
public class WebConfigDuplicatedException extends Exception {

    public WebConfigDuplicatedException(String msg) {
        super(msg);
    }
}
