package ru.nikitadrzh.photogallery;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by Nekit on 17.01.2018.
 */

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int page = 1;
    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;//<T> - тип объекта, который
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
        new FetchItemsTask().execute();//запуск фонового потока

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
        return v;
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {//фоновый поток

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {//что происходит на фоне
            return new FlickrFetchr().fetchItems(String.valueOf(page)); //в fetchItems передается
            // страница
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {//вызывается метод, который
            // уже выполняется в главном потоке, и работает после выполнения doInBackground
            mItems.addAll(galleryItems);
            if (mItems.size() == galleryItems.size()) {//адаптер только 1 раз устанавливается
                setupAdapter();
            } else {//а потом тупо обновляется, так как обновляется List
                mRecyclerView.getAdapter()
                        .notifyItemChanged(mItems.size() - galleryItems.size());
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_image_view);
            Log.i(TAG, "allRight");
        }

        public void bindDrawable(Drawable drawable) { //новому элементу в холдере задается
            // какое-либо значение
            mImageView.setImageDrawable(drawable);//устанавливается изображение
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
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);

            holder.bindDrawable(placeholder);//по-сути изменяется imageView от того элемента,
            // который сейчас в данном холдере, по его position
            if (position == (getItemCount() - 1)) {//проверка, что доскроллили до конца Rec.View
                uploadNewPage();
            }
            thumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl_s());//передаем в
            // thumbnailDownloader PhotoHolder, чтоб знать, куда фотку грузить, и url, откуда ее
            // скачивать
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

    private void setupLayoutManager() {
        final int widthOfColumn = 130; //ширина одного столбца в dp
        double widthInDp = mRecyclerView.getWidth() /
                getContext().getResources().getDisplayMetrics().density;//получаем ширину экрана в
        // dp а не в px
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                (int) Math.floor(widthInDp / widthOfColumn)));
    }

    private void uploadNewPage() {//метод загружает следующую страницу и присоединяет ее к Rec.View
        setPage(getPage() + 1);//увеличение страницы
        new FetchItemsTask().execute();//запуск фонового потока, загружающего новую стран.
    }

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
