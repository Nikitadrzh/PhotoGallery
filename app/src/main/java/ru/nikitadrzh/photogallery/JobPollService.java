package ru.nikitadrzh.photogallery;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Created by Nekit on 30.01.2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobPollService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {
        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            List<GalleryItem> items;//список, распарсенный из полученного JSON
            items = new FlickrFetchr().fetchRecentPhotos(); //получаем последние фото
            return null;
        }
    }
}
