package ru.nikitadrzh.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Nekit on 31.01.2018.
 */

public class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                PollService.PERM_PRIVATE, null);//здесь программно
        // регистрируется динамический широковещательный приемник с созданным фильтром
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);//удаляется приемник динамически также
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {//созается приемник
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Cancelling notification");//если до сюда дошли, то значит видим
            // оповещение, и оно отменяется
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
