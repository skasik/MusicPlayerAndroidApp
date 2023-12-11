package com.asik.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    ArrayList<SongModel> musics;
    HomepageActivity homepageActivity;


    SongAdapter(ArrayList<SongModel> musics, HomepageActivity homepageActivity) {
        this.musics = musics;
        this.homepageActivity = homepageActivity;

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
//            Glide.with(homepageActivity).load(songModel.getImage())
//                    .into(songImage);

            playMusic.setOnClickListener(v -> {
                if (songModel.getDownloadUrl().equals("")) fetchSongDownloadURL(songModel, position);
                else startPlayingSong(songModel, position);

                Log.d("debugTest", songModel.getThumbnail());

            });
        }

        void startPlayingSong(SongModel songModel, int pos) {
            try {
                if (homepageActivity.player != null){
                    homepageActivity.player.stop();
                    homepageActivity.player.reset();
                    homepageActivity.player = null;
                }
                homepageActivity.player = new MediaPlayer();
                homepageActivity.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                homepageActivity.player.setDataSource(homepageActivity, Uri.parse(songModel.getDownloadUrl()));
                homepageActivity.player.prepare();
                homepageActivity.player.setOnPreparedListener(mp -> {
                    mp.start();
                });

                homepageActivity.currentlyPlaying = musics;
                homepageActivity.curPos = pos;
                homepageActivity.updateCurrentPlaying(songModel);

            } catch (Exception e) {
                Toast.makeText(homepageActivity, "Unable to play this song", Toast.LENGTH_SHORT).show();
            }
        }


        void fetchSongDownloadURL(SongModel song, int pos) {
            String API_LINK = "https://saavn.me/songs?id=" + song.getId();
            Toast.makeText(homepageActivity, "Loading song...", Toast.LENGTH_SHORT).show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(API_LINK, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        JSONObject data = response.getJSONArray("data").getJSONObject(0);
                        JSONArray downloadUrls = data.getJSONArray("downloadUrl");
                        JSONArray image = data.getJSONArray("image");
                        song.setDownloadUrl(downloadUrls.getJSONObject(downloadUrls.length()-1).getString("link"));
                        song.setThumbnail(image.getJSONObject(0).getString("link"));
                        startPlayingSong(song, pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(homepageActivity, "failed to load the song", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(homepageActivity, "failed to load the song", Toast.LENGTH_SHORT).show();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(homepageActivity);
            requestQueue.add(jsonObjectRequest);
        }
    }
}
