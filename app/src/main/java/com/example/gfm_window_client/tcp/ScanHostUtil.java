package com.example.gfm_window_client.tcp;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScanHostUtil {
    private static final String TAG = "ScanHostUtil";
    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    private List<String> mIpList = new ArrayList<>();// ping成功的IP地址
    private ThreadPoolExecutor mExecutor;// 线程池对象

    public void scan(String localIp) {

        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                CORE_POOL_SIZE));

        String prefix = localIp.substring(0, localIp.lastIndexOf(".") + 1);
        Log.d(TAG, "prefix: " + prefix);

        for (int i = 0; i < 255; i++) {
            String testIp = prefix + String.valueOf(i);


            Runnable run = new Runnable() {
                @Override
                public void run() {
                    InetAddress address = null;
                    try {
                        address = InetAddress.getByName(testIp);
                        boolean reachable = address.isReachable(1000);
                        String hostName = address.getCanonicalHostName();
                        if (reachable) {
                            Log.i(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
                            mIpList.add(hostName);
                            // Log.i(TAG, "Host: " + hostName + "(" + testIp + ") is reachable!");
                        } else {
                            Log.i(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is not reachable!");

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mExecutor.execute(run);
        }
        mExecutor.shutdown();

        while(true){
            if (mExecutor.isTerminated()){
                Log.d(TAG, "scan: scan finished");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void destroy() {
        if (mExecutor != null){
            mExecutor.shutdownNow();
        }

    }

}
