package ru.nikitadrzh.photogallery;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Nekit on 27.01.2018.
 */

public class PollService extends IntentService {//служба опроса
    private static final String TAG = "PollService";//константа для debugger

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {//intent для создания службы, ставит команду в
        // очередь
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {//вызывается, когда подходит очередь
        if (!isNetworkAvailableAndConnected()) {//выход из метода при отсутствии сети
            return;
        }
        Log.i(TAG, "Received an intent: " + intent);
    }

    private boolean isNetworkAvailableAndConnected() {//проверяется доступность сети
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                connectivityManager.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

}
