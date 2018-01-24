package ru.nikitadrzh.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Nekit on 20.01.2018.
 */

public class ThumbnailDownloader<T> extends HandlerThread {//Фоновый поток с циклом сообщений
    // (загрузка миниатюры)
    private static final String TAG = "ThumbnailDownloader";//название потока
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;//ставит в очередь запросов на загрузку в потоке
    // ThumbnailDownloader, и обрабатывает сообщения при извлечении из очереди
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();//карта ключ:значение
    private Handler mResponseHandler;//а это уже Handler главного потока!
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {//создаем интерфейс слушателя для передачи
        // загруженных изображений
        void onThumbnailDownloaded(T target, Bitmap thumbnail);//метод, который нужно будет
        // реализовать, вызывается, когда загрузка произошла
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG); //в родительский класс уходит TAG - название потока
        mResponseHandler = responseHandler; //этой ссылке присваивается ссылка на Handler главного
        // потока
    }

    @Override
    public boolean quit() {//метод завершения потока
        mHasQuit = true;//заметка о выходе
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {//объект типа T - обобщ.параметр, вып. ф-ию
        // идентификатора загрузки, метод вызывается при биндинге, в этот метод передается сообщение
        //это как ящик сообщений????????????????????????????????????????????????????????????????????
        Log.i(TAG, "Got a URL: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);//создается связь между target и url, url нет в сообщении,
            // обновляется карта
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();//получение
            // нового сообщения и постановка его в очередь фонового потока, тут target - это obj -
            // объект для отправки получателю, sendToTarget ссылкается на mRequestHandler
        }
    }

    @Override
    protected void onLooperPrepared() {//настройка looper'а, вызывается еще до obtainMessage
        mRequestHandler = new Handler() {//суть анонимного класса - переопределяется handleMessage
            // класса Handler => новый класс без имени
            @Override
            public void handleMessage(Message msg) {//Subclasses must implement this to receive,
                // вызывается, когда сообщение извлечено из очереди и готово к обработке
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;//извлечение PhotoHolder из сообщения
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target); //обработка сообщения
                }
            }
        };
    }

    private void handleRequest(final T target) {//метод обработки Handler'ом
        try {
            final String url = mRequestMap.get(target);//получаем url из карты(ключ:значение)
            if (url == null) {//проверка отсутсвия url
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);//тут вы получаем набор битов,
            // но подставляем ДРУГОЙ url - url фотографии
            final Bitmap bitmap = BitmapFactory//тут получаем саму фотку, из байтов полученных
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
