package com.asik.musicplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
import java.util.concurrent.Callable;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    ArrayList<AlbumModel> albums;
    HomepageActivity homepageActivity;


    public AlbumAdapter(ArrayList<AlbumModel> albums, HomepageActivity homepageActivity) {
        this.albums = albums;
        this.homepageActivity = homepageActivity;

    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.ViewHolder holder, int position) {
        holder.setAlbum(albums.get(position), position);

    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        LinearLayout album;
        ImageView imageAlbum;
        TextView albumName;
        TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            album = itemView.findViewById(R.id.album);
            imageAlbum = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            artistName = itemView.findViewById(R.id.artistsName);


        }


        public void setAlbum(AlbumModel albumModel, int position) {
            albumName.setText(albumModel.getName());
            artistName.setText(albumModel.getFeaturedArtists());
            Glide.with(homepageActivity).load(albumModel.getImage()).into(imageAlbum);


            album.setOnClickListener(v -> {
                ArrayList<SongModel> songsOfThisAlbum = new ArrayList<>();
                BottomSheetDialog dialog = new BottomSheetDialog(homepageActivity);
                View contentView = LayoutInflater.from(homepageActivity).inflate(R.layout.songlis_bottomsheet, null);
                dialog.setContentView(contentView);

                View bottomSheet = (View) contentView.getParent();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Set the initial state to expanded

                ImageView playAll = dialog.findViewById(R.id.playAll);
                ImageView shuffle = dialog.findViewById(R.id.shuffle);

                ImageView dismiss = dialog.findViewById(R.id.dismiss);
                dismiss.setOnClickListener(v1 -> {
                    dialog.dismiss();
                });

                TextView albumen = dialog.findViewById(R.id.albumName);
                albumen.setSelected(true);
                albumen.setText(albumModel.getName());

                ImageView im = dialog.findViewById(R.id.songImage);
                Glide.with(homepageActivity).load(albumModel.getImage()).into(im);
                String ALBUM_API = "https://saavn.me/albums?id="+albumModel.getId();
                Log.d("debugTest", ALBUM_API);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ALBUM_API, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray songsAPI = data.getJSONArray("songs");
                            for (int i = 0; i < songsAPI.length(); i++) {
                                SongModel songsObject = new SongModel();
                                songsObject.setId(songsAPI.getJSONObject(i).getString("id"));
                                songsObject.setName(songsAPI.getJSONObject(i).getString("name"));
                                songsObject.setDuration(songsAPI.getJSONObject(i).getString("duration"));
                                songsObject.setFeaturedArtists(songsAPI.getJSONObject(i).getString("primaryArtists"));
                                songsObject.setUrl(songsAPI.getJSONObject(i).getString("url"));
                                JSONArray image = songsAPI.getJSONObject(i).getJSONArray("image");
                                songsObject.setImage(image.getJSONObject(image.length()-1).getString("link"));
//                                songsObject.setThumbnail(image.getJSONObject(0).getString("link"));
                                songsObject.setThumbnail(albumModel.getImage());
                                JSONArray downloadURLs = songsAPI.getJSONObject(i).getJSONArray("downloadUrl");
                                songsObject.setDownloadUrl(downloadURLs.getJSONObject(downloadURLs.length()-1).getString("link"));


                                songsOfThisAlbum.add(songsObject);


                            }

                            RecyclerView objetSongs = dialog.findViewById(R.id.songsRV);
                            objetSongs.setAdapter(new SongAdapter(songsOfThisAlbum, homepageActivity, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    dialog.dismiss();
                                    homepageActivity.openPlayerScreen(true);
                                    return null;
                                }
                            }));
                            objetSongs.setHasFixedSize(true);
                            objetSongs.setLayoutManager(new LinearLayoutManager(homepageActivity, LinearLayoutManager.VERTICAL, false));


                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(homepageActivity, "Not Found", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(homepageActivity, "Not Found", Toast.LENGTH_SHORT).show();
                    }
                });

                playAll.setOnClickListener(v1 -> {
                    homepageActivity.currentlyPlaying=songsOfThisAlbum;
                    homepageActivity.curPos=0;

                    SongModel firstSong = homepageActivity.currentlyPlaying.get(homepageActivity.curPos);
                    if (firstSong.getDownloadUrl().equals("")) homepageActivity.fetchSongDownloadURL(firstSong,homepageActivity.curPos);
                    else homepageActivity.startPlayingSong(firstSong,homepageActivity.curPos);

                    dialog.dismiss();
                    homepageActivity.openPlayerScreen(true);
                });

                shuffle.setOnClickListener(v1 -> {
                    ArrayList<SongModel> temp = new ArrayList<>();
                    temp.addAll(songsOfThisAlbum);
                    Collections.shuffle(temp);
                    homepageActivity.currentlyPlaying =temp;
                    homepageActivity.curPos=0;

                    SongModel firstSong = homepageActivity.currentlyPlaying.get(homepageActivity.curPos);
                    if (firstSong.getDownloadUrl().equals("")) homepageActivity.fetchSongDownloadURL(firstSong,homepageActivity.curPos);
                    else homepageActivity.startPlayingSong(firstSong,homepageActivity.curPos);

                    dialog.dismiss();
                    homepageActivity.openPlayerScreen(true);
                });
                dialog.show();
                RequestQueue requestQueue = Volley.newRequestQueue(homepageActivity);
                requestQueue.add(jsonObjectRequest);




            });



        }
    }
}
