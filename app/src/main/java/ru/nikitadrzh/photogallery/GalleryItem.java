package ru.nikitadrzh.photogallery;

import android.net.Uri;

/**
 * Created by Nekit on 17.01.2018.
 */

public class GalleryItem { //класс модели
//    private String mCaption;
//    private String mId;
//    private String mUrl;
    private String id;
    private String url_s;
    private String title;
    private String owner;

    @Override
    public String toString() {//override от object
        return title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl_s() {
        return url_s;
    }

    public void setUrl_s(String url_s) {
        this.url_s = url_s;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public Uri getPhotoPageUri() {//получем url страницы
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build();
    }

//    public String getmCaption() {
//        return mCaption;
//    }
//
//    public void setmCaption(String mCaption) {
//        this.mCaption = mCaption;
//    }
//
//    public String getmId() {
//        return mId;
//    }
//
//    public void setmId(String mId) {
//        this.mId = mId;
//    }
//
//
//    public String getmUrl() {
//        return mUrl;
//    }
//
//    public void setmUrl(String mUrl) {
//        this.mUrl = mUrl;
//    }
}
