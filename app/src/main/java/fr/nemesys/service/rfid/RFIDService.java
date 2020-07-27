package fr.nemesys.service.rfid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class RFIDService extends Service {
    private final IBinder binder = new RFIDBinder();

    /* access modifiers changed from: private */
    public static RFIDThread rfidThread = null;
    private String TAG = "RFIDService";
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
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            RFIDService.this.prefixStr = RFIDService.this.config.getPrefix();
            RFIDService.this.surfixStr = RFIDService.this.config.getSurfix();
            if (msg.what == RFIDThread.UHF) {
                String data = msg.getData().getString("data");
                String count = msg.getData().getString("c");
                if (data != null) {
                    if ("0A0D".equals(RFIDService.this.prefixStr)) {
                        RFIDService.this.sendToInput("", true);
                    } else {
                        RFIDService.this.sendToInput(RFIDService.this.prefixStr, false);
                    }
                    RFIDService.this.sendToInput(data, false);
                    if ("0A0D".equals(RFIDService.this.surfixStr)) {
                        RFIDService.this.sendToInput("", true);
                    } else {
                        RFIDService.this.sendToInput(RFIDService.this.surfixStr, false);
                    }
                    RFIDService.this.showToast(count);
                    if (RFIDService.this.config.isVoice()) {
                        Util.play(1, 0);
                    }
                }
            }
        }
    };

    private BroadcastReceiver killReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("kill2", false)) {
                Log.d(TAG, "killReceiver" );
                RFIDService.this.stopSelf();
            }
        }
    };
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent intent) {
            Log.d(TAG, "mScreenReceiver" );
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                if (RFIDService.rfidThread == null) {
                    try {
                        RFIDService.rfidThread = new RFIDThread(RFIDService.this.handler, RFIDService.this);
                        RFIDService.rfidThread.start();
                        Log.i("KeyReceiver", "ScanThread start");
                    } catch (Exception e) {
                        Log.i("KeyReceiver", "ScanThread error");
                    }
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action) && RFIDService.rfidThread != null) {
                RFIDService.rfidThread.close();
                RFIDService.rfidThread = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public String prefixStr;
    /* access modifiers changed from: private */
    public String surfixStr;
    private Toast toast;

    public IBinder onBind(Intent arg0) {
        return binder;
    }

    /* access modifiers changed from: private */
    public void showToast(String count) {
        if (this.toast == null) {
            this.toast = Toast.makeText(this, count, Toast.LENGTH_SHORT);
        } else {
            this.toast.setText(count);
        }
        this.toast.show();
    }

    public void onCreate() {
        this.config = new ScanConfig(this);
        Util.initSoundPool(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.KILL_SERVER");
        registerReceiver(this.killReceiver, filter);
        SetScreenReceiver();
        super.onCreate();
        Log.e(this.TAG, "open");
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.killReceiver);
        Log.e(this.TAG, "close");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (rfidThread == null) {
            try {
                rfidThread = new RFIDThread(this.handler, this);
                rfidThread.start();
                Log.e("KeyReceiver", "ScanThread start");
            } catch (Exception e) {
                Log.e("KeyReceiver", "ScanThread error");
            }
        } else {
            rfidThread.scan();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void Close() {
        if (rfidThread != null) {
            rfidThread.close();
            rfidThread = null;
        }
    }

    /* access modifiers changed from: private */
    public void sendToInput(String data, boolean enterFlag) {
        Intent toBack = new Intent();
        toBack.setAction("android.rfid.INPUT");
        toBack.putExtra("data", data);
        toBack.putExtra("enter", enterFlag);
        sendBroadcast(toBack);
    }

    private void SetScreenReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("android.intent.action.SCREEN_OFF");
        mFilter.addAction("android.intent.action.SCREEN_ON");
        registerReceiver(this.mScreenReceiver, mFilter);
    }

}
