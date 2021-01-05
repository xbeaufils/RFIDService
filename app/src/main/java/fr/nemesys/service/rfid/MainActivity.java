package fr.nemesys.service.rfid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.handheld.LF134K.LF134KManager;

import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.ISerialPort;
import io.sentry.core.Sentry;

public class MainActivity extends AppCompatActivity {
        @Override
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(this.boucleReceiver);
            unregisterReceiver(this.logReceiver);
        }

        private final int MSG_CANSEL_DIALOG = 1003;
        /* access modifiers changed from: private */
        public Dialog dialogLoading;
         /* access modifiers changed from: private */
        public Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CANSEL_DIALOG:
                        MainActivity.this.dialogLoading.cancel();
                        return;
                    default:
                        return;
                }
            }
        };
         /* access modifiers changed from: private */
        public ScanConfig scanConfig;
        private Timer timer;

        private Button buttonRead;
        private Button buttonClear;
        private TextView textViewData;
        private TextView textViewLog;
        private RadioGroup rdgPort;

        private void setBoucleReceiver() {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("nemesys.rfid.LF134.result");
            registerReceiver(this.boucleReceiver, mFilter);
        }

        private BroadcastReceiver boucleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                Log.d("boucleReceiver", "action " + action);
                MainActivity.this.WriteLog("boucleReceiver","action " + action );
                if (action.equals("nemesys.rfid.LF134.result")) {
                    Bundle extras = intent.getExtras();
                    Log.d("boucleReceiver", "bundle " + extras);
                    if (extras != null) {
                        String id = extras.getString("id");
                        String nation = extras.getString("nation");
                        String marquage =  extras.getString("marquage");
                        String boucle =  extras.getString("boucle");
                        Log.d("boucleReceiver", "id " + id);
                        MainActivity.this.WriteLog("boucleReceiver", "id " + id);
                        if (MainActivity.this.dialogLoading != null) {
                            MainActivity.this.textViewData.setText("nation :" + nation + " id " + id
                                    + "\nboucle " + boucle + " marquage " + marquage);
                            MainActivity.this.dialogLoading.cancel();
                        }
                    }
                }
            }
        };

        private void setLogReceiver() {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("nemesys.rfid.LF134.log");
            registerReceiver(this.logReceiver, mFilter);
        }

        private BroadcastReceiver logReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("logReceiver", "action " + action);
                if (action.equals("nemesys.rfid.LF134.log")) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String logKey = extras.getString("key");
                        String log = extras.getString("log");
                        MainActivity.this.WriteLog(logKey , log );
                    }
                }
            }
        };

        public void WriteLog (String logKey, String log) {
            MainActivity.this.textViewLog.append(logKey + ":" + log + "\n");
        }

        /* access modifiers changed from: protected */
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
//            this.scanConfig = new ScanConfig(this);
            initView();
            try {
                StringBuilder stringBuilder = new StringBuilder(getString(R.string.app_name));
                stringBuilder.append("-");
                stringBuilder.append(String.valueOf(LF134KManager.Port));
                stringBuilder.append(",");
                stringBuilder.append(LF134KManager.Power);
                stringBuilder.append("-v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                setTitle(stringBuilder.toString());
            } catch (PackageManager.NameNotFoundException e) {
                Sentry.captureException(e);
                e.printStackTrace();
            }

        }

        private void initView() {
            this.setBoucleReceiver();
            this.setLogReceiver();
            this.textViewData = (TextView) findViewById(R.id.textViewData);
            this.textViewLog = (TextView) findViewById(R.id.textViewLog);
            this.buttonRead = (Button) findViewById(R.id.button_read);
            //this.rdgPort = (RadioGroup) findViewById(R.id.rdgPort);
            try {
                Intent intentRfid = new Intent();
                //intentRfid.putExtra("port", com);
                intentRfid.setComponent(new ComponentName("fr.nemesys.service.rfid", "fr.nemesys.service.rfid.RFIDService"));
                //MainActivity.this.startService(intentRfid);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intentRfid);
                } else {
                    startService(intentRfid);
                }
            } catch (Exception e) {
                Sentry.captureException(e);
                MainActivity.this.WriteLog("initView", RFIDService.getStackTrace(e));
            }
            this.buttonRead.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Log.d("READ", "Click");
                    MainActivity.this.createLoadingDialog();
                    //Intent intentRfid = new Intent(MainActivity.this, RFIDService.class);
                    //try {Thread.sleep(1000);}
                    //catch(InterruptedException ex) { Thread.currentThread().interrupt();}
                    try {
                        Intent toRead = new Intent();
                        toRead.setAction("nemesys.rfid.LF134.read");
                        MainActivity.this.sendBroadcast(toRead);
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                Message msg = new Message();
                                msg.what = MSG_CANSEL_DIALOG;
                                MainActivity.this.mHandler.sendMessage(msg);
                            }
                        }, 2000);
                    } catch (Exception e) {
                        Sentry.captureException(e);
                        MainActivity.this.WriteLog("boucleReceiver", RFIDService.getStackTrace(e));
                    }
                    return;
                }
            });
            this.buttonClear = (Button) findViewById(R.id.button_clear);
            this.buttonClear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                MainActivity.this.textViewLog.setText(null);
                MainActivity.this.textViewData.setText(null);
                }
            });
            /*
            Intent intentRfid = new Intent();
            intentRfid.setComponent(new ComponentName("fr.nemesys.service.rfid", "fr.nemesys.service.rfid.RFIDService"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intentRfid);
            }else {
                startService(intentRfid);
            }
            */
            //this.startService(new Intent(MainActivity.this, RFIDService.class));
         }

        /* access modifiers changed from: private */
        public void createLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, (ViewGroup) null));
            this.dialogLoading = builder.create();
            this.dialogLoading.setCancelable(false);
            this.dialogLoading.show();
        }

     }

