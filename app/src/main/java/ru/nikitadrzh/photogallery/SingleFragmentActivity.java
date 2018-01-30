package ru.nikitadrzh.photogallery;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.evernote.android.job.JobManager;

/**
 * Created by Nikita on 01.12.2017.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity { //абстрактная активити

    protected abstract Fragment createFragment(); //зачем? чтобы изменить не универсальнывй метод
    //в зависимости от задачи

    @LayoutRes //пометка для того, чтобы возвращать действительный ид ресурса
    protected int getLayoutResId() { //возвращает идентификатор макета, логика в том, что теперь
        // субклассы могут переопределить этот метод под свой макет
        return R.layout.activity_fragment; //"дефолтное" значение
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        StrictMode.enableDefaults();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_сontainer);

        if (fragment == null) { //добавляем фрагмент в контейнер, если его там не существует
            fragment = createFragment();//тут строится фрагмент
            fm.beginTransaction().add(R.id.fragment_сontainer, fragment).commit();
        }
    }
}
