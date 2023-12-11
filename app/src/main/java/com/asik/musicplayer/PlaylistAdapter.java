package com.asik.musicplayer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    ArrayList<PlaylistModel> playlistModels;
    HomepageActivity homepageActivity;

    public PlaylistAdapter(ArrayList<PlaylistModel> playlistModels, HomepageActivity homepageActivity) {
        this.playlistModels = playlistModels;
        this.homepageActivity = homepageActivity;
    }

    @NonNull
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item,parent,false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.ViewHolder holder, int position) {
         holder.setPlaylistData(playlistModels.get(position), position);
    }

    @Override
    public int getItemCount() {
        return playlistModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        TextView followers;

        LinearLayout playlistClick;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.albumImage);
            name = itemView.findViewById(R.id.albumName);
            followers = itemView.findViewById(R.id.artistsName);
            playlistClick = itemView.findViewById(R.id.album);

        }

        public void setPlaylistData(PlaylistModel playlistModel, int position) {
            name.setText(playlistModel.getName());
            followers.setText(playlistModel.getSubtitle());
            Glide.with(homepageActivity).load(playlistModel.getImage()).into(image);

            playlistClick.setOnClickListener(v -> {
                ArrayList<SongModel> songsOfPlaylist = new ArrayList<>();
                BottomSheetDialog dialog = new BottomSheetDialog(homepageActivity);
                View songsView = LayoutInflater.from(homepageActivity).inflate(R.layout.songlis_bottomsheet,null);
                dialog.setContentView(songsView);


                View bottomSheet = (View) songsView.getParent();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Set the initial state to expanded

                ImageView dismiss = dialog.findViewById(R.id.dismiss);
                dismiss.setOnClickListener(v1 -> {
                    dialog.dismiss();
                });

                TextView playlistName = dialog.findViewById(R.id.albumName);
                playlistName.setText(playlistModel.getName());
                playlistName.setSelected(true);

                String playlist_API = "https://saavn.me/playlists?id="+playlistModel.getId();

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(playlist_API, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SongModel oneSong = new SongModel();

                        try {
                            JSONArray songs = response.getJSONObject("data").getJSONArray("songs");
                            for (int i = 0;i<songs.length();i++){
                                oneSong.setName(songs.getJSONObject(i).getString("name"));
                                oneSong.setId(songs.getJSONObject(i).getString("id"));
                                oneSong.setDuration(songs.getJSONObject(i).getString("duration"));
                                oneSong.setUrl(songs.getJSONObject(i).getString("url"));
                                oneSong.setFeaturedArtists(songs.getJSONObject(i).getString("primaryArtists"));
                                oneSong.setLanguage(songs.getJSONObject(i).getString("language"));

                                JSONArray image = songs.getJSONObject(i).getJSONArray("image");
                                oneSong.setImage(image.getJSONObject(image.length()-1).getString("link"));
                                oneSong.setThumbnail(playlistModel.getImage());
                                JSONArray downloadURLs = songs.getJSONObject(i).getJSONArray("downloadUrl");
                                oneSong.setDownloadUrl(downloadURLs.getJSONObject(downloadURLs.length()-1).getString("link"));

                                songsOfPlaylist.add(oneSong);

                            }
                            Log.d("debugtest", String.valueOf(songsOfPlaylist.size()));
                            RecyclerView playlist = dialog.findViewById(R.id.songsRV);
                            playlist.setAdapter(new SongAdapter(songsOfPlaylist,homepageActivity));
                            playlist.setHasFixedSize(true);
                            playlist.setLayoutManager(new LinearLayoutManager(homepageActivity,LinearLayoutManager.VERTICAL,false));

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }



                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

                dialog.show();

                RequestQueue requestQueue = Volley.newRequestQueue(homepageActivity);
                requestQueue.add(jsonObjectRequest);
            });

        }
    }
}
