package com.example.gfm_window_client;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.example.gfm_window_client.tcp.Client;
import com.example.gfm_window_client.tcp.StringConvertUtil;
import com.example.gfm_window_client.utils.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MessageActivity";

    @BindView(R.id.tv_client_ip)
    TextView tvClientIp;
    @BindView(R.id.tv_rain_value)
    TextView tvRainValue;
    @BindView(R.id.tv_windy_value)
    TextView tvWindyValue;
    @BindView(R.id.tv_temperature_value)
    TextView tvTemperatureValue;
    @BindView(R.id.tv_humidity_value)
    TextView tvHumidityValue;
    @BindView(R.id.tv_air_value)
    TextView tvAirValue;
    @BindView(R.id.tv_window_value)
    TextView tvWindowValue;
    @BindView(R.id.sw_window)
    SwitchCompat sw_window;

    private boolean sw_window_isOpen = false;

    // 线程池，此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;
    private OutputStream os;
    private Client client;

    @SuppressLint("HandlerLeak")
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.SOCKET_RECEIVED_MESSAGE_SUCESS:
                    String data_ascii_str = (String) msg.obj;
                    String data_hex_str = StringConvertUtil.charStr2hexStr(data_ascii_str);
                    Log.d(TAG, "handleMessage: ascii_data:" + data_ascii_str);
                    Log.d(TAG, "handleMessage: hex_data:" + data_hex_str);

                    String temp = data_ascii_str.substring(0, 2);
                    String humidity = data_ascii_str.substring(2, 4);
                    char smokelevel = data_ascii_str.charAt(4);
                    char rainlevel = data_ascii_str.charAt(5);
                    char windlevel = data_ascii_str.charAt(6);
                    char window_isOpen = data_ascii_str.charAt(7);

                    tvTemperatureValue.setText(temp);
                    tvHumidityValue.setText(humidity);

                    switch (smokelevel) {
                        case '0':
                            tvAirValue.setText("低");
                            break;
                        case '1':
                            tvAirValue.setText("中");
                            break;
                        case '2':
                            tvAirValue.setText("高");
                            break;
                        default:
                    }
                    switch (rainlevel) {
                        case '0':
                            tvRainValue.setText("晴");
                            break;
                        case '1':
                            tvRainValue.setText("雨");
                            break;
                    }
                    switch (windlevel) {
                        case '0':
                            tvWindyValue.setText("晴");
                            break;
                        case '1':
                            tvWindyValue.setText("雨");
                            break;
                        case '2':
                            tvWindyValue.setText("雨");
                            break;
                    }
                    switch(window_isOpen)
                    {
                        case '0':
                            tvWindowValue.setText("关");
                            sw_window_isOpen = false;
                            sw_window.setChecked(false);
                            break;
                        case '1':
                            tvWindowValue.setText("开");
                            sw_window_isOpen = true;
                            sw_window.setChecked(true);
                            break;
                    }

                    break;
                case Constant.SOCKET_RECEIVED_MESSAGE_FAILD:
                    //toast
                    Toast.makeText(MessageActivity.this, "接收数据出错", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();

        sw_window.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!sw_window_isOpen) {
                sw_window_isOpen = true;
                //打开窗户
                client.sendMessage("open");
                Toast.makeText(MessageActivity.this, "打开窗户", Toast.LENGTH_SHORT).show();
            } else {
                sw_window_isOpen = false;
                //关闭窗户
                client.sendMessage("close");
                Toast.makeText(MessageActivity.this, "关闭窗户", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            client.subscribeReceived(mhandler);
        } else {
            Log.d(TAG, "onResume: client is null check the login");
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void subscribeReceivedData(Client client) {
        if (client != null) {
            this.client = client;
            tvClientIp.setText("已连接到设备");
        }
    }

}
