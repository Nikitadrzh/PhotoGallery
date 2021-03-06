package ru.nikitadrzh.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nekit on 17.01.2018.
 */

public class FlickrFetchr {//Сетевой класс
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "59dedfc55b29ab8f34ad2ca08ff852bc";

    public byte[] getUrlBytes(String urlSpec) throws IOException {//данные по URL в виде байтов
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        ByteArrayOutputStream out = new ByteArrayOutputStream();//создаем поток ByteArray
        InputStream in = connection.getInputStream();//получаем входной поток из connection
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {//проверка исключения
            throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
        }

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {//read читает из потока и записывает в буфер
            out.write(buffer, 0, bytesRead);//записывает байты в массив выходного потока
        }
        out.close();
        return out.toByteArray(); //возвращается массив байтов только что созданный
    }

    public String getUrlString(String urlSpec) throws IOException {//преобразует массив байтов в
        // стринг
        return new String(getUrlBytes(urlSpec));//да, в аргумент стринга передается массив байтов:)
    }

    public List<GalleryItem> fetchItems(String page) {//построение URL запроса и получение результата
        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", page)
                    .build().toString();//изменяем строку с помощью Uri builder
            String jsonString = getUrlString(url);//это итоговая строка в формате JSON
            Log.i(TAG, "Receives JSON: " + jsonString);

            ////////////////////////////////////////////////////////////////////////////////////////
            Gson gson = new Gson();
            Photos galleryItem = gson.fromJson(jsonString, Photos.class);//так просто null
            items = galleryItem.getPhotos().getPhoto();

            Log.i(TAG, "galleryItem");//удалить
            ////////////////////////////////////////////////////////////////////////////////////////
            //JSONObject jsonBody = new JSONObject(jsonString);//Строится JSONObject
            //parseItems(items, jsonBody);//мотод парсит JSONObject и создает List<GalleryItem>
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } //catch (JSONException je) {
//            Log.e(TAG, "Failed to parse JSON", je);
//        }
        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException {//создаются экземпляры модели из JSONObject


//        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");//из jsonBody вытаскиваем
//        // JSONObject
//        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");//теперь уже из
//        // полученноо объекта получаем массив
//        for (int i = 0; i < photoJsonArray.length(); i++) {//зацикливаем создание объектов модели
//            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);//получаем объект из
//            // массива
//            GalleryItem item = new GalleryItem();//объект модели
//            item.setmId(photoJsonObject.getString("id"));//id
//            item.setmCaption(photoJsonObject.getString("title"));//title
//
//            if (!photoJsonObject.has("url_s")) {//если нет url в jsonObject
//                continue;//идет сразу на след. виток цикла
//            }
//
//            item.setmUrl(photoJsonObject.getString("url_s"));
//            items.add(item);//добавляет элемент модели в список items
//        }
    }
}
