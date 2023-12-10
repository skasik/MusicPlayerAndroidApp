package com.asik.musicplayer;

import java.util.ArrayList;

public class AlbumModel {
    String id;
    String name;
    int songCount;
    String featuredArtists;

    String url;
    String image;
    ArrayList<SongModel> songs;

    int playCount;

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(String playCount) {
        if (playCount.equals("")) this.playCount = 0;
        else this.playCount = Integer.parseInt(playCount);
    }

    AlbumModel() {

        this.id = "";
        this.name = "";
        this.songCount = 1;
        this.featuredArtists = "";

        this.url = "";
        this.image = "";
        this.songs = new ArrayList<>();
        this.playCount = 0;
    }

    void addSong(SongModel song){
        this.songs.add(song);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.replaceAll("&quot;", "\"");
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(String songCount) {
        this.songCount = Integer.parseInt(songCount);
    }

    public String getFeaturedArtists() {
        return featuredArtists;
    }

    public void setFeaturedArtists(String featuredArtists) {
        this.featuredArtists = featuredArtists;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<SongModel> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<SongModel> songs) {
        this.songs = songs;
    }
}
