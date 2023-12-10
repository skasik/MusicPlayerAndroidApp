package com.asik.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomepageActivity extends AppCompatActivity {

    MediaPlayer player;
    ArrayList<SongModel> currentlyPlaying =new ArrayList<>();
    int curPos = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);



        View playing = findViewById(R.id.playing);
        CardView playingImage = findViewById(R.id.card);

        playingImage.setOnClickListener(v -> {
            openPlayerScreen();
        });

        playing.setOnClickListener(v -> {
            openPlayerScreen();
        });


        loadHomePage("hindi,english,bengali");
//        loadHomePage("hindi");
    }

    private void loadHomePage(String language){
        ArrayList<SongModel> songs = new ArrayList<>();
        ArrayList<AlbumModel> albums = new ArrayList<>();

        String API_LINK = "https://saavn.me/modules?language="+language;
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
                        for(int j=0; j<artists.length(); ++j){
                            if (!featuredArtists.equals(""))
                                featuredArtists += ", ";
                            featuredArtists += artists.getJSONObject(j).getString("name");
                        }
                        albumModel.setPlayCount(album.getJSONObject(i).getString("playCount"));
                        albumModel.setFeaturedArtists(featuredArtists);
                        JSONArray image = album.getJSONObject(i).getJSONArray("image");
                        albumModel.setImage((image.getJSONObject(image.length() - 1).getString("link")));
//                        albumModel.setSongs();



                        if (type.equals("song")){
                            songs.add(AlbumToSong(albumModel));
                        }
                        else albums.add(albumModel);
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
                        for(int k=0; k<artists.length(); ++k){
                            if (!featuredArtists.equals(""))
                                featuredArtists += ", ";
                            featuredArtists += artists.getJSONObject(k).getString("name");
                        }

                        songModel.setFeaturedArtists(featuredArtists);
//                        songModel.setFeaturedArtists(song.getJSONObject(j).getString("featuredArtists"));
                        songModel.setUrl(song.getJSONObject(j).getString("url"));
                        JSONArray image = song.getJSONObject(j).getJSONArray("image");
                        songModel.setImage(image.getJSONObject(image.length()-1).getString("link"));


                        songs.add(songModel);


                    }

                    RecyclerView songAD = findViewById(R.id.songsRV);
                    songAD.setAdapter(new SongAdapter(songs, HomepageActivity.this));
                    songAD.setLayoutManager(new LinearLayoutManager(HomepageActivity.this,LinearLayoutManager.VERTICAL,false));
                    songAD.setHasFixedSize(true);
                    songAD.setNestedScrollingEnabled(false);



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

    public static SongModel AlbumToSong(AlbumModel albumModel){
        SongModel songModel = new SongModel();
        songModel.setId(albumModel.getId());
        songModel.setName(albumModel.getName());
        songModel.setUrl(albumModel.getUrl());
        songModel.setImage(albumModel.getImage());
        songModel.setFeaturedArtists(albumModel.getFeaturedArtists());
        songModel.setDuration(albumModel.getPlayCount());

        return songModel;
    }


    public void openPlayerScreen(){
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View contentView = LayoutInflater.from(this).inflate(R.layout.playing_music, null);
        dialog.setContentView(contentView);
//        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        View bottomSheet = (View) contentView.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

        TextView songName = dialog.findViewById(R.id.songName);
        TextView artistName = dialog.findViewById(R.id.artistName);

        songName.setText(currentlyPlaying.get(curPos).getName());
        artistName.setText(currentlyPlaying.get(curPos).getFeaturedArtists());

        behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Set the initial state to expanded
//        behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottomsheet_height));


        dialog.show();
    }

    public void updateCurrentPlaying(SongModel songModel){
        TextView songName = findViewById(R.id.selectedMusic);
        TextView artistName = findViewById(R.id.selectedArtist);
        ImageView songImage = findViewById(R.id.songImage);
        View parent = findViewById(R.id.bottomParent);
        parent.setVisibility(View.VISIBLE);

        Glide.with(this).load(songModel.getImage()).into(songImage);
        songName.setText(songModel.getName());
        artistName.setText(songModel.getFeaturedArtists());
    }
}