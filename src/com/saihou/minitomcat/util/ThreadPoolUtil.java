package com.saihou.minitomcat.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * スレッドツールクラス
 *
 * @author M20W0324-saihou
 * @version 1.0
 * @date 2021/11/19 23:19
 */
public class ThreadPoolUtil {

    // corePoolSize：スレッドポールに維持されるスレッド数
    // maximumPoolSize：プール内で可能なスレッドの最大数
    // keepAliveTime：アイドルタイム（idle time）状態のスレッドの待機最大時間
    // unit：keepAliveTime引数の時間単位
    // workQueue：タスクが実行されるまで保持するために使用するキュー
    // ArrayBlockingQueueは範囲のあるキュー、容量を変更することができないので
    // キューが埋まっているときに要素をputしようとすると、putオペレーションがブロックされます
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));

    public static void run(Runnable task) {
        pool.execute(task);
    }
}
