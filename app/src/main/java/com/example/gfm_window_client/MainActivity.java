package com.example.gfm_window_client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.gfm_window_client.tcp.TcpUtils;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv_host_value)
    TextView tvHostValue;
    @BindView(R.id.tv_port_value)
    TextView tvPortValue;
    @BindView(R.id.btn_start)
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        String host_ip = TcpUtils.getIP();
        tvHostValue.setText(host_ip);
        tvPortValue.setText("9898");

    }


    @OnClick(R.id.btn_start)
    public void onViewClicked() {

        Intent intent = new Intent(MainActivity.this, ScanHostActivity.class);
        startActivity(intent);
    }
}
