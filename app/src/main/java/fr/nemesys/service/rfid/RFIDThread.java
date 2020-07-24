package fr.nemesys.service.rfid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.handheld.LF134K.Lf134KManager;
import com.handheld.LF134K.Lf134kDataModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.SerialPort;
import cn.pda.serialport.Tools;

public class RFIDThread extends Thread {
    public static int UHF = 1001;
    private ScanConfig config;
    private Handler handler;
    private Context mContext;
    private Timer mTimer = null;
    private Timer mTimer2 = null;
    private List<String> mydatamodel = new ArrayList<>();
    private Lf134KManager reader;
    private boolean runFlag = true;
    /* access modifiers changed from: private */
    public boolean startFlag = false;

    public RFIDThread(Handler handler, Context context) throws SecurityException, IOException {
        this.mContext = context;
        this.config = new ScanConfig(this.mContext);
        this.handler = handler;
        try {
            this.reader = new Lf134KManager(SerialPort.Power_Rfid);
            if (this.reader != null) {
                Log.e("RFIDThread", "init lf success");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        super.run();
        while (this.runFlag) {
            Lf134kDataModel datamodel = this.reader.GetData( /*Boolean.valueOf(this.startFlag)); ,*/ 100);
            if (this.startFlag && datamodel != null) {
                try {
                    if (!this.config.isRepeat()) {
                        byte[] e = datamodel.ID;
                        String est = new StringBuilder(String.valueOf(Tools.BytesToLong(e))).toString();
                        if (!this.mydatamodel.contains(est)) {
                            sendMessege(datamodel.Country, e, UHF);
                            this.mydatamodel.add(est);
                        }
                    } else {
                        sendMessege(datamodel.Country, datamodel.ID, UHF);
                    }
                    if (!this.config.isCirculate()) {
                        this.startFlag = false;
                    }
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void sendMessege(byte[] nation, byte[] data, int mode) {
        String nationStr = new StringBuilder(String.valueOf(Tools.BytesToLong(nation))).toString();
        String dataStr = new StringBuilder(String.valueOf(Tools.BytesToLong(data))).toString();
        int datalent = dataStr.length();
        int nationlent = nationStr.length();
        for (int i = 0; i < 12 - datalent; i++) {
            dataStr = "0" + dataStr;
        }
        for (int j = 0; j < 3 - nationlent; j++) {
            nationStr = "0" + nationStr;
        }
        Bundle bundle = new Bundle();
        bundle.putString("data", String.valueOf(nationStr) + dataStr);
        Log.e("data", nationStr);
        if (this.config.isCirculate()) {
            bundle.putString("c", new StringBuilder(String.valueOf(this.mydatamodel.size())).toString());
        } else {
            bundle.putString("c", "1");
        }
        Message msg = new Message();
        msg.what = mode;
        msg.setData(bundle);
        this.handler.sendMessage(msg);
    }

    public void scan() {
        Log.e("RFIDThread", "scan");
        this.mydatamodel.removeAll(this.mydatamodel);
        this.startFlag = true;
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        this.mTimer = new Timer();
        this.mTimer.schedule(new TimerTask() {
            public void run() {
                RFIDThread.this.startFlag = false;
            }
        }, (long) (this.config.getTime() * 1000));
    }

    public void close() {
        this.reader.Close();
    }

    public void sendTo(String data) {
        Intent toBack = new Intent();
        toBack.setAction("android.rfid.INPUT");
        toBack.putExtra("data", data);
        toBack.putExtra("enter", true);
        this.mContext.sendBroadcast(toBack);
    }

}
