package com.example.hp.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.futuremind.recyclerviewfastscroll.FastScroller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainList extends AppCompatActivity implements SearchView.OnQueryTextListener{

    MainList mainList=this;
    static int requestCode=123;
    static ArrayList<AudioInfo> audioInfoArrayList=new ArrayList<AudioInfo>();
    CustomListAdapter customListAdapter;
    FastScroller fastScroller;
    CustomGridAdapter customGridAdapter;
    TextView titleTextView2;
    ImageView coverImage2;
    ViewFlipper viewFlipper,optionsViewFlipper;
    Animation in,out;
    RecyclerView mainAudioList,audioGridView;
    Button playPauseButton2;
    int i=0;
    GestureDetector gestureDetector;

    boolean flag,search=false;

    public static String getContext() {
        return MainList.getContext();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        titleTextView2=findViewById(R.id.titleText2);
        coverImage2=findViewById(R.id.coverImage2);
        playPauseButton2=findViewById(R.id.playPauseButton2);
        mainAudioList=findViewById(R.id.mainList);
        fastScroller=findViewById(R.id.fastscroll);
        audioGridView=findViewById(R.id.mainGridView);
        viewFlipper=findViewById(R.id.mainViewFlipper);
        optionsViewFlipper=findViewById(R.id.optionsViewFlipper);
        in= AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        out= AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);
        optionsViewFlipper.setInAnimation(in);
        optionsViewFlipper.setOutAnimation(out);

        setBroadcastRecivers();

        if(getIntent().getSerializableExtra("audioList")!=null) {
            audioInfoArrayList = (ArrayList<AudioInfo>) getIntent().getSerializableExtra("audioList");
            Collections.sort(audioInfoArrayList,new NameComparator());
            if(CoverPlay.currentSong==null)
                setSubView(audioInfoArrayList.get(0),false);
            else
                setSubView(CoverPlay.currentSong,flag);
        }

        mainAudioList.setLayoutManager(new LinearLayoutManager(this));
        customListAdapter=new CustomListAdapter(this,audioInfoArrayList);
        mainAudioList.setAdapter(customListAdapter);

        audioGridView.setLayoutManager(new GridLayoutManager(this,2));
        customGridAdapter=new CustomGridAdapter(this,audioInfoArrayList);
        audioGridView.setAdapter(customGridAdapter);
        fastScroller.setRecyclerView(audioGridView);
        fastScroller.setBubbleColor(Color.LTGRAY);
        fastScroller.setHandleColor(Color.BLACK);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);

        SearchView searchView =(SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
        //search=true;

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        ArrayList<AudioInfo> filteredList=new ArrayList<>(1);
        for(int i=0;i<audioInfoArrayList.size();i++)
            if((audioInfoArrayList.get(i).getAudioName()).contains(s))
                filteredList.add(audioInfoArrayList.get(i));

        if(mainAudioList.getVisibility()==View.VISIBLE)
            customListAdapter.filterList(filteredList);
        else if(audioGridView.getVisibility()==View.VISIBLE)
            customGridAdapter.filterList(filteredList);
        search=true;

        return true;
    }

    @Override
    public void onBackPressed() {
        if(search) {Log.i("logText","in if onBackPressed");
            customListAdapter.filterList(audioInfoArrayList);
            search=false;
        }
        else {Log.i("logText","in else onBackPressed");
            super.onBackPressed();
        }
    }

    public void setBroadcastRecivers() {
        BroadcastReceiver broadcastReceiver1=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getStringExtra("url")!=null) {
                    String url=intent.getStringExtra("url");
                    for(int i=0;i<audioInfoArrayList.size();i++)
                        if((audioInfoArrayList.get(i).getUrl()).compareTo(url)==0) {
                            setSubView(CoverPlay.currentSong, intent.getBooleanExtra("flag", true));
                            break;
                        }
                }
            }
        };
        BroadcastReceiver broadcastReceiver2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                optionsViewFlipper.showNext();
            }
        };
        BroadcastReceiver broadcastReceiver3=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                optionsViewFlipper.showNext();
            }
        };
        BroadcastReceiver broadcastReceiver4=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                for(int i=0;i<audioInfoArrayList.size();i++)
                    if(!(new File(audioInfoArrayList.get(i).getUrl())).exists())
                        audioInfoArrayList.remove(i);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver1,new IntentFilter("currentAudio"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver2,new IntentFilter("selectedItems"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver3,new IntentFilter("actionCompleted"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver4,new IntentFilter("songs deleted"));
    }

    public void setSubView(AudioInfo audioObj,boolean flag) {
        titleTextView2.setText(audioObj.getAudioName());
        if(CoverPlay.flag==false)
            playPauseButton2.setText("Play");
        else
            playPauseButton2.setText("Pause");
        BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
        bitmapOptions.inSampleSize=16;
        byte[] b=audioObj.getCoverImageByteArray(this);
        if(b!=null)
            coverImage2.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length,bitmapOptions));
        this.flag=flag;
    }

    public void onClickSubView(View v) {
        if(CoverPlay.currentSong==null) {
            if (v.getId() == R.id.playPauseButton2) {
                Intent intent = new Intent(this, CoverPlay.class);
                intent.putExtra("Play", audioInfoArrayList);
                intent.putExtra("Position", 0);
                startActivityForResult(intent, requestCode);

                Intent i = new Intent("playPauseBroadcast");
                if (flag == false) {
                    flag = true;
                    i.putExtra("play", true);
                    playPauseButton2.setText("Pause");
                } else {
                    flag = false;
                    i.putExtra("play", false);
                    playPauseButton2.setText("Play");
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            }
            else if (v.getId() == R.id.titleText2 || v.getId() == R.id.coverImage2) {
                Intent i = new Intent(this, CoverPlay.class);
                i.putExtra("Play", audioInfoArrayList);
                i.putExtra("Position", 0);
                startActivityForResult(i, requestCode);
            }
            else if (v.getId() == R.id.button) {
                viewFlipper.showNext();
            }
        }
        else {
            if (v.getId() == R.id.playPauseButton2) {
                Intent i = new Intent("playPauseBroadcast");
                if (CoverPlay.flag == false) {
                    flag = true;
                    i.putExtra("play", true);
                    playPauseButton2.setText("Pause");
                } else {
                    flag = false;
                    i.putExtra("play", false);
                    playPauseButton2.setText("Play");
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            } else if (v.getId() == R.id.titleText2 || v.getId() == R.id.coverImage2) {
                try {
                    Intent i = new Intent(mainList, CoverPlay.class);
                    startActivityForResult(i, requestCode);
                } catch (Exception e) {
                    Log.i("logText", " error " + e);
                }
            } else if (v.getId() == R.id.button) {
                viewFlipper.showNext();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public class NameComparator implements Comparator<AudioInfo> {
        @Override
        public int compare(AudioInfo audioInfo, AudioInfo t1) {
            return audioInfo.getAudioName().compareTo(t1.getAudioName());
        }
    }

    public class SizeComparator implements Comparator<AudioInfo> {
        @Override
        public int compare(AudioInfo audioInfo, AudioInfo t1) {
            return Float.compare(audioInfo.getSize(),t1.getSize());
            //return audioInfo.getSize().compareTo(t1.getSize());
        }
    }

}