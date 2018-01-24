package ru.nikitadrzh.photogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        String pullRequestTest = "deleteLater";
        String pullRequestTest2 = "deleteLater";
        return PhotoGalleryFragment.newInstance();
    }
    //1-наслед. от SingleFragActiv, создаем контейнер(activ.fragment.xml)//
    //2-доб. в зависимости Rec.View//
    //3-переимен. activ.photo.gal.xml в fragment.photo.gal.xml//
    //4-добавляем в него Rec.view//
    //5-создаем PhotoGalleryFragment//

    //сеть:
    //1 создаем класс FlicrFetchr и 2 метода getUrl//
    //2 разрешение на работу с сетью//
    //3 создаем фоновый поток FetchItemsTask//
    //4 получаем APIkey//
    //5 строим метод fetchItems для формирования url запроса//
    //6 вызываем fetchItems в фоновом потоке//
    //на этом этапе JSON получен

    //Парсим:
    //создаем модель в которую грузим данные из json//
    //разбираем JSON через JSONObject//
    //пишем метод parseItems//
    //создаем объекты модели и заполняем ими list//

    //Из модели заполняем Rec.view//
    //вызываем конструктор адаптера из кода//
    //используя onPostExecute обновляем список и вызываем setupAdapter//

    //1 задание//
    //используем GSon: 1 dependencies//
    //создаем шаблонный объект и в него парсим//

    //2 задание//
    //вешаем листенер на rec.view//
    //ждем, пока position будет == count//
    //в методе листенера перезапускаем фоновый поток//

    //вывод изображения://
    //1 - в holder заменим textview на imageview(gallery_item.xml)//
    //2 - передаем в констуктор ViewHolder иерархию представлений, вместо textView//
    //3 - добавляем временное изображение - Биллов, изменяем через onBindViewHolder//
    //используем множественную загрузку - фоновый поток HandlerThread, использ. цикл сообщений//
    //1 - создаем ThumbnailDownloader, создаем его "почту"//
    //2 - создаем экземпляр thumbnailDownloader в фрагменте и запускаем поток//
    //3 - в bindviewholder вызываем queueThumbnail//
    //4 - создаем mRequestHandler и в queueThumbnail заносим message в "почту", сообщение там ждет и
    // потом опять обрабатывается mRequestHandler//
    //5 - обрабатываем handler'ом сообщения, выкинутые из ящика looper'ом - это все в своем потоке//
    //6 - теперь кидаем сообщения в главный поток//

    //остановились на реализации интерфейса//


}
