package com.example.gfm_window_client;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gfm_window_client.tcp.Client;
import com.example.gfm_window_client.tcp.NetworkSniffTask;
import com.example.gfm_window_client.tcp.ScanDeviceTool;
import com.example.gfm_window_client.tcp.TcpUtils;
import com.example.gfm_window_client.utils.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanHostActivity extends AppCompatActivity {

    private static final String TAG = "ScanHostActivity";

    @BindView(R.id.btn_start) //butterKnife注解方式。注册控件
    Button btnStart;
    @BindView(R.id.tv_host_value)
    TextView tvHostValue;
    @BindView(R.id.rv_ip)
    RecyclerView rvIp;

    private LinearLayoutManager layoutManager;
    private IpSelectAdapter adapter;
    private static List<String> ipList;

    private LoadingDialog loadingDialog;
    private boolean isShow;
    private static ScanDeviceTool scanDeviceTool;
    private NetworkSniffTask sniffTask;
    private static Client client;
    private CustomHandler mhandler;
    public static final int SERVER_PORT = 8787;

    private boolean isScaned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_host);
        ButterKnife.bind(this);//这里绑定context,将视图文件和java文件绑定

        EventBus.getDefault().register(this); //注册evenbus，在这里是为了传递tcp client 对象

        mhandler = new CustomHandler(this); //handle 在这里是为了通知这个activity,tcp连接的结果

        loadingDialog = new LoadingDialog.Builder(ScanHostActivity.this).create(); //显示加载弹出对话框

        String host_ip = TcpUtils.getIP();
        tvHostValue.setText(host_ip);
        scanDeviceTool = new ScanDeviceTool();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanDeviceTool.destory();
        sniffTask.cancel(true);
    }

    @OnClick(R.id.btn_start)
    public void onViewClicked() {
        btnStart.setText("扫描中...");
        loadingDialog.show();

        sniffTask =new NetworkSniffTask(scanDeviceTool);
        sniffTask.setOnScanFinishListener(strings -> {
            btnStart.setText("扫描完成");
            loadingDialog.dismiss();
            //创建布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
            layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            //创建适配器，将数据传递给适配器
            adapter = new IpSelectAdapter(strings);
            //设置布局管理器
            rvIp.setLayoutManager(layoutManager);
            //设置适配器adapter
            rvIp.setAdapter(adapter);
            adapter.setOnItemClickListener(new IpSelectAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Log.d(TAG, "onItemClick: click:" + strings.get(position) + ":" + SERVER_PORT);
                    client = new Client(strings.get(position), SERVER_PORT);
                    client.connect(mhandler);
                }
            });
        });
        sniffTask.execute();
    }

    private static class CustomHandler extends Handler {
        private WeakReference<Activity> weakReference = null;

        public CustomHandler(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = weakReference.get();
            switch (msg.what) {
                case Constant.SOCKET_CONN_SUCESS:
                    //跳转
                    Intent intent = new Intent(activity, MessageActivity.class);
                    EventBus.getDefault().postSticky(client);
                    activity.startActivity(intent);
                    break;
                case Constant.SOCKET_CONN_FAILD:
                    //toast
                    Toast.makeText(activity, "Socket连接失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void onReceivedData(Client client) {
    }

}
