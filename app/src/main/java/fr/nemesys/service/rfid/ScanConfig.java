package fr.nemesys.service.rfid;
import android.content.Context;
import android.content.SharedPreferences;

    public class ScanConfig {
        private Context context;

        public ScanConfig(Context context2) {
            this.context = context2;
        }

          public String getPrefix() {
            return this.context.getSharedPreferences("scanConfig", 0).getString("prefix", "");
        }


        public String getSurfix() {
            return this.context.getSharedPreferences("scanConfig", 0).getString("surfix", "0A0D");
        }


        public boolean isCirculate() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("circulate", false);
        }

        public void setCirculate(boolean circulate) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("circulate", circulate);
            editor.commit();
        }

        public int getTime() {
            return this.context.getSharedPreferences("scanConfig", 0).getInt("time", 5);
        }

        public void setTime(int time) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putInt("time", time);
            editor.commit();
        }

        public int getPower() {
            return this.context.getSharedPreferences("scanConfig", 0).getInt("power", 26);
        }
    }

