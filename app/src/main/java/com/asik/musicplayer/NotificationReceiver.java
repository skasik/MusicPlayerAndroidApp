package com.asik.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case MusicApplication.ACTION_PLAY:
                    serviceIntent.putExtra("action", MusicApplication.ACTION_PLAY);
                    break;
                case MusicApplication.ACTION_NEXT:
                    serviceIntent.putExtra("action", MusicApplication.ACTION_NEXT);
                    break;
                case MusicApplication.ACTION_PREV:
                    serviceIntent.putExtra("action", MusicApplication.ACTION_PREV);
                    break;
            }
//            Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();
            context.startService(serviceIntent);
        }
    }
}
