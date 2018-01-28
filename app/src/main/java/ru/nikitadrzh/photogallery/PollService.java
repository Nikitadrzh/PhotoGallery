package ru.nikitadrzh.photogallery;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nekit on 27.01.2018.
 */

public class PollService extends IntentService {//служба опроса
    private static final String TAG = "PollService";//константа для debugger
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);//60 сек

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {//intent для создания службы, ставит команду в
        // очередь
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {//настройка AlarmManager
        Intent intent = PollService.newIntent(context);//context, тк метод статический и может
        // вызываться из разного контекста
        PendingIntent pendingIntent = PendingIntent
                .getService(context, 0, intent, 0);//тут упаковывается "пожеление"
        // запуска службы
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);//системная служба, отправляющая интенты
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS, pendingIntent);//установка повтора запуска службы,
            // ELAPSED_REALTIME - врямя используется после включения/пробуждения без вывода из сна
        } else {
            alarmManager.cancel(pendingIntent);//отменяем пожелание запуска службы
            pendingIntent.cancel();//дополнительная отмена, полезно делать
        }
    }

    public static boolean isServiceAlarmOn(Context context) {//метод для проверки существует ли
        // PendingIntent
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_NO_CREATE);//с этим флагом, если объект PendingIntent не
        // существует, то возвращается null
        return pendingIntent != null;//если pendingIntent null, то возвращается false
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {//вызывается, когда подходит очередь
        if (!isNetworkAvailableAndConnected()) {//выход из метода при отсутствии сети
            return;
        }
        String query = QueryPreferences.getStoredQuery(this);//query ТОЛЬКО для этого
        // контекста
        String lastResultId = QueryPreferences.getLastResultId(this);//id ТОЛЬКО для этого
        // контекста
        List<GalleryItem> items;//список, распарсенный из полученного JSON

        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos(); //получаем последние фото
        } else {
            items = new FlickrFetchr().searchPhotos(query);//находим фото по query
        }

        if (items.size() == 0) {//если список нулевой, то return
            return;
        }
        String resultId = items.get(0).getId();//получаем первый результат

        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);
        }

        QueryPreferences.setLastResultId(this, resultId);//сохраняем в общих настройках
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
