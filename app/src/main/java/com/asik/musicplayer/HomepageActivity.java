package com.asik.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomepageActivity extends AppCompatActivity {

    MediaPlayer player;
    ArrayList<SongModel> currentlyPlaying = new ArrayList<>();
    int curPos = 0;
    ImageView play;
    ImageView pause;
    SeekBar seekBar;
    Boolean isPlayerScreenShown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        View playing = findViewById(R.id.playing);
        CardView playingImage = findViewById(R.id.card);
        seekBar = findViewById(R.id.seekbar);

        playingImage.setOnClickListener(v -> {
            openPlayerScreen();
        });

        playing.setOnClickListener(v -> {
            openPlayerScreen();
        });
        play = findViewById(R.id.playButton);
        pause = findViewById(R.id.pauseButton);

        play.setOnClickListener(v -> {
            if (player != null) {
                player.start();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }


        });
        pause.setOnClickListener(v -> {
            if (player != null && player.isPlaying()) {
                player.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }


        });


        loadHomePage("hindi,english,bengali");
//        loadHomePage("hindi");
    }

    private void loadHomePage(String language) {
        ArrayList<SongModel> songs = new ArrayList<>();
        ArrayList<AlbumModel> albums = new ArrayList<>();

        String API_LINK = "https://saavn.me/modules?language=" + language;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(API_LINK, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject albumData = response.getJSONObject("data");
                    JSONArray album = albumData.getJSONArray("albums");

                    for (int i = 0; i < album.length(); ++i) {
                        String type = album.getJSONObject(i).getString("type");

                        AlbumModel albumModel = new AlbumModel();
                        albumModel.setId(album.getJSONObject(i).getString("id"));
                        albumModel.setName(album.getJSONObject(i).getString("name"));
                        albumModel.setUrl(album.getJSONObject(i).getString("url"));
                        JSONArray artists = album.getJSONObject(i).getJSONArray("artists");
                        String featuredArtists = "";
                        for (int j = 0; j < artists.length(); ++j) {
                            if (!featuredArtists.equals(""))
                                featuredArtists += ", ";
                            featuredArtists += artists.getJSONObject(j).getString("name");
                        }
                        albumModel.setPlayCount(album.getJSONObject(i).getString("playCount"));
                        albumModel.setFeaturedArtists(featuredArtists);
                        JSONArray image = album.getJSONObject(i).getJSONArray("image");
                        albumModel.setImage(image.getJSONObject(image.length() - 1).getString("link"));
                        albumModel.setThumbnail(image.getJSONObject(0).getString("link"));
//                        albumModel.setSongs();


                        if (type.equals("song")) {
                            songs.add(AlbumToSong(albumModel));
                        } else albums.add(albumModel);
                    }
                    RecyclerView albumAD = findViewById(R.id.albumRV);
                    albumAD.setAdapter(new AlbumAdapter(albums, HomepageActivity.this));
                    albumAD.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    albumAD.setHasFixedSize(true);

                    JSONArray song = albumData.getJSONObject("trending").getJSONArray("songs");
                    for (int j = 0; j < song.length(); j++) {
                        SongModel songModel = new SongModel();
                        songModel.setName(song.getJSONObject(j).getString("name"));
                        songModel.setId(song.getJSONObject(j).getString("id"));
                        songModel.setDuration(song.getJSONObject(j).getString("duration"));
                        JSONArray artists = song.getJSONObject(j).getJSONArray("primaryArtists");
                        String featuredArtists = "";
                        for (int k = 0; k < artists.length(); ++k) {
                            if (!featuredArtists.equals(""))
                                featuredArtists += ", ";
                            featuredArtists += artists.getJSONObject(k).getString("name");
                        }

                        songModel.setFeaturedArtists(featuredArtists);
//                        songModel.setFeaturedArtists(song.getJSONObject(j).getString("featuredArtists"));
                        songModel.setUrl(song.getJSONObject(j).getString("url"));
                        JSONArray image = song.getJSONObject(j).getJSONArray("image");
                        songModel.setImage(image.getJSONObject(image.length() - 1).getString("link"));
                        songModel.setThumbnail(image.getJSONObject(0).getString("link"));


                        songs.add(songModel);


                    }

                    RecyclerView songAD = findViewById(R.id.songsRV);
                    songAD.setAdapter(new SongAdapter(songs, HomepageActivity.this));
                    LinearLayoutManager llm = new LinearLayoutManager(HomepageActivity.this, LinearLayoutManager.VERTICAL, false);
                    llm.setAutoMeasureEnabled(true);
                    songAD.setLayoutManager(llm);
//                    songAD.setHasFixedSize(true);
                    songAD.setNestedScrollingEnabled(false);

//                    Log.d("debugTestLength", songs.size()+">>"+songs.get(songs.size()-1).getName());


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HomepageActivity.this, "Not Found", Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public static SongModel AlbumToSong(AlbumModel albumModel) {
        SongModel songModel = new SongModel();
        songModel.setId(albumModel.getId());
        songModel.setName(albumModel.getName());
        songModel.setUrl(albumModel.getUrl());
        songModel.setImage(albumModel.getImage());
        songModel.setFeaturedArtists(albumModel.getFeaturedArtists());
        songModel.setDuration(albumModel.getPlayCount());
        songModel.setThumbnail(albumModel.getThumbnail());

        return songModel;
    }


    public void openPlayerScreen() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View contentView = LayoutInflater.from(this).inflate(R.layout.playing_music, null);
        dialog.setContentView(contentView);
//        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        View bottomSheet = (View) contentView.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Set the initial state to expanded

        dialog.setOnDismissListener(dialog1 -> {
            isPlayerScreenShown = false;
        });

        ImageView playPlaying = dialog.findViewById(R.id.playButton);
        ImageView pausePlaying = dialog.findViewById(R.id.pauseButton);
        ImageView nextPlaying = dialog.findViewById(R.id.nextButton);
        ImageView previousPlaying = dialog.findViewById(R.id.previousBTN);
        SeekBar seekbarPlaying = dialog.findViewById(R.id.seekPlaying);

        playPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
                playPlaying.setVisibility(View.GONE);
                pausePlaying.setVisibility(View.VISIBLE);
            }
        });
        pausePlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
                playPlaying.setVisibility(View.VISIBLE);
                pausePlaying.setVisibility(View.GONE);
            }
        });
        nextPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });
        previousPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPos -= 1;
                if (curPos <= 0) curPos = 0;
                SongModel songModel = currentlyPlaying.get(curPos);
                if (songModel.getDownloadUrl().equals("")) fetchSongDownloadURL(songModel, curPos);
                else {
                    startPlayingSong(songModel, curPos);
                }
            }
        });
        seekbarPlaying.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) player.seekTo(seekBar.getProgress());
            }
        });


//        behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottomsheet_height));

        TextView songName = dialog.findViewById(R.id.songName);
        TextView artistName = dialog.findViewById(R.id.artistName);

        songName.setSelected(true);
        artistName.setSelected(true);

        isPlayerScreenShown = true;
        updatePlayerScreen(dialog);
        dialog.show();
    }

    public void playNextSong() {
        curPos += 1;
        if (curPos >= currentlyPlaying.size()) curPos = 0;
        SongModel songModel = currentlyPlaying.get(curPos);
        if (songModel.getDownloadUrl().equals("")) fetchSongDownloadURL(songModel, curPos);
        else {
            startPlayingSong(songModel, curPos);
        }
    }

    public void updateCurrentPlaying(SongModel songModel) {
        TextView songName = findViewById(R.id.selectedMusic);
        TextView artistName = findViewById(R.id.selectedArtist);
        ImageView songImage = findViewById(R.id.selectedImage);
        View parent = findViewById(R.id.bottomParent);
        parent.setVisibility(View.VISIBLE);

        songName.setSelected(true);
        artistName.setSelected(true);

//        Glide.with(this).load(songModel.getImage()).into(songImage);
//        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(this).load(songModel.getImage())
                .thumbnail(Glide.with(this).load(songModel.getThumbnail()))
                .into(songImage);
        songName.setText(songModel.getName());
        artistName.setText(songModel.getFeaturedArtists());

        play.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
        updateSeekbar();
        seekBar.setMax(player.getDuration());
    }

    void updateSeekbar() {
        new Handler().postDelayed(() -> {
            if (player == null) seekBar.setProgress(0);
            else if (player != null && player.isPlaying()) {
//                seekBar.setProgress(seekBar.getProgress()+10);
                seekBar.setProgress(player.getCurrentPosition());
            }

            if (player != null && player.isPlaying()) {
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
            if (player != null && !player.isPlaying()) {
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }

            updateSeekbar();
        }, 10);
    }

    void updatePlayerScreen(BottomSheetDialog dialog) {

        if (!isPlayerScreenShown) return;

        TextView songName = dialog.findViewById(R.id.songName);
        TextView artistName = dialog.findViewById(R.id.artistName);
        ImageView songImage = dialog.findViewById(R.id.songImage);
        TextView endTime = dialog.findViewById(R.id.endTime);
        TextView currentTime = dialog.findViewById(R.id.currentTime);
        SeekBar seekPlaying = dialog.findViewById(R.id.seekPlaying);


        ImageView playPlaying = dialog.findViewById(R.id.playButton);
        ImageView pausePlaying = dialog.findViewById(R.id.pauseButton);

        if (player != null && player.isPlaying()) {
            playPlaying.setVisibility(View.GONE);
            pausePlaying.setVisibility(View.VISIBLE);
        }
        if (player != null && !player.isPlaying()) {
            pausePlaying.setVisibility(View.GONE);
            playPlaying.setVisibility(View.VISIBLE);
        }


        new Handler().postDelayed(() -> {

            SongModel currentPlayingSong = currentlyPlaying.get(curPos);

            songName.setText(currentPlayingSong.getName());

            artistName.setText(currentPlayingSong.getFeaturedArtists());
            endTime.setText(SongModel.convertDuration(Integer.valueOf(player.getDuration() / 1000)));
            Glide.with(this).load(currentPlayingSong.getImage()).thumbnail(Glide.with(this).load(currentPlayingSong.getThumbnail())).into(songImage);

            seekPlaying.setMax(player.getDuration());
//            seekPlaying.setProgress(player.getCurrentPosition());
            currentTime.setText(SongModel.convertDuration(Integer.valueOf(seekPlaying.getProgress() / 1000)));

            if (player == null) seekPlaying.setProgress(0);
            else if (player != null && player.isPlaying()) {
//                seekPlaying.setProgress(seekBar.getProgress()+10);
                seekPlaying.setProgress(player.getCurrentPosition());
            }


            updatePlayerScreen(dialog);
        }, 10);
    }

    void startPlayingSong(SongModel songModel, int pos) {
        try {
            if (player != null) {
                player.stop();
                player.reset();
                player = null;
            }
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(songModel.getDownloadUrl()));
            player.prepare();
            player.setOnPreparedListener(mp -> {
                mp.start();
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNextSong();
                }
            });


            curPos = pos;
            updateCurrentPlaying(songModel);

        } catch (Exception e) {
            Toast.makeText(this, "Unable to play this song", Toast.LENGTH_SHORT).show();
        }
    }

    void fetchSongDownloadURL(SongModel song, int pos) {
        String API_LINK = "https://saavn.me/songs?id=" + song.getId();
        Toast.makeText(this, "Loading song...", Toast.LENGTH_SHORT).show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(API_LINK, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject data = response.getJSONArray("data").getJSONObject(0);
                    JSONArray downloadUrls = data.getJSONArray("downloadUrl");
                    JSONArray image = data.getJSONArray("image");
                    song.setDownloadUrl(downloadUrls.getJSONObject(downloadUrls.length() - 1).getString("link"));
                    song.setThumbnail(image.getJSONObject(0).getString("link"));
//                    currentlyPlaying = musics;
                    startPlayingSong(song, pos);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HomepageActivity.this, "failed to load the song", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(HomepageActivity.this, "failed to load the song", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}