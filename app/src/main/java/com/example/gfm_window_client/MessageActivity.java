package com.example.gfm_window_client;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import butterknife.OnClick;

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
    @BindView(R.id.ed_temp)
    EditText edTemp;
    @BindView(R.id.btn_set_temp)
    Button btnSetTemp;
    @BindView(R.id.ed_humi)
    EditText edHumi;
    @BindView(R.id.btn_set_humi)
    Button btnSetHumi;
    @BindView(R.id.ed_wind)
    EditText edWind;
    @BindView(R.id.btn_set_wind)
    Button btnSetWind;
    @BindView(R.id.ed_smoke)
    EditText edSmoke;
    @BindView(R.id.btn_set_smoke)
    Button btnSetSmoke;

    private boolean sw_window_isOpen = false;
    private boolean sw_soft_check = false;
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
                    Log.d(TAG, "handleMessage: ascii_data:" + data_ascii_str);
//                    Log.d(TAG, "handleMessage: hex_data:" + data_hex_str);
                    char window_isOpen ;
                    char smokelevel;
                    String temp ;
                    String humidity;
                    char rainlevel;
                    char windlevel;
                    char head;

                    head = data_ascii_str.charAt(0);
                    switch (head) {
                        case 'w':
                            window_isOpen = data_ascii_str.charAt(1);
                            switch (window_isOpen) {
                                case '0':
                                    tvWindowValue.setText("关");
                                    sw_window_isOpen = false;
                                    sw_soft_check = true;
                                    sw_window.setChecked(false);
                                    break;
                                case '1':
                                    tvWindowValue.setText("开");
                                    sw_window_isOpen = true;
                                    sw_soft_check = true;
                                    sw_window.setChecked(true);
                                    break;
                            }
                            break;
                        case 'd':
                            temp = data_ascii_str.substring(1, 3);
                            humidity = data_ascii_str.substring(3, 5);
                            smokelevel = data_ascii_str.charAt(5);
                            rainlevel = data_ascii_str.charAt(6);
                            windlevel = data_ascii_str.charAt(7);
                            tvTemperatureValue.setText(temp + "℃");
                            tvHumidityValue.setText(humidity + "%");

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
                                    tvWindyValue.setText("低");
                                    break;
                                case '1':
                                    tvWindyValue.setText("中");
                                    break;
                                case '2':
                                    tvWindyValue.setText("高");
                                    break;
                            }
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
            if (isChecked) {
                //打开窗户
                client.sendMessage("open");
                Toast.makeText(MessageActivity.this, "打开窗户", Toast.LENGTH_SHORT).show();
//                if (!sw_soft_check) {
//
//                } else {
//                    sw_soft_check = false;
//                }
            } else {
                //关闭窗户
                client.sendMessage("close");
                Toast.makeText(MessageActivity.this, "关闭窗户", Toast.LENGTH_SHORT).show();
//                if (!sw_soft_check) {
//
//                } else {
//                    sw_soft_check = false;
//                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            Log.d(TAG, "onResume: subscribeReceived");
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
            Log.d(TAG, "subscribeReceivedData:已连接到设备 ");
        }
    }

    @OnClick({R.id.btn_set_temp, R.id.btn_set_humi, R.id.btn_set_wind, R.id.btn_set_smoke})
    public void onViewClicked(View view) {
        String input;
        switch (view.getId()) {
            case R.id.btn_set_temp:
                input = edTemp.getText().toString();
                if (!input.equals("")) client.sendMessage("tht" + input);
                break;
            case R.id.btn_set_humi:
                input = edHumi.getText().toString();
                if (!input.equals("")) client.sendMessage("thh" + input);
                break;
            case R.id.btn_set_wind:
                input = edWind.getText().toString();
                if (!input.equals("")) client.sendMessage("thw" + input);
                break;
            case R.id.btn_set_smoke:
                input = edSmoke.getText().toString();
                if (!input.equals("")) client.sendMessage("ths" + input);
                break;
        }
    }
}
