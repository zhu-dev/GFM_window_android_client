package com.example.gfm_window_client.tcp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.gfm_window_client.utils.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final String TAG = "Client";

    private String host;

    private int port;

    private Socket client;

    private OutputStream os = null;
    private InputStream is = null;

    // 线程池，此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mLoginHandler;

    private Handler mUpdateHandler;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;

        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        Log.d(TAG, "Client: host+port" + host + port);
    }

    public void connect(Handler loginHandler) {
        mLoginHandler = loginHandler;

        //子线程访问网路
        mThreadPool.execute(() -> {
            try {
                client = new Socket(host, port);
                Log.d(TAG, "connect: -----");
                os = client.getOutputStream();
                is = client.getInputStream();
                Message msg = Message.obtain();
                if (client.isConnected()) {
                    msg.what = Constant.SOCKET_CONN_SUCESS;
                    mLoginHandler.sendMessage(msg);
                    mThreadPool.execute(new KeepAliveThread(client));
                    //mThreadPool.execute(new RecevThread(client));
                } else {
                    msg.what = Constant.SOCKET_CONN_FAILD;
                    mLoginHandler.sendMessage(msg);
                    Log.d(TAG, "connect: connect faild...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void disconnect() {
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
        }
    }

    public void sendMessage(String msg) {
        if (os != null && client != null) {

            mThreadPool.execute(() -> {
                try {
                    os.write(msg.getBytes());
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.d(TAG, "sendMessage: client or stream has closed...");
        }
    }

    public void subscribeReceived(Handler updateHandler) {
        mUpdateHandler = updateHandler;

        mThreadPool.execute(() -> {
            try {
                while (true) {
                    byte[] b = new byte[4];
                    int r = 0;
                    r = is.read(b);
                    if (r > -1) {
                        String str = new String(b);
                        Message msg = mUpdateHandler.obtainMessage(Constant.SOCKET_RECEIVED_MESSAGE_SUCESS, str);
                        mUpdateHandler.sendMessage(msg);
                        Log.d(TAG, "Server: " + str);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message msg = mUpdateHandler.obtainMessage(Constant.SOCKET_RECEIVED_MESSAGE_FAILD);
                mUpdateHandler.sendMessage(msg);
            }

        });
    }

}
