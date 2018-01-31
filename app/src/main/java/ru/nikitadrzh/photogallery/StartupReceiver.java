package ru.nikitadrzh.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Nekit on 31.01.2018.
 */

public class StartupReceiver extends BroadcastReceiver {//широковещательный Приемник
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());//тут action -
        // BOOT_COMPLETED из манифеста

        boolean isOn = QueryPreferences.isAlarmOn(context);
        Log.i(TAG, "context is: " + context);
        Log.i(TAG, "isOn: " + isOn);
        PollService.setServiceAlarm(context, isOn);
    }
}
