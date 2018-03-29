package com.example.hp.myplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WelcomeActivity extends AppCompatActivity {

    RelativeLayout welcomeCoverRelativeLayout;
    TextView welcomeNowPlaying,getWelcomeNowPlayingInfo,allSongs,allSongsCount,favouriteSongs,favouriteSongsCount,playlist,playlistCount,mostPlayed,mostPlayedCount;
    Button welcomePlayPauseButton;
    int requestCode=123;
    boolean flag=false;
    static ArrayList<AudioInfo> audioInfoArrayList=new ArrayList<>(1);

    static PlaylistDatabase playlistDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeCoverRelativeLayout=findViewById(R.id.welcomeCoverRelativeLayout);
        welcomeNowPlaying=findViewById(R.id.welcomeNowPlaying);
        getWelcomeNowPlayingInfo=findViewById(R.id.welcomeNowPlayingInfo);
        welcomePlayPauseButton=findViewById(R.id.welcomePlayPauseButton);
        allSongs=findViewById(R.id.allSongs);
        allSongsCount=findViewById(R.id.allSongsCount);
        favouriteSongs=findViewById(R.id.favouriteSongs);
        favouriteSongsCount=findViewById(R.id.favouriteSongsCount);
        playlist=findViewById(R.id.playlists);
        playlistCount=findViewById(R.id.playlistsCount);
        mostPlayed=findViewById(R.id.mostPlayed);
        mostPlayedCount=findViewById(R.id.mostPlayedCount);

        playlistDatabase=new PlaylistDatabase(this);

        checkPermission();
        setBroadcastReciever();
        setLabels();

        setWelcomeCover(0,false);

    }

    public void checkPermission() {
        if(Build.VERSION.SDK_INT>=23)
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},requestCode);
                return;
            }
            else { Log.i("logText","in else");
                loadMedia();
            }
        }
        else
            loadMedia();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==this.requestCode)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                loadMedia();

            else
                Toast.makeText(this,"Permission Denied !!",Toast.LENGTH_SHORT).show();
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void loadMedia() {
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection=MediaStore.Audio.Media.IS_MUSIC+"!=0";
        Cursor c=getContentResolver().query(uri,null,selection,null,null);
        if(c!=null)
        {
            c.moveToFirst();

            do {
                String audioName=c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String artist=c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album=c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String url=c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                String albumID=c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                float size= (float) (1.0*(c.getInt(c.getColumnIndex(MediaStore.Audio.Media.SIZE)))/(1024*1024));

                if(new File(url).exists())
                    audioInfoArrayList.add(new AudioInfo(url,audioName,artist,album,albumID,size));

            }while (c.moveToNext());

            c.close();
            Collections.sort(audioInfoArrayList,new WelcomeActivity.NameComparator());
        }
        else
            Toast.makeText(this,"No song(s) available",Toast.LENGTH_SHORT).show();

    }

    public void setBroadcastReciever() {
        BroadcastReceiver broadcastReceiver1=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { Log.i("logText","broadcast recieved");
                if(intent.getStringExtra("url")!=null){
                    String url=intent.getStringExtra("url");
                    for(int i=0;i<audioInfoArrayList.size();i++)
                        if((audioInfoArrayList.get(i).getUrl()).compareTo(url)==0) {
                            setWelcomeCover(i, intent.getBooleanExtra("flag", true));
                            break;
                        }
                }
            }
        };
        final BroadcastReceiver broadcastReceiver2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getSerializableExtra("selectedSongs") != null) {
                    ArrayList<String> selectedSongs = (ArrayList<String>) intent.getSerializableExtra("selectedSongs");
                    if(intent.getStringExtra("playlistName")!=null)
                        managePlaylists(selectedSongs,intent.getStringExtra("playlistName"));
                 }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver1,new IntentFilter("currentAudio"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver2,new IntentFilter("songsForPlaylist"));
    }

    public void managePlaylists(ArrayList<String> selectedSongs,String playlistName) {
        playlistDatabase.addTable(playlistName);
        for(String url:selectedSongs)
            playlistDatabase.addItem(playlistName, url);
        Toast.makeText(this,selectedSongs.size()+" songs added sucessfuly to "+playlistName,Toast.LENGTH_SHORT).show();
    }

    public void setWelcomeCover (int i,Boolean play) {
        welcomeNowPlaying.setText(audioInfoArrayList.get(i).getAudioName());
        getWelcomeNowPlayingInfo.setText(audioInfoArrayList.get(i).getArtist());

        if(play==false)
            welcomePlayPauseButton.setText("Play");
        else
            welcomePlayPauseButton.setText("Pause");

        byte[] b=audioInfoArrayList.get(i).getCoverImageByteArray(this);
        if(b!=null) {
            Drawable backgroundDrawable= new BitmapDrawable(this.getResources(), BitmapFactory.decodeByteArray(b,0,b.length));
            welcomeCoverRelativeLayout.setBackground(backgroundDrawable);
        }
        flag=play;
    }

    public void welcomeOnClick(View v){
        if(v.getId()==R.id.welcomeCoverRelativeLayout||v.getId()==R.id.welcomePlayPauseButton) {
         if(CoverPlay.currentSong==null){
             if(CoverPlay.currentSong==null) {
                 if (v.getId() == R.id.welcomePlayPauseButton) {
                     Intent intent = new Intent(this, CoverPlay.class);
                     intent.putExtra("Play", audioInfoArrayList);
                     intent.putExtra("Position", 0);
                     startActivityForResult(intent, requestCode);

                     Intent i = new Intent("playPauseBroadcast");
                     if (flag == false) {
                         flag = true;
                         i.putExtra("play", true);
                         welcomePlayPauseButton.setText("Pause");
                     } else {
                         flag = false;
                         i.putExtra("play", false);
                         welcomePlayPauseButton.setText("Play");
                     }
                     LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                 }
                 else if (v.getId() == R.id.welcomeCoverRelativeLayout) {
                     Intent i = new Intent(this, CoverPlay.class);
                     i.putExtra("Play", audioInfoArrayList);
                     i.putExtra("Position", 0);
                     startActivityForResult(i, requestCode);
                 }
             }
         }
         else if (v.getId() == R.id.welcomeCoverRelativeLayout) {
                Intent intent = new Intent(this, CoverPlay.class);
                startActivityForResult(intent, requestCode);
            }
         else if(v.getId()==R.id.welcomePlayPauseButton){
             Intent i=new Intent("playPauseBroadcast");
             if(!flag) {
                 flag=true;
                 i.putExtra("play",true);
                 welcomePlayPauseButton.setText("Pause");
             }
             else {
                 flag=false;
                 i.putExtra("play",false);
                 welcomePlayPauseButton.setText("Play");
             }
             LocalBroadcastManager.getInstance(this).sendBroadcast(i);
         }
        }
        else if(v.getId()==R.id.allSongsRelativeLayout){
            Intent intent=new Intent(this,MainList.class);
            intent.putExtra("audioList",audioInfoArrayList);
            startActivity(intent);
        }
        else if(v.getId()==R.id.favouriteRelativeLayout){}
        else if(v.getId()==R.id.playlistRdelativeLayout){
            Intent intent=new Intent(this,PlaylistActivity.class);
            startActivity(intent);
        }
        else if(v.getId()==R.id.mostPlayedRelativeLayout){
            //for test
            Intent intent=new Intent(this,AndroidDatabaseManager.class);
            startActivity(intent);
        }
    }

    public void setLabels() {
        allSongs.setText("All Songs");
        allSongsCount.setText(audioInfoArrayList.size()+" songs");
        favouriteSongs.setText("Favourites");
        favouriteSongsCount.setText(0+" songs");
        playlist.setText("Playlists");
        playlistCount.setText("0"+" songs");
        mostPlayed.setText("Most Played");
        mostPlayedCount.setText(0+" songs");
    }

    static public ArrayList<AudioInfo> getPlaylistSongs(ArrayList<String> urlList){
        ArrayList<AudioInfo> playlist=new ArrayList<>(1);

        int position=0;

        while(position!=audioInfoArrayList.size()-1){ Log.i("logText","urlist(0) "+urlList.get(0)); Log.i("logText","audioInfoArrayList(0) "+audioInfoArrayList.get(0).getUrl());
            if(urlList.contains(audioInfoArrayList.get(position).getUrl()))
                playlist.add(audioInfoArrayList.get(position));
            position++;
        }

        return playlist;
    }

    public class NameComparator implements Comparator<AudioInfo> {
        @Override
        public int compare(AudioInfo audioInfo, AudioInfo t1) {
            return audioInfo.getAudioName().compareTo(t1.getAudioName());
        }
    }

}