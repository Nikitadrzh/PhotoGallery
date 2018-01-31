package ru.nikitadrzh.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Nekit on 26.01.2018.
 */

public class QueryPreferences {//класс для работы с хранимым запросом (сохраняется даже при
    // перезагрузке)
    private static final String PREF_SEARCH_QUERY = "searchQuery";//ключ для StoredQuery
    private static final String PREF_LAST_RESULT = "lastResultId";//ключ для LastResultId
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";//ключ ддя Alarm

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

    public static String getLastResultId(Context context) {//выдает id последней загруженной фото
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT, null);
    }

    public static void setLastResultId(Context context, String lastResultId) {//устанавливает id
        // последней загруженной фото
        PreferenceManager.getDefaultSharedPreferences(context)//возвращаются общие настройки
                .edit()//возвращает объект Editor
                .putString(PREF_LAST_RESULT, lastResultId)//записывается строка с ключом
                .apply();
    }

    public static boolean isAlarmOn(Context context) {//проверка состояния сигнала
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);//false - defaultValue
    }

    public static void setAlarmOn(Context context, boolean isOn) {//устанавливается состояние
        // сигнала
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}
