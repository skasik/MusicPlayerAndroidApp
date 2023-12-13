package com.asik.musicplayer;

import android.net.Uri;
import android.os.Handler;
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
import java.util.concurrent.Callable;

import kotlin.Unit;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    ArrayList<SongModel> musics;
    HomepageActivity homepageActivity;

    Callable<Void> onClick;
    Boolean aBoolean;


    SongAdapter(ArrayList<SongModel> musics, HomepageActivity homepageActivity, Boolean aBoolean) {
        this.musics = musics;
        this.aBoolean = aBoolean;
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
        this.aBoolean = false;
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
                        if (aBoolean) {
                            loadRecommendedSongs(songModel, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    if (songModel.getDownloadUrl().equals(""))
                                        homepageActivity.fetchSongDownloadURL(homepageActivity.currentlyPlaying.get(homepageActivity.curPos), homepageActivity.curPos);
                                    else {
                                        homepageActivity.startPlayingSong(homepageActivity.currentlyPlaying.get(homepageActivity.curPos), homepageActivity.curPos);
                                    }
                                    return null;
                                }
                            });


                        }
                        else {
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
                        }
                        });
                    checkIfPlaying(songModel);
        }

        void checkIfPlaying(SongModel songModel) {
            if (homepageActivity.currentlyPlaying.size() > 0) {
                SongModel current = homepageActivity.currentlyPlaying.get(homepageActivity.curPos);
                if (current.getId().equals(songModel.getId()) && !current.getId().equals("")) {
                    playMusic.setBackgroundColor(homepageActivity.getResources().getColor(R.color.play_bar));
                } else {
                    playMusic.setBackgroundColor(homepageActivity.getResources().getColor(R.color.white));
                }
            }

            new Handler().postDelayed(() -> {
                checkIfPlaying(songModel);
            }, 100);
        }

        private void loadRecommendedSongs(SongModel songModel, Callable<Void> afterResponse) {
            ArrayList<SongModel> recommendedSongs = new ArrayList<>();
            recommendedSongs.add(songModel);
            String recommendedSongsAPI = "https://saavn.me/artists/";
            String ss = songModel.getId().trim();
            ArrayList<String> artistID = songModel.getArtistId();
            artistID.forEach(v1 -> {
                String recAPI = recommendedSongsAPI + v1.trim() + "/recommendations/" +ss;

                Log.d("debugTest",recAPI);Log.d("debugTest",v1);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(recAPI, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("data");
                            for (int i = 0; i < results.length(); i++) {
                                SongModel recommended = new SongModel();
                                recommended.setName(results.getJSONObject(i).getString("name"));
                                recommended.setId(results.getJSONObject(i).getString("id"));
                                recommended.setDuration(results.getJSONObject(i).getString("duration"));
                                recommended.setLanguage(results.getJSONObject(i).getString("language"));
                                recommended.setUrl(results.getJSONObject(i).getString("url"));
                                recommended.setFeaturedArtists(results.getJSONObject(i).getString("primaryArtists"));
                                recommended.setArtistId(results.getJSONObject(i).getString("primaryArtistsId"));
                                JSONArray image = results.getJSONObject(i).getJSONArray("image");
                                recommended.setImage(image.getJSONObject(image.length() - 1).getString("link"));
                                recommendedSongs.add(recommended);
                            }
                            homepageActivity.curPos=0;
                            homepageActivity.currentlyPlaying = recommendedSongs;
                            if (afterResponse != null) {
                                try {
                                    afterResponse.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {

                            Toast.makeText(homepageActivity, "failed to load the song", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(homepageActivity, "failed to load the song", Toast.LENGTH_SHORT).show();
                    }
                });

                RequestQueue requestQueue = Volley.newRequestQueue(homepageActivity);
                requestQueue.add(jsonObjectRequest);

            });
        }


    }
}
