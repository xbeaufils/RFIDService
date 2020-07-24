package fr.nemesys.service.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeyReceiver extends BroadcastReceiver {
    private String TAG = "KeyReceiver";

    public void onReceive(Context context, Intent intent) {
        int keyCode = intent.getIntExtra("keyCode", 0);
        boolean keyDown = intent.getBooleanExtra("keydown", false);
        Log.e(this.TAG, "KEYcODE = " + keyCode + ", Down = " + keyDown);
        ScanConfig config = new ScanConfig(context);
        if (keyDown && config.isOpen()) {
            Intent toService = new Intent(context, RFIDService.class);
            switch (keyCode) {
                case 131:
                    if (config.isF1()) {
                        context.startService(toService);
                        return;
                    }
                    return;
                case 132:
                    if (config.isF2()) {
                        context.startService(toService);
                        return;
                    }
                    return;
                case 133:
                    if (config.isF3()) {
                        context.startService(toService);
                        return;
                    }
                    return;
                case 134:
                    if (config.isF4()) {
                        context.startService(toService);
                        return;
                    }
                    return;
                case 135:
                    if (config.isF5()) {
                        context.startService(toService);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

}
