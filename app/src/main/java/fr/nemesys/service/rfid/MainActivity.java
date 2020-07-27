package fr.nemesys.service.rfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.nemesys.service.rfid.ScanConfig;
import fr.nemesys.service.rfid.RFIDService;
import com.handheld.LF134K.Lf134KManager;
import java.util.Timer;
import java.util.TimerTask;

    public class MainActivity extends AppCompatActivity {

        private final int MSG_CANSEL_DIALOG = 1003;
        private Button buttonMin;
        private Button buttonPlus;
        private Button buttonSet;
        private CheckBox checkCirculate;
        private CheckBox checkRepeat;
        private Button readBtn;
        /* access modifiers changed from: private */
        public Dialog dialogLoading;
        /* access modifiers changed from: private */
        public Dialog dialogOther;
        /* access modifiers changed from: private */
        public EditText editText_time;
        /* access modifiers changed from: private */
        public EditText editUserChar;
        /* access modifiers changed from: private */
        public EditText editValues;
        /* access modifiers changed from: private */
        public String enterStr;
        /* access modifiers changed from: private */
        public String[] fixArray;
        /* access modifiers changed from: private */
        public Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1003:
                        MainActivity.this.dialogLoading.cancel();
                        MainActivity.this.scanConfig.setOpen(true);
                        return;
                    default:
                        return;
                }
            }
        };
        /* access modifiers changed from: private */
        public String noneStr;
        private Switch openSwitch;
        /* access modifiers changed from: private */
        public String otherStr;
        /* access modifiers changed from: private */
        public ScanConfig scanConfig;
        /* access modifiers changed from: private */
        public String spaceStr;
        private Spinner spinnerPrefix;
        private Spinner spinnerSurfix;
        /* access modifiers changed from: private */
        public String tabStr;
        private Timer timer;
        /* access modifiers changed from: private */
        public TextView tvPrefix;
        /* access modifiers changed from: private */
        public TextView tvSurfix;
        /* access modifiers changed from: private */
        public int value = 26;

        public Button buttonRead;

        private void SetScreenReceiver() {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("android.intent.action.SCREEN_OFF");
            mFilter.addAction("android.rfid.INPUT");
            registerReceiver(this.boucleReceiver, mFilter);
        }

        private void setBoucleReceiver() {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("android.rfid.INPUT");
            registerReceiver(this.boucleReceiver, mFilter);
        }

        private BroadcastReceiver boucleReceiver = new BroadcastReceiver() {
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.rfid.INPUT")) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String data = extras.getString("data");
                        Log.d("boucleReceiver", data);
                        boolean enterFlag = extras.getBoolean("enter");
                        Log.d("boucleReceiver", String.valueOf(enterFlag));
                    }
                }
            }
        };

        /* access modifiers changed from: protected */
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            this.scanConfig = new ScanConfig(this);
            initView();
            setTimer();
            try {
                StringBuilder stringBuilder = new StringBuilder(getString(R.string.app_name));
                stringBuilder.append("-");
                stringBuilder.append(String.valueOf(Lf134KManager.Port));
                stringBuilder.append(",");
                stringBuilder.append(Lf134KManager.getPowerString());
                stringBuilder.append("-v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                setTitle(stringBuilder.toString());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        private void setTimer() {
            if (this.timer == null) {
                this.timer = new Timer();
                this.timer.schedule(new TimerTask() {
                    public void run() {
                        String timest = MainActivity.this.editText_time.getText().toString();
                        if (timest != null && timest != "") {
                            try {
                                MainActivity.this.scanConfig.setTime(Integer.valueOf(timest).intValue());
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }, 200, 200);
            }
        }

        private void initView() {
            this.tabStr = getResources().getString(R.string.tab);
            this.spaceStr = getResources().getString(R.string.space);
            this.enterStr = getResources().getString(R.string.enter);
            this.noneStr = getResources().getString(R.string.none);
            this.otherStr = getResources().getString(R.string.other);
            this.fixArray = new String[]{this.tabStr, this.spaceStr, this.enterStr, this.noneStr, this.otherStr};
            this.tvPrefix = (TextView) findViewById(R.id.text_prefix);
            this.editText_time = (EditText) findViewById(R.id.editText_circulatetime);
            this.openSwitch = (Switch) findViewById(R.id.switch_scan);
            this.buttonRead = (Button) findViewById(R.id.button_read);
            this.spinnerPrefix = (Spinner) findViewById(R.id.spinner_prefix);
            this.spinnerSurfix = (Spinner) findViewById(R.id.spinner_surfix);

            this.checkRepeat = (CheckBox) findViewById(R.id.checkBox_repeat);
            this.checkCirculate = (CheckBox) findViewById(R.id.checkBox_circulate);
            this.openSwitch.setChecked(this.scanConfig.isOpen());
            this.checkRepeat.setChecked(this.scanConfig.isRepeat());
            this.checkCirculate.setChecked(this.scanConfig.isCirculate());
            this.editText_time.setText(new StringBuilder(String.valueOf(this.scanConfig.getTime())).toString());
            this.spinnerPrefix.setSelection(this.scanConfig.getPrefixIndex());
            this.spinnerSurfix.setSelection(this.scanConfig.getSurfixIndex());

            this.checkRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MainActivity.this.scanConfig.setRepeat(isChecked);
                }
            });
            this.checkCirculate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MainActivity.this.scanConfig.setCirculate(isChecked);
                }
            });

            this.openSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        MainActivity.this.createLoaddingDialog();
                        MainActivity.this.startService(new Intent(MainActivity.this, RFIDService.class));
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                Message msg = new Message();
                                msg.what = 1003;
                                MainActivity.this.mHandler.sendMessage(msg);
                            }
                        }, 2000);
                        return;
                    }
                    Intent toKill = new Intent();
                    toKill.setAction("android.rfid.KILL_SERVER");
                    toKill.putExtra("kill", true);
                    MainActivity.this.sendBroadcast(toKill);
                    MainActivity.this.scanConfig.setOpen(false);
                    RFIDService.Close();
                }
            });
            this.spinnerPrefix.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (MainActivity.this.fixArray[position].equals(MainActivity.this.tabStr)) {
                        MainActivity.this.scanConfig.setPrefix("\t");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.spaceStr)) {
                        MainActivity.this.scanConfig.setPrefix(" ");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.enterStr)) {
                        MainActivity.this.scanConfig.setPrefix("0A0D");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.noneStr)) {
                        MainActivity.this.scanConfig.setPrefix("");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.otherStr)) {
                        MainActivity.this.createOtherDialog(true);
                    }
                    MainActivity.this.scanConfig.setPrefixIndex(position);
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.e("nothing selected", "");
                }
            });
            this.spinnerSurfix.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (MainActivity.this.fixArray[position].equals(MainActivity.this.tabStr)) {
                        MainActivity.this.scanConfig.setSurfix("\t");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.spaceStr)) {
                        MainActivity.this.scanConfig.setSurfix(" ");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.enterStr)) {
                        MainActivity.this.scanConfig.setSurfix("0A0D");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.noneStr)) {
                        MainActivity.this.scanConfig.setSurfix("");
                    } else if (MainActivity.this.fixArray[position].equals(MainActivity.this.otherStr)) {
                        MainActivity.this.createOtherDialog(false);
                    }
                    MainActivity.this.scanConfig.setSurfixIndex(position);
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            this.buttonMin = (Button) findViewById(R.id.button_min);
            this.buttonPlus = (Button) findViewById(R.id.button_plus);
            this.buttonPlus.setOnClickListener(new View.OnClickListener() {
               public void onClick(View arg0) {
                   MainActivity.this.createLoaddingDialog();
                   MainActivity.this.startService(new Intent(MainActivity.this, RFIDService.class));
                   new Timer().schedule(new TimerTask() {
                       public void run() {
                           Message msg = new Message();
                           msg.what = 1003;
                           MainActivity.this.mHandler.sendMessage(msg);
                       }
                   }, 2000);
                   return;
                   /*
                   Intent toKill = new Intent();
                   toKill.setAction("android.rfid.KILL_SERVER");
                   toKill.putExtra("kill", true);
                   MainActivity.this.sendBroadcast(toKill);
                   MainActivity.this.scanConfig.setOpen(false);
                   RFIDService.Close();

                    */
               }
            });
            initView2();
        }

        private void initView2() {
            this.buttonSet = (Button) findViewById(R.id.button_set);
            this.editValues = (EditText) findViewById(R.id.editText_power);
            this.buttonMin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    if (MainActivity.this.value > 16) {
                        MainActivity mainActivity = MainActivity.this;
                        mainActivity.value = mainActivity.value - 1;
                    } else {
                        MainActivity.this.value = 26;
                    }
                    MainActivity.this.editValues.setText(new StringBuilder(String.valueOf(MainActivity.this.value)).toString());
                }
            });
            this.buttonPlus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    if (MainActivity.this.value < 26) {
                        MainActivity mainActivity = MainActivity.this;
                        mainActivity.value = mainActivity.value + 1;
                    } else {
                        MainActivity.this.value = 16;
                    }
                    MainActivity.this.editValues.setText(new StringBuilder(String.valueOf(MainActivity.this.value)).toString());
                }
            });
            this.buttonSet.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    if (MainActivity.this.scanConfig.setPower(MainActivity.this.value)) {
                        Toast.makeText(MainActivity.this.getApplicationContext(), R.string._setsuccess, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this.getApplicationContext(), R.string._setfail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            this.value = this.scanConfig.getPower();
            this.editValues.setText(new StringBuilder().append(this.value).toString());
            this.setBoucleReceiver();
        }

        /* access modifiers changed from: private */
        public void createLoaddingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, (ViewGroup) null));
            this.dialogLoading = builder.create();
            this.dialogLoading.setCancelable(false);
            this.dialogLoading.show();
        }

        /* access modifiers changed from: private */
        public void createOtherDialog(final boolean isPrefix) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_others, (ViewGroup) null);
            this.editUserChar = (EditText) view.findViewById(R.id.editText_others);
            builder.setView(view);
            builder.setTitle(getResources().getString(R.string.user_char));
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.dialogOther.cancel();
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String userChar = MainActivity.this.editUserChar.getText().toString();
                    if (isPrefix) {
                        MainActivity.this.tvPrefix.setText(userChar);
                        MainActivity.this.scanConfig.setPrefix(userChar);
                    } else {
                        MainActivity.this.tvSurfix.setText(userChar);
                        MainActivity.this.scanConfig.setSurfix(userChar);
                    }
                    MainActivity.this.dialogOther.cancel();
                }
            });
            this.dialogOther = builder.create();
            this.dialogOther.show();
        }
    }

