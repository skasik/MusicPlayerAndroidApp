package com.asik.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {
    private IBinder iBinder = new MyBinder();
    ActionPlaying actionPlaying;
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        if (action != null && actionPlaying != null) {
            switch (action) {
                case MusicApplication.ACTION_PLAY:
                    actionPlaying.playClicked();
                    break;
                case MusicApplication.ACTION_NEXT:
                    actionPlaying.nextClicked();
                    break;
                case MusicApplication.ACTION_PREV:
                    actionPlaying.prevClicked();
                    break;
            }
        }

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}