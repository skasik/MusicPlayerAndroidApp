package com.asik.musicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;
    int posit;
    TextView selectedAudio = null;
    TextView selectedArtist = null;
    SeekBar seekBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedAudio = findViewById(R.id.selectedMusic);
        selectedArtist = findViewById(R.id.selectedArtist);
        seekBar = findViewById(R.id.seekbar);

        updateSeekbar();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player!=null){
                    player.seekTo(seekBar.getProgress());
                }
            }
        });


//        if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
//            showAllAudio();
//        else{
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_MEDIA_AUDIO
//            },10);
//        }

        if (android.os.Build.VERSION.SDK_INT >= 33){
            if (!checkPermission(Manifest.permission.READ_MEDIA_AUDIO)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        10);
            }
            else showAllAudio();
        }
        else {
            if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        10);
            }
            else showAllAudio();
        }
        if (player!=null && player.isPlaying()){

        }



    }

    void updateSeekbar() {
        new Handler().postDelayed(() -> {
            if (player == null) seekBar.setProgress(0);
            else if (player!= null && player.isPlaying()) {
                seekBar.setProgress(seekBar.getProgress()+10);
                seekBar.setProgress(player.getCurrentPosition());
            }

            updateSeekbar();
        }, 10);
    }

    public void showAllAudio(){
        ArrayList<AudioModel> audios= getAllAudioFromDevice(MainActivity.this);
        RecyclerView music = findViewById(R.id.musicList);

        ImageView play = findViewById(R.id.playButton);
        ImageView pause = findViewById(R.id.pauseButton);
        ImageView next = findViewById(R.id.nextButton);

        LinearLayout playMusic = findViewById(R.id.playMusic);

        MusicAdapter adapter = new MusicAdapter(audios,player,this,selectedAudio,selectedArtist,posit,play,pause);
        music.setAdapter(adapter);
        music.setHasFixedSize(true);
        music.setLayoutManager(new LinearLayoutManager(this));

        play.setOnClickListener(v1 ->{
            if (player == null) {
                posit=0;
                player = MediaPlayer.create(this, Uri.parse(audios.get(posit).getAudioPath()));
                selectedAudio.setText(audios.get(posit).getAudioName());
                selectedArtist.setText(audios.get(posit).getArtistName());
            }

            if (seekBar!=null && player!=null) {
                seekBar.setMax(player.getDuration());
                seekBar.setProgress(0);
            }
            player.start();
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onComplete(audios, posit+1);
                }
            });

        } );

        pause.setOnClickListener(v1 -> {
            if (player!= null && player.isPlaying()){
                player.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });
        next.setOnClickListener(v -> {

            posit+=1;
            if (posit >= audios.size()-1) posit=0;
            player.pause();
            player = MediaPlayer.create(this, Uri.parse(audios.get(posit).getAudioPath()));
            if (seekBar!=null && player!=null) {
                seekBar.setMax(player.getDuration());
                seekBar.setProgress(0);
            }
            player.start();
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            selectedAudio.setText(audios.get(posit).getAudioName());
            selectedArtist.setText(audios.get(posit).getArtistName());
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onComplete(audios, posit+1);
                }
            });

        });


    }
    void onComplete(ArrayList<AudioModel> audios, int pos){
        posit = pos;
        if (pos >= audios.size()-1) player.reset();
        else{
            player = MediaPlayer.create(this, Uri.parse(audios.get(pos).getAudioPath()));
            player.setOnCompletionListener(mp -> {
                onComplete(audios,pos+1);
            });
            if (seekBar!=null && player!=null) {
                seekBar.setMax(player.getDuration());
                seekBar.setProgress(0);
            }
            player.start();
            selectedAudio.setText(audios.get(pos).getAudioName());
            selectedArtist.setText(audios.get(pos).getArtistName());
        }
//            play.setVisibility(View.GONE);
//            pause.setVisibility(View.VISIBLE);

    }



    public ArrayList<AudioModel> getAllAudioFromDevice(Context context){
        ArrayList<AudioModel> audios = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE
        };
        Cursor c = context.getContentResolver().query(uri,projection,null,null,MediaStore.Audio.Media.DATE_ADDED+" DESC");
        if(c!=null){
            while(c.moveToNext()){
                AudioModel model = new AudioModel();
                model.setAudioName(c.getString(3));
                model.setAlbum(c.getString(2));
                model.setArtistName(c.getString(1));
                model.setAudioPath(c.getString(0));
                audios.add(model);
            }
            c.close();
        }
        return audios;
    }
    boolean checkPermission(String permission){
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("debugTest", requestCode+">>"+resultCode);
        if (requestCode==10 && resultCode==RESULT_OK){
            Log.d("debugTest2", requestCode+">>"+resultCode);
            showAllAudio();
        }

    }

}