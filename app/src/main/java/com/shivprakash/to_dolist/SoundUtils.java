package com.shivprakash.to_dolist;

import android.view.SoundEffectConstants;
import android.view.View;

public class SoundUtils {
    public static void click(View v){
        if (v != null && v.isSoundEffectsEnabled()) {
            v.playSoundEffect(SoundEffectConstants.CLICK);
        }
    }
}
