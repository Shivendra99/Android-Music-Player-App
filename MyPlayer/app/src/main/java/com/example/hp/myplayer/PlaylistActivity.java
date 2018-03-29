package com.example.hp.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {

    ArrayList<String> playlistList;
    ArrayList<Long> songCount;
    ListView playlistListView;
    ImageView coverImage3;
    TextView titleText3;
    Button playPauseButton3;

    boolean flag;

    PlaylistDatabase playlistDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistListView=findViewById(R.id.playlistListView);
        coverImage3=findViewById(R.id.coverImage3);
        titleText3=findViewById(R.id.titleText3);
        playPauseButton3=findViewById(R.id.playPauseButton3);

        playlistDatabase=new PlaylistDatabase(this);

        setPlaylistListView();
        //setSubView();

        setBroadcastReceiver();
    }

    public void setBroadcastReceiver(){}

    public void setPlaylistListView() {

        playlistList=new ArrayList<String>(1);
        songCount=new ArrayList<Long>(1);

        playlistList=playlistDatabase.getPlaylistName();
        songCount= playlistDatabase.getPlaylistItemCount();

        CustomListViewAdapter customListViewAdapter=new CustomListViewAdapter(this,playlistList,songCount);
        playlistListView.setAdapter(customListViewAdapter);

        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PlaylistActivity.this,"Playlist item "+i+" clicked",Toast.LENGTH_SHORT).show();
                ArrayList<AudioInfo> playlist=WelcomeActivity.getPlaylistSongs(playlistDatabase.getItems(playlistList.get(i)));

                Intent intent=new Intent(PlaylistActivity.this,MainList.class);
                intent.putExtra("audioList",playlist);
                startActivity(intent);
            }
        });

    }

    public void setSubView(AudioInfo audioObj,boolean flag) {
        titleText3.setText(audioObj.getAudioName());
        if(CoverPlay.flag==false)
            playPauseButton3.setText("Play");
        else
            playPauseButton3.setText("Pause");
        BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
        bitmapOptions.inSampleSize=16;
        byte[] b=audioObj.getCoverImageByteArray(this);
        if(b!=null)
            coverImage3.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length,bitmapOptions));
        this.flag=flag;
    }

}