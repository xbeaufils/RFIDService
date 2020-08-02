package fr.nemesys.service.rfid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.PrintWriter;
import java.io.StringWriter;

import cn.pda.serialport.SerialPort;
import io.sentry.core.Sentry;

public class RFIDService extends Service  {
    private final IBinder binder = new RFIDBinder();

    /* access modifiers changed from: private */
    public static RFIDThread rfidThread = null;
    private String TAG = "RFIDService";
    public static int LOG = 2020;
    /* access modifiers changed from: private */
    public ScanConfig config;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class RFIDBinder extends Binder {
        RFIDService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RFIDService.this;
        }
    }
    /* access modifiers changed from: private */
    public  class RFIDHandler extends Handler {
        public void sendLog (String key, String message) {
            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            bundle.putString("log", message);
            Message msg = new Message();
            msg.what = 	RFIDService.LOG;
            msg.setData(bundle);
            this.sendMessage(msg);
        }

        public void handleMessage(Message msg) {
            Log.d(TAG, msg.toString());
            //RFIDService.this.sendLog("RFIDServiceMessage", msg.toString());
            if (msg.what == RFIDThread.LF) {
                Bundle bundle = msg.getData();
                String data = bundle.getString("id");
                if (data != null) {
                    RFIDService.this.sendToInput(bundle);
                }
            }
            if (msg.what == RFIDService.LOG) {
                Bundle bundle = msg.getData();
                String log = bundle.getString("log");
                String key = bundle.getString("key");
                RFIDService.this.sendLog(key, log);
            }
        }
    }
    public RFIDHandler handler = new RFIDHandler();

    private BroadcastReceiver killReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("kill2", false)) {
                Log.d(TAG, "killReceiver" );
                RFIDService.this.stopSelf();
            }
        }
    };

    private BroadcastReceiver readReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "readReceiver" );
            RFIDService.this.sendLog("readReceiver","readReceiver");
            String action = intent.getAction();
            if (action.equals("nemesys.rfid.LF134.read")) {
                if (RFIDService.rfidThread != null) {
                    try {
                        RFIDService.rfidThread.start();
                        RFIDService.rfidThread.scan();
                    } catch (Exception e) {
                        Log.i("readReceiver", "ScanThread error", e);
                        RFIDService.this.sendLog("RFID:OnstartCommand", getStackTrace(e) );
                    }
                }
                else {
                    RFIDService.this.sendLog("readReceiver","Thread not startd");
                    Log.e("readReceiver", "Thread not startd");
                }
            }
         }
    };

    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public void onCreate() {
        this.config = new ScanConfig(this);
        Util.initSoundPool(this);
        IntentFilter killfilter = new IntentFilter();
        killfilter.addAction("android.rfid.KILL_SERVER");
        registerReceiver(this.killReceiver, killfilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("nemesys.rfid.LF134.read");
        registerReceiver(this.readReceiver, filter);
        super.onCreate();
        Log.d(this.TAG, "open");
        this.sendLog(TAG, "open");
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.killReceiver);
        unregisterReceiver(this.readReceiver);
        Log.d(this.TAG, "close");
        this.sendLog(TAG, "close");
    }

     public int onStartCommand(Intent intent, int flags, int startId) {
        int com = intent.getIntExtra("port", -1);
        Log.d("RFID:OnstartCommand", " Port " + com);
        this.sendLog("RFID:OnstartCommand", " Port " + com);
        if (rfidThread == null) {
            try {
                rfidThread = new RFIDThread(this.handler, com,this);
                //rfidThread.start();
                //Log.d("RFIDService", "ScanThread start");
            } catch (Exception e) {
                Sentry.captureException(e);
                this.sendLog("RFID:OnstartCommand", getStackTrace(e) );
                Log.e("RFIDService", "ScanThread error", e);
            }
        } else {
            try {
                rfidThread.close();
                rfidThread = new RFIDThread(this.handler, com,this);
                //rfidThread.start();
            } catch (Exception e) {
                Sentry.captureException(e);
                this.sendLog("RFID:OnstartCommand", getStackTrace(e) );
                Log.e("RFIDService", "ScanThread error", e);
            }

           // rfidThread.scan();
        }
        //rfidThread.startFlag = true;
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendLog(String key, String message) {
        Intent logIntent = new Intent();
        logIntent.setAction("nemesys.rfid.LF134.log");
        logIntent.putExtra("key", key);
        logIntent.putExtra("log", message);
        sendBroadcast(logIntent);
    }

    public static void Close() {
        if (rfidThread != null) {
            rfidThread.close();
            rfidThread = null;
        }
    }

    /* access modifiers changed from: private */
    public void sendToInput(Bundle bundle) {
        try {
            String data = bundle.getString("id");
            String nation = bundle.getString("nation");
            String type = bundle.getString("type");
            int datalent = data.length();
            int nationlent = nation.length();
            for (int i = 0; i < 12 - datalent; i++) {
                data = "0" + data;
            }
            for (int j = 0; j < 3 - nationlent; j++) {
                nation = "0" + nation;
            }
            Intent toBack = new Intent();
            toBack.setAction("nemesys.rfid.LF134.result");
            toBack.putExtra("id", data);
            toBack.putExtra("nation", nation);
            toBack.putExtra("type", type);
            sendBroadcast(toBack);
            this.rfidThread.close();
            this.rfidThread.interrupt();
            this.rfidThread.runFlag = false;
        }
        catch (Exception e) {
            Sentry.captureException(e);
            this.sendLog("RFID:sendToInput", getStackTrace(e) );
        }
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
