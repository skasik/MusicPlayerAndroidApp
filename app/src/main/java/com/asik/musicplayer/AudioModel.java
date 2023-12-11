package com.asik.musicplayer;

import android.text.Html;

public class AudioModel {
    String artistName;
    String audioName;
    String audioPath;
    String album;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = Html.fromHtml(artistName).toString();
    }

    public String getAudioName(){
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = Html.fromHtml(audioName).toString();
    }

    public String getAudioPath(){
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }

}
