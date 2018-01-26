package ru.nikitadrzh.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Nekit on 26.01.2018.
 */

public class QueryPreferences {//класс для работы с хранимым запросом (сохраняется даже при
    // перезагрузке)
    private static final String PREF_SEARCH_QUERY = "searchQuery";//ключ для getString

    public static String getStoredQuery(Context context) {//возвращает сохраненный запрос
        return PreferenceManager.getDefaultSharedPreferences(context)//механизм общих настроек
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query) {//записывает запрос
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()//возвращает объект Editor
                .putString(PREF_SEARCH_QUERY, query)//записывается строка с ключом
                .apply();
    }
}
