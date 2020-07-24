package fr.nemesys.service.rfid;
import android.content.Context;
import android.content.SharedPreferences;

    public class ScanConfig {
        private Context context;

        public ScanConfig(Context context2) {
            this.context = context2;
        }

        public boolean isOpen() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("open", false);
        }

        public void setOpen(boolean open) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("open", open);
            editor.commit();
        }

        public String getPrefix() {
            return this.context.getSharedPreferences("scanConfig", 0).getString("prefix", "");
        }

        public void setPrefix(String prefix) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putString("prefix", prefix);
            editor.commit();
        }

        public int getPrefixIndex() {
            return this.context.getSharedPreferences("scanConfig", 0).getInt("prefixIndex", 3);
        }

        public void setPrefixIndex(int prefixIndex) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putInt("prefixIndex", prefixIndex);
            editor.commit();
        }

        public String getSurfix() {
            return this.context.getSharedPreferences("scanConfig", 0).getString("surfix", "0A0D");
        }

        public void setSurfix(String surfix) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putString("surfix", surfix);
            editor.commit();
        }

        public int getSurfixIndex() {
            return this.context.getSharedPreferences("scanConfig", 0).getInt("surfixIndex", 2);
        }

        public void setSurfixIndex(int surfixIndex) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putInt("surfixIndex", surfixIndex);
            editor.commit();
        }

        public boolean isVoice() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("voice", true);
        }

        public void setVoice(boolean voice) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("voice", voice);
            editor.commit();
        }

        public boolean isRepeat() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("repeat", false);
        }

        public void setRepeat(boolean repeat) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("repeat", repeat);
            editor.commit();
        }

        public boolean isCirculate() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("circulate", false);
        }

        public void setCirculate(boolean circulate) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("circulate", circulate);
            editor.commit();
        }

        public boolean isF1() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("f1", true);
        }

        public void setF1(boolean f1) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("f1", f1);
            editor.commit();
        }

        public boolean isF2() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("f2", true);
        }

        public void setF2(boolean f2) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("f2", f2);
            editor.commit();
        }

        public boolean isF3() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("f3", true);
        }

        public void setF3(boolean f3) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("f3", f3);
            editor.commit();
        }

        public boolean isF4() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("f4", true);
        }

        public void setF4(boolean f4) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("f4", f4);
            editor.commit();
        }

        public boolean isF5() {
            return this.context.getSharedPreferences("scanConfig", 0).getBoolean("f5", true);
        }

        public void setF5(boolean f5) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putBoolean("f5", f5);
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

        public boolean setPower(int power) {
            SharedPreferences.Editor editor = this.context.getSharedPreferences("scanConfig", 0).edit();
            editor.putInt("power", power);
            editor.commit();
            return true;
        }
    }

