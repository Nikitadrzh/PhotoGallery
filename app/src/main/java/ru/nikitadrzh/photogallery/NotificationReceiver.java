package ru.nikitadrzh.photogallery;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Nekit on 31.01.2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received result: " + getResultCode()); //result code устанавливвается при
        // вызове PollService.showBackgroundNotification
        if (getResultCode() != RESULT_OK) {
            return; // если активность переднего плана отменила рассылку
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(requestCode, notification);
    }
}
