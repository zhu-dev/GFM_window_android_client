package com.example.gfm_window_client.tcp;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class KeepAliveThread implements Runnable {
    private static final String TAG = "KeepAliveThread";

    private Socket socket;

    private boolean isRunning = true;

    private OutputStream os = null;

    public KeepAliveThread(Socket socket) {
        this.socket = socket;

        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        isRunning = false;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: hbp start sending...");
        try {
            while (isRunning) {
                Thread.sleep(2000);
                os.write("app:hbp!".getBytes());
                os.flush();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}


