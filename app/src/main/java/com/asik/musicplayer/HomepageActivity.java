package com.asik.musicplayer;

import static com.asik.musicplayer.MusicApplication.ACTION_NEXT;
import static com.asik.musicplayer.MusicApplication.ACTION_PLAY;
import static com.asik.musicplayer.MusicApplication.ACTION_PREV;
import static com.asik.musicplayer.MusicApplication.CHANNEL2_ID;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class HomepageActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    MediaPlayer player;
    ArrayList<SongModel> currentlyPlaying = new ArrayList<>();
    int curPos = 0;
    ImageView play;
    ImageView pause;
    SeekBar seekBar;
    Boolean isPlayerScreenShown = false;
    ArrayList<LanguageModel> languageModels = new ArrayList<>();
    private OnBackPressedDispatcher onBackPressedDispatcher;

    private ArrayList<LanguageModel> selectedLanguages = new ArrayList<>();
    String languageParam = "";
    SharedPreferences sp;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (((LinearLayout) findViewById(R.id.homeScreen)).getVisibility() == View.VISIBLE) {
                    NestedScrollView nestedScrollView = findViewById(R.id.scrollbar);
                    if (nestedScrollView.canScrollVertically(-1)) {
                        nestedScrollView.smoothScrollTo(0, 0);
                    } else {
//                    finish();
                        //move activity to back
                        moveTaskToBack(true);

                    }
                } else {
                    ((LinearLayout) findViewById(R.id.searchLayout)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.homeScreen)).setVisibility(View.VISIBLE);
                }

            }
        });
        ImageView search = findViewById(R.id.searchButton);
        search.setOnClickListener(v -> {
            ((LinearLayout) findViewById(R.id.searchLayout)).setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.homeScreen)).setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                ((EditText) findViewById(R.id.searchElementID)).requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(((EditText) findViewById(R.id.searchElementID)), InputMethodManager.SHOW_IMPLICIT);
            }, 500);
        });
        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {

            ((LinearLayout) findViewById(R.id.searchLayout)).setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.homeScreen)).setVisibility(View.VISIBLE);
        });

        EditText searched = findViewById(R.id.searchElementID);
        String searchAPI = "https://saavn.me/search/songs?limit=100&query=";
        searched.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String searchQuery = searched.getText().toString().trim().toLowerCase();
                searchSongs(searchAPI + Uri.encode(searchQuery));
                hideKeyboard(HomepageActivity.this);
                return true;
            }
        });


        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        mediaSessionCompat = new MediaSessionCompat(this, "My Music");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }

        sp = getSharedPreferences("music_prefs", MODE_PRIVATE);
        ed = sp.edit();

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
                showNotification(R.drawable.pause);
            }


        });
        pause.setOnClickListener(v -> {
            if (player != null && player.isPlaying()) {
                player.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                showNotification(R.drawable.play);
            }


        });

        languageModels.add(new LanguageModel("For You", "hindi,english,bengali", R.id.allSong));
        languageModels.add(new LanguageModel("Hindi", R.id.hindiSong));
        languageModels.add(new LanguageModel("Bengali", R.id.bengaliSong));
        languageModels.add(new LanguageModel("English", R.id.englishSong));
        languageModels.add(new LanguageModel("Punjabi", R.id.punjabiSong));
        languageModels.add(new LanguageModel("Bhojpuri", R.id.bhojpuriSong));
        languageModels.add(new LanguageModel("Urdu", R.id.urduSong));
        languageModels.add(new LanguageModel("Gujarati", R.id.gujratiSong));
        languageModels.add(new LanguageModel("Odia", R.id.odiaSong));
        languageModels.add(new LanguageModel("Rajasthani", R.id.rajasthaniSong));
        languageModels.add(new LanguageModel("Haryanvi", R.id.haryanviSong));
        languageModels.add(new LanguageModel("Malayalam", R.id.malayalamSong));
        languageModels.add(new LanguageModel("Marathi", R.id.marathiSong));
        languageModels.add(new LanguageModel("Tamil", R.id.tamilSong));
        languageModels.add(new LanguageModel("Telugu", R.id.teleguSong));


        languageModels.forEach(languageModel -> {
            Button btn = findViewById(languageModel.getButtonId());
            btn.setOnClickListener(v -> {
                if (languageModel.getButtonId() == R.id.allSong) {
                    languageModels.forEach(languageModel1 -> {
                        Button btn2 = findViewById(languageModel1.getButtonId());
                        btn2.setBackgroundColor(getResources().getColor(R.color.gray));
                    });
                    ((Button) findViewById(R.id.allSong)).setBackgroundColor(getResources().getColor(R.color.accent3));
                    selectedLanguages = new ArrayList<>();
                    loadHomePage(languageModel.getParameter());
                    ed.putString("languages", "").commit();
                } else {
                    ((Button) findViewById(R.id.allSong)).setBackgroundColor(getResources().getColor(R.color.gray));
                    if (LanguageModel.isLanguagePresent(languageModel, selectedLanguages)) {
                        btn.setBackgroundColor(getResources().getColor(R.color.gray));
                        selectedLanguages = LanguageModel.removeLanguage(languageModel, selectedLanguages);
                    } else {
                        btn.setBackgroundColor(getResources().getColor(R.color.accent3));
                        selectedLanguages.add(languageModel);
                    }

                    languageParam = "";
                    selectedLanguages.forEach(languageModel1 -> {
                        if (!languageParam.equals("")) languageParam += ",";
                        languageParam += languageModel1.getParameter();
                    });
                    Log.d("debugTest", languageParam);
                    loadHomePage(languageParam);
                    ed.putString("languages", languageParam).commit();
                }

//                loadHomePage(languageModel.getParameter());
//                languageModels.forEach(languageModel1 -> {
//                    Button btn2 = findViewById(languageModel1.getButtonId());
//                    btn2.setBackgroundColor(getResources().getColor(R.color.gray));
//                });
//                btn.setBackgroundColor(getResources().getColor(R.color.black));
            });
            btn.setTextColor(getResources().getColor(R.color.white));

        });


        languageParam = sp.getString("languages", "");
        if (languageParam.equals("")) {
            ((Button) findViewById(R.id.allSong)).setBackgroundColor(getResources().getColor(R.color.accent3));
            loadHomePage(languageModels.get(0).getParameter());
        } else {
            selectedLanguages = new ArrayList<>();
            ((Button) findViewById(R.id.allSong)).setBackgroundColor(getResources().getColor(R.color.gray));
            languageModels.forEach(languageModel -> {
                if (languageParam.contains(languageModel.getParameter())) {
                    selectedLanguages.add(languageModel);
                    ((Button) findViewById(languageModel.getButtonId())).setBackgroundColor(getResources().getColor(R.color.accent3));
                }
            });
            loadHomePage(languageParam);
        }


    }

    private void searchSongs(String s) {
        ArrayList<SongModel> searchedSongs = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(s, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray results = response.getJSONObject("data").getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        SongModel songModel = new SongModel();
                        songModel.setName(results.getJSONObject(i).getString("name"));
                        songModel.setId(results.getJSONObject(i).getString("id"));
                        songModel.setDuration(results.getJSONObject(i).getString("duration"));
                        songModel.setLanguage(results.getJSONObject(i).getString("language"));
                        songModel.setUrl(results.getJSONObject(i).getString("url"));
                        songModel.setFeaturedArtists(results.getJSONObject(i).getString("primaryArtists"));
                        songModel.setArtistId(results.getJSONObject(i).getString("primaryArtistsId"));
                        JSONArray image = results.getJSONObject(i).getJSONArray("image");
                        songModel.setImage(image.getJSONObject(image.length() - 1).getString("link"));


                        searchedSongs.add(songModel);

                    }

                    RecyclerView searchedRV = findViewById(R.id.searchItemRV);
                    searchedRV.setAdapter(new SongAdapter(searchedSongs, HomepageActivity.this, true));
                    searchedRV.setHasFixedSize(true);
                    searchedRV.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, LinearLayoutManager.VERTICAL, false));

                } catch (Exception e) {
                    Toast.makeText(HomepageActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomepageActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(HomepageActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    private void loadHomePage(String language) {
        ArrayList<SongModel> songs = new ArrayList<>();
        ArrayList<AlbumModel> albums = new ArrayList<>();
        ArrayList<AlbumModel> trendingAlbums = new ArrayList<>();
        ArrayList<PlaylistModel> playlists = new ArrayList<>();

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

                    JSONArray playlist = albumData.getJSONArray("playlists");
                    for (int i = 0; i < playlist.length(); i++) {
                        PlaylistModel playlistModel = new PlaylistModel();
                        playlistModel.setId(playlist.getJSONObject(i).getString("id"));
                        playlistModel.setName(playlist.getJSONObject(i).getString("title"));
                        playlistModel.setSubtitle(playlist.getJSONObject(i).getString("subtitle"));
                        playlistModel.setUrl(playlist.getJSONObject(i).getString("url"));
                        playlistModel.setSongCount(playlist.getJSONObject(i).getString("songCount"));
                        JSONArray images = playlist.getJSONObject(i).getJSONArray("image");
                        playlistModel.setImage(images.getJSONObject(images.length() - 1).getString("link"));

                        playlists.add(playlistModel);

                    }
                    RecyclerView playlistAD = findViewById(R.id.playlistRV);
                    playlistAD.setAdapter(new PlaylistAdapter(playlists, HomepageActivity.this));
                    playlistAD.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, RecyclerView.HORIZONTAL, false));
                    playlistAD.setHasFixedSize(true);


                    album = albumData.getJSONObject("trending").getJSONArray("albums");

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
                        } else trendingAlbums.add(albumModel);
                    }
                    RecyclerView albumAD = findViewById(R.id.albumRV);
                    albumAD.setAdapter(new AlbumAdapter(albums, HomepageActivity.this));
                    albumAD.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    albumAD.setHasFixedSize(true);

                    RecyclerView trendingAlbumAD = findViewById(R.id.trendingAlbumRV);
                    trendingAlbumAD.setAdapter(new AlbumAdapter(trendingAlbums, HomepageActivity.this));
                    trendingAlbumAD.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    trendingAlbumAD.setHasFixedSize(true);

                    JSONArray song = albumData.getJSONObject("trending").getJSONArray("songs");
                    for (int j = 0; j < song.length(); j++) {
                        SongModel songModel = new SongModel();
                        songModel.setName(song.getJSONObject(j).getString("name"));
                        songModel.setId(song.getJSONObject(j).getString("id"));
                        songModel.setDuration(song.getJSONObject(j).getString("duration"));
                        JSONArray artists = song.getJSONObject(j).getJSONArray("primaryArtists");
                        String featuredArtists = "";
                        String featuredArtistId = "";
                        for (int k = 0; k < artists.length(); ++k) {
                            if (!featuredArtists.equals(""))
                                featuredArtists += ", ";
                            featuredArtists += artists.getJSONObject(k).getString("name");
                            if (!featuredArtistId.equals(""))
                                featuredArtistId += ", ";
                            featuredArtistId += artists.getJSONObject(k).getString("id");
                        }

                        songModel.setFeaturedArtists(featuredArtists);
                        songModel.setArtistId(featuredArtistId);
//                        songModel.setFeaturedArtists(song.getJSONObject(j).getString("featuredArtists"));
                        songModel.setUrl(song.getJSONObject(j).getString("url"));
                        JSONArray image = song.getJSONObject(j).getJSONArray("image");
                        songModel.setImage(image.getJSONObject(image.length() - 1).getString("link"));
                        songModel.setThumbnail(image.getJSONObject(0).getString("link"));


                        songs.add(songModel);


                    }

                    RecyclerView songAD = findViewById(R.id.songsRV);
                    songAD.setAdapter(new SongAdapter(songs, HomepageActivity.this, false));
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
        openPlayerScreen(false);
    }

    public void openPlayerScreen(Boolean openUpcomingSongsList) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View contentView = LayoutInflater.from(this).inflate(R.layout.playing_music, null);
        dialog.setContentView(contentView);
//        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        View bottomSheet = (View) contentView.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Set the initial state to expanded

        ImageView dismiss = dialog.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialog1 -> {
            isPlayerScreenShown = false;
        });

        ImageView upcomingSong = dialog.findViewById(R.id.upcomingSong);
        upcomingSong.setOnClickListener(v -> {
            showUpcomingSongs(dialog);
        });
        LottieAnimationView playPauseBtn = dialog.findViewById(R.id.playPauseBtn);
        if (openUpcomingSongsList) {
            new Handler().postDelayed(() -> {
                showUpcomingSongs(dialog);
                if (player != null) {
                    if (!player.isPlaying()) {
                        playPauseBtn.setMinFrame(66);
                        playPauseBtn.setMaxFrame(67);
                        playPauseBtn.playAnimation();
                    } else {
                        playPauseBtn.setMinFrame(29);
                        playPauseBtn.setMaxFrame(30);
                        playPauseBtn.playAnimation();
                    }
                }
            }, 300);
        }


        playPauseBtn.setOnClickListener(v -> {
            handlePlayPauseBtnAnim(playPauseBtn);
        });
        if (player != null) {
            if (!player.isPlaying()) {
                playPauseBtn.setMinFrame(66);
                playPauseBtn.setMaxFrame(67);
                playPauseBtn.playAnimation();
            } else {
                playPauseBtn.setMinFrame(29);
                playPauseBtn.setMaxFrame(30);
                playPauseBtn.playAnimation();
            }
        }

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
                playPrevSong();
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

        TextView songName = dialog.findViewById(R.id.songNameBS);
        TextView artistName = dialog.findViewById(R.id.artistNameBS);

        isPlayerScreenShown = true;
        updatePlayerScreen(dialog);
        dialog.show();

        songName.setSelected(true);
        artistName.setSelected(true);

    }

    public void showUpcomingSongs(BottomSheetDialog parentDialog) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View songs = LayoutInflater.from(this).inflate(R.layout.songlis_bottomsheet, null);
        dialog.setContentView(songs);


        TextView name = dialog.findViewById(R.id.albumName);
        name.setText("Currently Playing");

        ((ImageView) dialog.findViewById(R.id.dismiss)).setOnClickListener(v -> {
            dialog.dismiss();
        });

        ((ImageView) dialog.findViewById(R.id.songImage)).setVisibility(View.GONE);
        ((ImageView) dialog.findViewById(R.id.playAll)).setVisibility(View.GONE);
        ((ImageView) dialog.findViewById(R.id.shuffle)).setVisibility(View.GONE);
        RecyclerView songRV = dialog.findViewById(R.id.songsRV);
        songRV.setAdapter(new SongAdapter(currentlyPlaying, HomepageActivity.this, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (parentDialog != null) {
                    LottieAnimationView playPauseBtn = parentDialog.findViewById(R.id.playPauseBtn);

                    if (player != null) {
                        if (player.isPlaying()) {
                            playPauseBtn.setMinFrame(66);
                            playPauseBtn.setMaxFrame(67);
                            playPauseBtn.playAnimation();
                        } else {
                            playPauseBtn.setMinFrame(29);
                            playPauseBtn.setMaxFrame(30);
                            playPauseBtn.playAnimation();
                        }
                    }
                }
                return null;
            }
        }));
        songRV.setHasFixedSize(true);
        songRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        dialog.show();
        new Handler().postDelayed(() -> {
            int height = songRV.getChildAt(curPos).getHeight();
            NestedScrollView nestedScrollView = dialog.findViewById(R.id.scrollView);
            nestedScrollView.smoothScrollTo(0, curPos * height);
        }, 100);


    }

    public void showUpcomingSongs() {
        showUpcomingSongs(null);
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

    public void playPrevSong() {
        curPos -= 1;
        if (curPos <= 0) curPos = 0;
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
        showNotification(R.drawable.pause);
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
                showNotification(R.drawable.pause);
            }
            if (player != null && !player.isPlaying()) {
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                showNotification(R.drawable.play);
            }

            updateSeekbar();
        }, 10);
    }

    ArrayList<SongModel> upcomingSongsList = new ArrayList<>();

    void updatePlayerScreen(BottomSheetDialog dialog) {

        if (!isPlayerScreenShown) return;

        TextView songName = dialog.findViewById(R.id.songNameBS);
        TextView artistName = dialog.findViewById(R.id.artistNameBS);
        ImageView songImage = dialog.findViewById(R.id.songImage);
        TextView endTime = dialog.findViewById(R.id.endTime);
        TextView currentTime = dialog.findViewById(R.id.currentTime);
        SeekBar seekPlaying = dialog.findViewById(R.id.seekPlaying);

//        if (currentlyPlaying.subList(curPos + 1, currentlyPlaying.size()).size() != upcomingSongsList.size()){
//            RecyclerView upcomingSongs = dialog.findViewById(R.id.upcomingRV);
//            upcomingSongsList = new ArrayList<>();
//            upcomingSongsList.addAll(currentlyPlaying.subList(curPos+1,currentlyPlaying.size()));
//            upcomingSongs.setAdapter(new SongAdapter(upcomingSongsList, this));
//            upcomingSongs.setHasFixedSize(true);
//            upcomingSongs.setLayoutManager(new LinearLayoutManager(HomepageActivity.this,LinearLayoutManager.VERTICAL,false));
//        }

        new Handler().postDelayed(() -> {

            SongModel currentPlayingSong = currentlyPlaying.get(curPos);

            if (!currentPlayingSong.getName().equals(songName.getText().toString()))
                songName.setText(currentPlayingSong.getName());

            if (!currentPlayingSong.getFeaturedArtists().equals(artistName.getText().toString()))
                artistName.setText(currentPlayingSong.getFeaturedArtists());
            endTime.setText(SongModel.convertDuration(Integer.valueOf(player.getDuration() / 1000)));
            Glide.with(this).load(currentPlayingSong.getImage()).thumbnail(Glide.with(this).load(currentPlayingSong.getThumbnail())).into(songImage);

            seekPlaying.setMax(player.getDuration());
//            seekPlaying.setProgress(player.getCurrentPosition());
            currentTime.setText(SongModel.convertDuration(Integer.valueOf(seekPlaying.getProgress() / 1000)));

            if (player == null) seekPlaying.setProgress(0);
            else if (player != null) {
//                seekPlaying.setProgress(seekBar.getProgress()+10);
                seekPlaying.setProgress(player.getCurrentPosition());
            }


            updatePlayerScreen(dialog);
        }, 10);
    }

    void startPlayingSong(SongModel songModel, int pos) {
        try {
            if (pos == currentlyPlaying.size() - 1) {
                loadRecommendedSongs(currentlyPlaying.get(pos));
                Log.d("debugTest", String.valueOf(pos));
            }
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

                    if (pos == currentlyPlaying.size() - 1) {
                        loadRecommendedSongs(currentlyPlaying.get(pos));
                    }
                    playNextSong();
                }


            });


            curPos = pos;
            updateCurrentPlaying(songModel);

        } catch (Exception e) {
            Toast.makeText(this, "Unable to play this song", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecommendedSongs(SongModel songModel) {
        String recommendedSongsAPI = "https://saavn.me/artists/";
        String ss = songModel.getId().trim();
        ArrayList<String> artistID = songModel.getArtistId();
        artistID.forEach(v1 -> {
            String recAPI = recommendedSongsAPI + v1.trim() + "/recommendations/" + ss;

            Log.d("debugTest", recAPI);
            Log.d("debugTest", v1);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(recAPI, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray results = response.getJSONArray("data");
                        for (int i = 0; i < results.length(); i++) {
                            boolean bool = true;
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
                            for (int j = 0; j < currentlyPlaying.size(); j++) {
                                if (currentlyPlaying.get(j).getId().equals(recommended.getId())){
                                    bool = false;
                                    break;
                                }

                            }
                            if(bool==true)currentlyPlaying.add(recommended);

                        }


                    } catch (Exception e) {

                        Toast.makeText(HomepageActivity.this, "failed to load the song", Toast.LENGTH_SHORT).show();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(HomepageActivity.this, "failed to load the song", Toast.LENGTH_SHORT).show();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(HomepageActivity.this);
            requestQueue.add(jsonObjectRequest);

        });


    }

    void fetchSongDownloadURL(SongModel song, int pos) {
        if (pos == currentlyPlaying.size() - 1) {
            loadRecommendedSongs(currentlyPlaying.get(pos));
            Log.d("debugTest", String.valueOf(pos));
        }
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

    void handlePlayPauseBtnAnim(LottieAnimationView playPauseBtn) {
        if (player != null) {
            if (player.isPlaying()) {
                playPauseBtn.setMinFrame(30);
                playPauseBtn.setMaxFrame(67);
                playPauseBtn.playAnimation();
                player.pause();
            } else {
                playPauseBtn.setMinFrame(0);
                playPauseBtn.setMaxFrame(30);
                playPauseBtn.playAnimation();
                player.start();
            }
        }
    }

    void playAll(SongModel song, int pos) {


    }

    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(HomepageActivity.this);
//        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
//        isBound = false;
        musicService = null;
    }

    void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, HomepageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        try {
            if (player != null && currentlyPlaying.size() > 0) {
                largeIcon = Glide.with(this).asBitmap().load(currentlyPlaying.get(curPos).getImage()).submit().get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL2_ID)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(largeIcon)
                .setContentTitle(currentlyPlaying.get(curPos).getName())
                .setContentText(currentlyPlaying.get(curPos).getFeaturedArtists())
                .addAction(R.drawable.previous_button, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Pause", pausePendingIntent)
                .addAction(R.drawable.next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 33) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
            return;
        }
        notificationManagerCompat.notify(2, notification);
    }

    @Override
    public void nextClicked() {
        playNextSong();
    }

    @Override
    public void prevClicked() {
        playPrevSong();
    }

    @Override
    public void playClicked() {
        if (player != null && play != null && pause != null) {
            if (player.isPlaying()) {
                pause.callOnClick();
            } else {
                play.callOnClick();
            }
        }
    }
}