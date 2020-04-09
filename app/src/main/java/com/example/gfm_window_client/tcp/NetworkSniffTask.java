package com.example.gfm_window_client.tcp;

import android.os.AsyncTask;


import java.util.List;

public class NetworkSniffTask extends AsyncTask<Void, Void, List<String>> {
    private ScanDeviceTool scanDeviceTool;
    private List<String> ipList;
    private OnScanFinishListener onScanFinishListener;

    public NetworkSniffTask(ScanDeviceTool scanDeviceTool) {
        super();
        this.scanDeviceTool = scanDeviceTool;
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        scanDeviceTool = new ScanDeviceTool();
        ipList = scanDeviceTool.scan();
        return ipList;
    }

    @Override
    protected void onPostExecute(List<String> strings) {
        super.onPostExecute(strings);
        onScanFinishListener.onScanFinish(strings);
    }

    public void setOnScanFinishListener(OnScanFinishListener onScanFinishListener) {
        this.onScanFinishListener = onScanFinishListener;
    }

    public interface OnScanFinishListener {
        void onScanFinish(List<String> strings);
    }
}


