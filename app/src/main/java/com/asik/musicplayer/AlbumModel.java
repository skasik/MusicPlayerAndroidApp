package com.asik.musicplayer;

import android.text.Html;

import java.util.ArrayList;

public class AlbumModel {
    String id;
    String name;
    int songCount;
    String featuredArtists;

    String url;
    String image;
    ArrayList<SongModel> songs;

    String thumbnail;

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

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
        this.songCount = 0;
        this.thumbnail = "";
    }

    void addSong(SongModel song) {
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
        this.name = Html.fromHtml(name).toString();
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
        this.featuredArtists = Html.fromHtml(featuredArtists).toString();
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
