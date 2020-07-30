package fr.nemesys.service.rfid;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class Util {

    public static Context context;
    public static SoundPool sp;
    public static Map<Integer, Integer> suondMap;

    public static void initSoundPool(Context context2) {
        context = context2;
        sp = (new SoundPool.Builder()).build();// new SoundPool(1, 3, 1);
        suondMap = new HashMap<>();
        //suondMap.put(1, Integer.valueOf(sp.load(context2, R.raw.msg, 1)));
    }

    public static void play(int sound, int number) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = (float) am.getStreamMaxVolume(3);
        float audioCurrentVolume = (float) am.getStreamVolume(3);
        float f = audioCurrentVolume / audioMaxVolume;
        sp.play(suondMap.get(Integer.valueOf(sound)).intValue(), audioCurrentVolume, audioCurrentVolume, 1, number, 1.0f);
    }
}
