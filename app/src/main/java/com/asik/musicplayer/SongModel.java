package com.asik.musicplayer;

import android.text.Html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SongModel {
    String id;
    String name;
    int duration;
    String featuredArtists;
    boolean hasLyrics;
    String url;
    String image;
    String downloadUrl;
    String language;
    String thumbnail;
    ArrayList<String> artistId;

    public ArrayList<String> getArtistId() {

        if (artistId.size() <=2) return artistId;
        return new ArrayList<>(artistId.subList(0, 2));
    }

    public void setArtistId(String artistId) {
        String[] ar=artistId.split(",");
         List<String> list = Arrays.asList(ar);
        ArrayList<String> ar2 = new ArrayList<>(list);
        this.artistId = ar2;
    }

    public void setHasLyrics(boolean hasLyrics) {
        this.hasLyrics = hasLyrics;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    SongModel() {
        this.downloadUrl = "";
        this.id = "";
        this.name = "";
        this.duration = 0;
        this.featuredArtists = "";
        this.hasLyrics = false;
        this.url = "";
        this.image = "";
        this.language = "";
        this.artistId = new ArrayList<>();
        this.thumbnail = "";
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

    public String getDuration() {
        return convertDuration(this.duration);
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public void setDuration(String duration) {
        this.duration = Integer.parseInt(duration);
    }

    public String getFeaturedArtists() {
        return featuredArtists;
    }

    public void setFeaturedArtists(String featuredArtists) {
        this.featuredArtists = Html.fromHtml(featuredArtists).toString();
    }

    public boolean isHasLyrics() {
        return hasLyrics;
    }

    public void setHasLyrics(String hasLyrics) {
        this.hasLyrics = hasLyrics.equals("true");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url.contains("/s/song/")){
            String[] splits = url.split("/");
            this.url = "https://www.jiosaavn.com/song/"+splits[splits.length-2]+"/"+splits[splits.length-1];
        }
        else this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static String convertDuration(int duration){
        String min =String.valueOf(duration/60);
        String sec = String.valueOf(duration%60);
        if ((duration/60)<10) min = "0"+min;
        if ((duration%60)<10) sec = "0"+sec;

        if ((duration/60) > 20) return "";
        return min + ":"+sec;
    }
}
