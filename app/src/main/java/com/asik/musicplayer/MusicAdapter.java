package com.asik.musicplayer;


import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    ArrayList<AudioModel> allMusic;
    Context context;
//    MediaPlayer player;
    TextView sMusic;
    TextView sArtist;
    ImageView play;
    ImageView pause;
    ImageView next;
    MainActivity mainActivity;
    int posit;
    MusicAdapter(ArrayList<AudioModel> allMusic, MediaPlayer player, MainActivity mainActivity, TextView sMusic, TextView sArtist, int posit, ImageView play,ImageView pause){
        this.allMusic = allMusic;
        this.mainActivity = mainActivity;
        this.context = mainActivity.getApplicationContext();
//        this.player = player;
        this.sMusic= sMusic;
        this.sArtist = sArtist;
        this.posit=posit;
        this.pause = pause;
        this.play = play;

    }




    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ViewHolder holder, int position) {
        holder.setData(allMusic.get(position), position);
    }

    @Override
    public int getItemCount() {
        return allMusic.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView audioName;
        TextView artistName;
        LinearLayout playMusic;
//        AudioModel audioModel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            audioName = itemView.findViewById(R.id.musicName);
            artistName = itemView.findViewById(R.id.musicArtist);
            playMusic = itemView.findViewById(R.id.playMusic);

        }
        void setData(AudioModel audioModel, int pos){

            audioName.setText(audioModel.getAudioName());
            artistName.setText(audioModel.getArtistName());
            playMusic.setOnClickListener(v -> {
                if(mainActivity.player!= null && mainActivity.player.isPlaying()){
                    mainActivity.player.pause();
                }
                mainActivity.posit = pos;
                if (mainActivity.player!=null) mainActivity.player.reset();
                mainActivity.player = null;
                mainActivity.player = MediaPlayer.create(context, Uri.parse(audioModel.getAudioPath()));
                if (mainActivity.player!=null) mainActivity.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onComplete(pos+1);
                    }
                });
                if (mainActivity.seekBar!=null && mainActivity.player!=null) {
                    mainActivity.seekBar.setMax(mainActivity.player.getDuration());
                    mainActivity.seekBar.setProgress(0);
                }
                mainActivity.player.start();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);

                sMusic.setText(audioModel.getAudioName());
                sArtist.setText(audioModel.getArtistName());
//                mainActivity.player = player;


            });


        }
        void onComplete( int pos) {
            mainActivity.posit = pos;
            if (mainActivity.posit >= allMusic.size() - 1) {
//                mainActivity.player.reset();
                mainActivity.posit = 0;
                if (mainActivity.player!=null) mainActivity.player.reset();
                mainActivity.player = null;
            }
//            else {
                mainActivity.player = MediaPlayer.create(context, Uri.parse(allMusic.get(mainActivity.posit).getAudioPath()));
                mainActivity.player.setOnCompletionListener(mp -> {
                    onComplete(mainActivity.posit + 1);
                });
                if (mainActivity.seekBar!=null && mainActivity.player!=null) {
                    mainActivity.seekBar.setMax(mainActivity.player.getDuration());
                    mainActivity.seekBar.setProgress(0);
                }
                mainActivity.player.start();
                sMusic.setText(allMusic.get(mainActivity.posit).getAudioName());
                sArtist.setText(allMusic.get(mainActivity.posit).getArtistName());
//            }
//            mainActivity.player = player;

        }

    }
}
