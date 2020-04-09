package com.example.gfm_window_client.tcp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerThread extends Thread {

    private static final String TAG = "TcpServerThread";

    Handler handler = null;
    private int port;
    Socket client;

    private byte buffer[] = new byte[500];

    InputStream inputStream;
    OutputStream outputStream;

    ServerSocket serverSocket;

    public TcpServerThread(Handler handler, int port) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();

        try {
            serverSocket = new ServerSocket(port);//监听端口
        } catch (IOException e) {
            Log.d("server IOException:", e.getMessage().toString());
        }

        while (true) {
            try {
                client = serverSocket.accept();
                inputStream = client.getInputStream();
                outputStream = client.getOutputStream();

                //将连接的客户端IP显示
                Message msg = new Message();
                msg.what = 0x01;
                msg.obj = client.getInetAddress().getHostAddress();
                handler.sendMessage(msg);

                //启动接收线程
                Receive_Thread receive_Thread = new Receive_Thread();
                receive_Thread.start();

            } catch (IOException e) {
                Log.d(TAG, e.getMessage().toString());
            }
        }
    }

    public void sendData(String msg) {

        if (outputStream != null && client != null && client.isConnected()) {

            //任何网络访问都要在子线程，不然就报错
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream.write(msg.getBytes());
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "sendMessage: client or stream has closed...");
        }
    }

    //接收线程
    class Receive_Thread extends Thread {
        public void run()//重写run方法
        {
            while (true) {
                    try {
                        final byte[] buf = new byte[1024];
                        final int len = inputStream.read(buf);
                        if (len != -1) {
                            final String text = new String(buf, 0, len);

                            Message msg = new Message();
                            msg.what = 0x10;
                            msg.obj = text;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }



            }
        }
    }



}


