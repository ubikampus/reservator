package com.futurice.android.reservator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KioskStateReceiver extends BroadcastReceiver {

    public static final String KIOSK_ON_INTENT_NAME = "com.futurice.android.reservator.KIOSK_ON";
    public static final String KIOSK_OFF_INTENT_NAME = "com.futurice.android.reservator.KIOSK_OFF";


    public static final String KIOSK_ON = "KIOSK_ON";
    public static final String KIOSK_OFF = "KIOSK_OFF";

    public KioskStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("KioskStateReceiver", intent.getAction());
        if (KIOSK_ON_INTENT_NAME.equalsIgnoreCase(intent.getAction())) {
            context.sendBroadcast(new Intent(KIOSK_ON));
        } else if (KIOSK_OFF_INTENT_NAME.equalsIgnoreCase(intent.getAction())) {
            context.sendBroadcast(new Intent(KIOSK_OFF));
        }
    }
}