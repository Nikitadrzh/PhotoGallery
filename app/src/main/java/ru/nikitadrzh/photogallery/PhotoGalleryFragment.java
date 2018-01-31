package ru.nikitadrzh.photogallery;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by Nekit on 17.01.2018.
 */

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ProgressBar progressBar;
    private int page = 1;
    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;//<T> - тип объекта, который

    private final int JOB_ID = 1;
    // используется в качестве id для загрузки, его легко использовать для определения, куда
    // загружать картинку

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);//Включаем удержание фрагмента(далее используется)ИЗ-ЗА ЭТОГО БАГ В
        // ПОВОРОТАХ
        setHasOptionsMenu(true);//регистрация фрагмента для получения обратных вызовов меню
        updateItems();//запуск фонового потока
        Handler responseHandler = new Handler();//Handler главного потока
        thumbnailDownloader = new ThumbnailDownloader<>(responseHandler);//создание потока
        // (цикла сообщений), ему передается Handler главного потока
        thumbnailDownloader.setThumbnailDownloadListener(//для thumbnailDownloader устанавливается
                // слушатель, устанавливающий картинку в photoHolder после ее загрузки
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        thumbnailDownloader.start();//запуск фонового потока
        thumbnailDownloader.getLooper();//Looper управляет очередью сообщений потока
        Log.i(TAG, "Background thread started");

        JobScheduler scheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            scheduler = (JobScheduler) getContext()
                    .getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(
                    JOB_ID, new ComponentName(getContext(), JobPollService.class))
                    .setPeriodic(1000 * 60 * 15)
                    .setPersisted(true)
                    .build();
            if (scheduler != null) {
                scheduler.schedule(jobInfo);
            } else {
                Log.i(TAG, "scheduler = null");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mRecyclerView.getViewTreeObserver()//вешается слушатель, который срабатывает, когда view
                // отрисовывается
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        removeGlobalLayoutListener(this);//убираем листенер
                        setupLayoutManager();//настройка менеджера (под экран)
                    }
                });
        progressBar = v.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {//переопределяется метод
        // создания меню, в котором заполняется созданный XML меню с поиском
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);//находим searchItem из menu
        final SearchView searchView = (SearchView) searchItem.getActionView();//определяем
        // searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {//вызывается когда отправляется запрос
                Log.d(TAG, "QueryTextSubmit: " + query);
                hideKeyboard();//прячем клавиатуру
                hideSearchView(searchView);//сворачиваем searchView
                QueryPreferences.setStoredQuery(getActivity(), query);//записывается запрос в
                // хранилище общих настроек
                mItems.clear();
                mRecyclerView.removeAllViews();//очищается RecyclerView
                progressBar.setVisibility(View.VISIBLE);//запускаем progressBar
                updateItems();//метод который перезапускает поток FlickrFetchr
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {//вызывается при печати
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem alarmOnItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            alarmOnItem.setTitle(R.string.stop_polling);
        } else {
            alarmOnItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//метод, срабатывает, когда нажимаем на
        // item menu
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                hideKeyboard();//прячем клавиатуру
                QueryPreferences.setStoredQuery(getActivity(), null);//очищаем хранилище
                updateItems();//обновляем картинки
                mItems.clear();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);//запускает или
                // выключает AlarmManager
                getActivity().invalidateOptionsMenu();//обновляем список menu
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateItems() {//метод, перезапускащий поток FlickrFetchr
        String query = QueryPreferences.getStoredQuery(getActivity());//получает сохраненный запрос
        new FetchItemsTask(query).execute();
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {//фоновый поток
        private String mQuery;

        public FetchItemsTask(String query) {//конструктор, принимающий запрос
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {//что происходит на фоне
            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {

                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {//вызывается метод, который
            // уже выполняется в главном потоке, и работает после выполнения doInBackground
            mItems.addAll(galleryItems);
            progressBar.setVisibility(View.INVISIBLE);//выключаем progressBar
            if (mItems.size() == galleryItems.size()) {//адаптер только 1 раз устанавливается
                setupAdapter();
            } else {//а потом тупо обновляется, так как обновляется List
                mRecyclerView.getAdapter()//тут нужно менять реализацию, так как сейчас
                        // присоединяется к Recycler View постоянно 1ая страница
                        .notifyItemChanged(mItems.size() - galleryItems.size());
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImageView mImageView;
        private GalleryItem mGalleryItem;//сслылка на объект из массива List<GalleryItem>


        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
            Log.i(TAG, "allRight");
        }

        public void bindDrawable(Drawable drawable) { //новому элементу в холдере задается
            // какое-либо значение
            mImageView.setImageDrawable(drawable);//устанавливается изображение
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());//неявный
//            // интент для перехода по uri в браузере
            Intent intent = PhotoPageActivity.newIntent(getActivity(),
                    mGalleryItem.getPhotoPageUri());
            startActivity(intent);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItem; //его значение устанавливается из конструктора

        public PhotoAdapter(List<GalleryItem> galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity())
                    .inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItem.get(position);
            holder.bindGalleryItem(galleryItem);//привязываем элемент списка для клика по нему
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);//по-сути изменяется imageView от того элемента,
            // который сейчас в данном холдере, по его position
//            if (position == (getItemCount() - 1)) {//проверка, что доскроллили до конца Rec.View
//                uploadNewPage();
//            }

            if (position % 10 == 0) {//метод срабатывает только каждую 10ую позицию
                thumbnailDownloader.queueCacheThumbnail(createUrlArray(position));//создаем массив для
                // url и передаем в метод queueCacheThumbnail
            }

            thumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl_s());//передаем в
            // thumbnailDownloader PhotoHolder, чтоб знать, куда фотку грузить, и url, откуда ее
            // скачивать
        }

        private List<String> createUrlArray(int position) {
            List<String> urlArray = new ArrayList<>();//массив, в который добавляются url
            for (int pos = position; pos < position + 10; pos++) {
                urlArray.add(mGalleryItem.get(pos).getUrl_s());//добавляем в массив ОДИН url
            }
            return urlArray;
        }

        @Override
        public int getItemCount() {
            return mGalleryItem.size();
        }
    }

    private void removeGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//очищаем листенер, чтоб
            // больше не срабатывал
            mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
        } else {
            mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
        }
    }

    private void hideKeyboard() {//метод прячет клавиатуру
        InputMethodManager manager = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void hideSearchView(SearchView searchView) {
        searchView.setQuery("", false);
        searchView.setIconified(true);
    }

    private void setupLayoutManager() {
        final int widthOfColumn = 130; //ширина одного столбца в dp
        double widthInDp = mRecyclerView.getWidth() /
                getContext().getResources().getDisplayMetrics().density;//получаем ширину экрана в
        // dp а не в px
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                (int) Math.floor(widthInDp / widthOfColumn)));
    }

//    private void uploadNewPage() {//метод загружает следующую страницу и присоединяет ее к Rec.View
//        setPage(getPage() + 1);//увеличение страницы
//        new FetchItemsTask().execute();//запуск фонового потока, загружающего новую стран.
//    }

    private void setupAdapter() {
        if (isAdded()) {//проверка, что фрагмент присоединен к активности
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private void setPage(int currentPage) {
        page = currentPage;
    }

    private int getPage() {
        return page;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();//завершение потока
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();//очищаем сообщения из очереди сообщений
    }
}
