package ru.nikitadrzh.photogallery;

import android.app.Notification;
import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobRequest;

/**
 * Created by Nekit on 29.01.2018.
 */

public class DemoSyncJob extends Job {
    public static final String TAG = "job_demo_tag";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Notification notification = new NotificationCompat
                .Builder(getContext(), "new_photo_channel_notification")
                .setTicker(getContext().getResources().getString(R.string.new_pictures_title))//бегущая
                // строка
                .setSmallIcon(android.R.drawable.ic_menu_report_image)//системная иконка
                .setContentTitle("Evernote")
                .setContentText(getContext().getResources().getString(R.string.new_pictures_text))
                .setContentIntent(PendingIntent.getActivity(getContext(), 0,
                        PhotoGalleryActivity.newIntent(getContext()), 0))
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat.from(getContext()).notify(0, notification);//создается
        // notificationManager и отправляется notification

        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(DemoSyncJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
    }
}
