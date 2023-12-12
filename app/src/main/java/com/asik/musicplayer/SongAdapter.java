package com.asik.musicplayer;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import kotlin.Unit;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    ArrayList<SongModel> musics;
    HomepageActivity homepageActivity;

    Callable<Void> onClick;


    SongAdapter(ArrayList<SongModel> musics, HomepageActivity homepageActivity) {
        this.musics = musics;
        this.homepageActivity = homepageActivity;
        this.onClick = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        };

    }

    SongAdapter(ArrayList<SongModel> musics, HomepageActivity homepageActivity, Callable<Void> onClick) {
        this.musics = musics;
        this.homepageActivity = homepageActivity;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        holder.setSongs(musics.get(position), position);

    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView songImage;
        TextView songName;
        TextView artistName;
        TextView duration;
        View playMusic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songName);
            artistName = itemView.findViewById(R.id.artistName);
            songImage = itemView.findViewById(R.id.songImage);
            duration = itemView.findViewById(R.id.duration);
            playMusic = itemView.findViewById(R.id.playMusic);


        }

        public void setSongs(SongModel songModel, int position) {
            songName.setText(songModel.getName());
            artistName.setText(songModel.getFeaturedArtists());
            duration.setText(songModel.getDuration());
            Glide.with(homepageActivity).load(songModel.getImage())
                    .thumbnail(Glide.with(homepageActivity).load(songModel.getThumbnail()))
                    .into(songImage);

            playMusic.setOnClickListener(v -> {
                homepageActivity.currentlyPlaying = musics;
                homepageActivity.curPos = position;
                if (songModel.getDownloadUrl().equals(""))
                    homepageActivity.fetchSongDownloadURL(songModel, position);
                else {
                    homepageActivity.startPlayingSong(songModel, position);
                }

                try {
                    onClick.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            checkIfPlaying(songModel);
        }

        void checkIfPlaying(SongModel songModel) {
            if (homepageActivity.currentlyPlaying.size() > 0) {
                SongModel current = homepageActivity.currentlyPlaying.get(homepageActivity.curPos);
                if (current.getId().equals(songModel.getId()) && !current.getId().equals("")) {
                    playMusic.setBackgroundColor(homepageActivity.getResources().getColor(R.color.play_bar));
                } else playMusic.setBackgroundColor(homepageActivity.getResources().getColor(R.color.white));
            }

            new Handler().postDelayed(() -> {
                checkIfPlaying(songModel);
            }, 100);
        }


    }
}
