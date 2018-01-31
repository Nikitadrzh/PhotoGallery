package ru.nikitadrzh.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Nekit on 31.01.2018.
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoPageActivity";

    private PhotoPageFragment currentPhotoPageFragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {//когда надо запустить
        // WebView, вызывается этот метод и создается интент, в который добавляется Uri страницы
        Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(photoPageUri);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        currentPhotoPageFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return currentPhotoPageFragment;
    }

    @Override
    public void onBackPressed() {//переопределение нажатия кнопки Back
        if (currentPhotoPageFragment.canWebViewGoBack()) { //если история не пуста, то переход назад
            currentPhotoPageFragment.webViewGoBack();
        } else { //если пуста, то как обычно
            super.onBackPressed();
        }
    }
}
