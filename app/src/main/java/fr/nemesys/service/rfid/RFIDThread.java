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
import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.SerialPort;
import cn.pda.serialport.Tools;
import io.sentry.core.Sentry;

public class RFIDThread extends Thread {
    public static int LF = 1004;
    private ScanConfig config;
    private RFIDService.RFIDHandler handler;
    private Context mContext;
    private Timer mTimer = null;
    private Lf134KManager reader;
    public boolean runFlag = true;
    /* access modifiers changed from: private */
    public boolean startFlag = false;

    public RFIDThread(Handler handler, int port,  Context context) throws SecurityException, IOException {
        this.mContext = context;
        this.config = new ScanConfig(this.mContext);
        this.handler = (RFIDService.RFIDHandler) handler;
        try {
            this.reader = new Lf134KManager( port, this.handler);
            if (this.reader != null) {
                Log.e("RFIDThread", "init lf success");
                this.handler.sendLog("RFIDThread", "init lf success");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Sentry.captureException(e);
                    e.printStackTrace();
                }
            }
        }catch (IOException e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
    }

    public void run() {
        super.run();
        while (this.runFlag) {
            if (startFlag) {
                Lf134kDataModel datamodel = this.reader.GetData( /*Boolean.valueOf(this.startFlag)); ,*/ 100);
                if (datamodel!=null) {
                    this.handler.sendLog("RFIDThread:run",Tools.BytesToLong(datamodel.ID)+"" );
                    sendMSG(Tools.BytesToLong(datamodel.ID)+"",Tools.BytesToLong(datamodel.Country)+"",datamodel.Type);
                }
            }
        }
    }

    private void sendMSG(String id,String nation,String type) {
        // TODO Auto-generated method stub
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("nation", nation);
        bundle.putString("type", type);
        Message msg = new Message();
        msg.what = 	Lf134KManager.LF;
        msg.setData(bundle);
        this.close();
        this.interrupt();
        this.handler.sendMessage(msg);
    }

    private void sendLog (String key, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putString("log", message);
        Message msg = new Message();
        msg.what = 	RFIDService.LOG;
        msg.setData(bundle);
        this.handler.sendMessage(msg);
    }

     public void scan() {
        Log.e("RFIDThread", "scan");
        this.sendLog("RFIDThread", "scan");
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
        }, (long) (5000));
    }

    public void close() {
        this.reader.Close();
    }
/*
    public void sendTo(String data) {
        Intent toBack = new Intent();
        toBack.setAction("android.rfid.INPUT");
        toBack.putExtra("data", data);
        toBack.putExtra("enter", true);
        this.mContext.sendBroadcast(toBack);
    }
 */

}
