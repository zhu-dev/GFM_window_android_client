package com.example.gfm_window_client.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Description:
 * Author: Breeziness
 */
public class CloseableUtil {
    public static void release(Closeable... closeables){
        for (Closeable closeable:closeables){
            try {
                if(null !=closeable){
                    closeable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
