package com.example.hp.myplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by hp on 04-Jan-18.
 */

public class AudioInfo implements Serializable{

    String audioName,artist,album,url,coverImagePath,albumID;
    float size;
    static MediaMetadataRetriever mmr=new MediaMetadataRetriever();

    public AudioInfo(String url,String audioName,String artist,String album,String albumID,float size){
        try {
            this.url=url;
            this.audioName=audioName;
            this.artist=artist;
            this.album=album;
            this.albumID=albumID;
            this.size=size;
        } catch (Exception e) {
            Log.i("logText : ","In AudioInfo class"+coverImagePath+" "+e);
        }
    }

    public void setAudioName(String audioName) {this.audioName = audioName;}
    public void setArtist(String artist) { this.artist = artist;}
    public void setAlbum(String album) { this.album = album;}
    public void setUrl(String url) { this.url = url;}


    public String getAudioName() {return audioName;}
    public String getArtist() {return artist;}
    public String getAlbum() {return album;}
    public String getUrl() {return url;}
    public String getAlbumID() {return albumID;}
    public float getSize() {return size;}

    public byte[] getCoverImageByteArray(Context context) {
        try {
            mmr.setDataSource(context, Uri.parse(url));
            return mmr.getEmbeddedPicture();
        } catch (Exception e) {
            Log.i("logText3 ",url+" error "+e);
        }
        return null;
    }

}