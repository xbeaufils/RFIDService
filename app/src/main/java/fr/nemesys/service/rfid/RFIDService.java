package fr.nemesys.service.rfid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.handheld.LF134K.LF134KManager;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.sentry.core.Sentry;

public class RFIDService extends Service  {
    private final IBinder binder = new RFIDBinder();

    private static final String ANDROID_CHANNEL_ID = "fr.nemesys.service.rfid";
    private static final int NOTIFICATION_ID = 555;
    /* access modifiers changed from: private */
    public static LF134KManager reader;
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
        String TAG = "RFIDHandler";
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
            if (msg.what == LF134KManager.MSG_RFID_134K) {
                Bundle bundle = msg.getData();
                String  data = bundle.getString(LF134KManager.KEY_134K_ID);
                //String data = bundle.getString("id");
                if (data != null) {
                    String nation = bundle.getString(LF134KManager.KEY_134K_COUNTRY);
                    String type = bundle.getString("type");
                    int datalent = data.length();
                    int nationlent = nation.length();
                    for (int i = 0; i < 11 - datalent; i++) {
                        data = "0" + data;
                    }
                    for (int j = 0; j < 3 - nationlent; j++) {
                        nation = "0" + nation;
                    }
                    bundle.putString(LF134KManager.KEY_134K_ID, data);
                    bundle.putString(LF134KManager.KEY_134K_COUNTRY, nation);
                    RFIDService.this.sendToInput(bundle);
                    RFIDService.this.reader.stopRead();
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
                if (RFIDService.reader != null) {
                    try {
                        RFIDService.reader.startRead();
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
        /*

         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "RFID Service";
            NotificationChannel chan = new NotificationChannel(ANDROID_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Lecture RFID")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);        }
        else
            startForeground(1, new Notification());
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

     @RequiresApi(api = Build.VERSION_CODES.O)
     public int onStartCommand(Intent intent, int flags, int startId) {
        int com = intent.getIntExtra("port", -1);
        Log.d("RFID:OnstartCommand", " Port " + com);
        this.sendLog("RFID:OnstartCommand", " Port " + com);
        if (reader == null) {
            try {
                reader = new LF134KManager(handler);
            } catch (Exception e) {
                Sentry.captureException(e);
                this.sendLog("RFID:OnstartCommand", getStackTrace(e) );
                Log.e("RFIDService", "ScanThread error", e);
            }
        } else {
            try {
                reader.Close();
                reader = new LF134KManager(handler);
                //rfidThread.start();
            } catch (Exception e) {
                Sentry.captureException(e);
                this.sendLog("RFID:OnstartCommand", getStackTrace(e) );
                Log.e("RFIDService", "ScanThread error", e);
            }
        }
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                     .setContentTitle(getString(R.string.app_name))
                     .setContentText("Recherche de boucle...")
                     .setAutoCancel(true);
             Notification notification = builder.build();
             startForeground(NOTIFICATION_ID, notification);
         } else {
             NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                     .setContentTitle(getString(R.string.app_name))
                     .setContentText("Recherche de boucle...")
                     .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                     .setAutoCancel(true);
             Notification notification = builder.build();
             startForeground(NOTIFICATION_ID, notification);
         }
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
        if (reader != null) {
            reader.Close();
            reader = null;
        }
    }

    /* access modifiers changed from: private */
    public void sendToInput(Bundle bundle) {
        try {
            /*
            int iData = bundle.getInt(LF134KManager.KEY_134K_ID);
            Intent toBack = new Intent();
            toBack.setAction("nemesys.rfid.LF134.result");
            if (iData != 0) {
                String data = new Integer(bundle.getInt(LF134KManager.KEY_134K_ID)).toString();
                int datalent = data.length();
                for (int i = 0; i < 12 - datalent; i++) {
                    data = "0" + data;
                }
                toBack.putExtra("id", data);
                Log.d(TAG, "sendToInput: " + data);
            }
            int iNation = bundle.getInt(LF134KManager.KEY_134K_COUNTRY);
            if (iNation != 0) {
                String nation = new Integer(iNation).toString();
                String type = bundle.getString("type");
                int nationlent = nation.length();
                for (int j = 0; j < 3 - nationlent; j++) {
                    nation = "0" + nation;
                }
                toBack.putExtra("nation", nation);
                toBack.putExtra("type", type);
            }
            */
            String data = bundle.getString(LF134KManager.KEY_134K_ID);
            String nation = bundle.getString(LF134KManager.KEY_134K_COUNTRY);
            Intent toBack = new Intent();
            toBack.setAction("nemesys.rfid.LF134.result");
            // obj {"id":"3530654 00345","nation":"156","boucle":"5400345","marquage":"35306"}
            toBack.putExtra("marquage",data.substring(0,6 ));
            toBack.putExtra("boucle",data.substring(6) );
            toBack.putExtra(LF134KManager.KEY_134K_ID, data);
            toBack.putExtra(LF134KManager.KEY_134K_COUNTRY, nation);
            sendBroadcast(toBack);
            this.reader.stopRead();
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
