package com.asik.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
                dialog.setContentView(LayoutInflater.from(homepageActivity).inflate(R.layout.songlis_bottomsheet, null));
                TextView albname = dialog.findViewById(R.id.albumName);
                albname.setSelected(true);
                albname.setText(albumModel.getName());
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
                                songsObject.setThumbnail(albumModel.getThumbnail());
                                JSONArray downloadURLs = songsAPI.getJSONObject(i).getJSONArray("downloadUrl");
                                songsObject.setDownloadUrl(downloadURLs.getJSONObject(downloadURLs.length()-1).getString("link"));


                                songsOfThisAlbum.add(songsObject);


                            }

                            RecyclerView objetSongs = dialog.findViewById(R.id.songsRV);
                            objetSongs.setAdapter(new SongAdapter(songsOfThisAlbum, homepageActivity));
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

                dialog.show();
                RequestQueue requestQueue = Volley.newRequestQueue(homepageActivity);
                requestQueue.add(jsonObjectRequest);
            });



        }
    }
}
