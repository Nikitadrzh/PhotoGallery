package ru.nikitadrzh.photogallery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Nekit on 30.01.2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobPollService extends JobService {
    private static final String TAG = "JobPollService";//константа для debugger

    private String query;
    private String lastResultId;
    private Context jobContext;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!isNetworkAvailableAndConnected()) {//выход из метода при отсутствии сети
            return false;
        }
        jobContext = this;
        query = QueryPreferences.getStoredQuery(this);//query ТОЛЬКО для этого
        // контекста
        lastResultId = QueryPreferences.getLastResultId(this);//id ТОЛЬКО для этого
        // контекста
        new PollTask().execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {
        List<GalleryItem> items;//список, распарсенный из полученного JSON
        NotificationManager notificationManager;

        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos(); //получаем последние фото
            } else {
                items = new FlickrFetchr().searchPhotos(query);//находим фото по query
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (items.size() == 0) {//если список нулевой, то return
                return;
            }
            String resultId = items.get(0).getId();//получаем первый результат
            if (resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelId = "new_photo_channel_notification";
                    CharSequence channelName = "New Photo Channel";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel notificationChannel =
                            new NotificationChannel(channelId, channelName, importance);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                }

                Notification notification = new NotificationCompat
                        .Builder(getApplicationContext(), "new_photo_channel_notification")
                        .setTicker(getResources().getString(R.string.new_pictures_title))//бегущая
                        // строка
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)//системная иконка
                        .setContentTitle("Old Photo Result")
                        .setContentText(getResources().getString(R.string.new_pictures_text))
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                PhotoGalleryActivity.newIntent(getApplicationContext()), 0))
                        .setAutoCancel(true)
                        .build();
//                NotificationManagerCompat.from(getApplicationContext())//тут по сути 2 менеджера
//                        // работают, поэтому оповещение двойное
//                        .notify(0, notification);//создается
                // notificationManager и отправляется notification
                if (notificationManager != null) {
                    notificationManager.notify(0, notification);
                }
            } else {
                Log.i(TAG, "Got a new result: " + resultId);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelId = "new_photo_channel_notification";
                    CharSequence channelName = "New Photo Channel";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel notificationChannel =
                            new NotificationChannel(channelId, channelName, importance);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                }

                Notification notification = new NotificationCompat
                        .Builder(getApplicationContext(), "new_photo_channel_notification")
                        .setTicker(getResources().getString(R.string.new_pictures_title))//бегущая
                        // строка
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)//системная иконка
                        .setContentTitle(getResources().getString(R.string.new_pictures_title))
                        .setContentText(getResources().getString(R.string.new_pictures_text))
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                PhotoGalleryActivity.newIntent(getApplicationContext()), 0))
                        .setAutoCancel(true)
                        .build();
//                NotificationManagerCompat.from(getApplicationContext())//тут по сути 2 менеджера
//                        // работают, поэтому оповещение двойное
//                        .notify(0, notification);//создается
                // notificationManager и отправляется notification
                if (notificationManager != null) {
                    notificationManager.notify(0, notification);
                }
            }
            QueryPreferences.setLastResultId(jobContext, resultId);//сохраняем в общих настройках
        }
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
