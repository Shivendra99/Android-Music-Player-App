package com.example.hp.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class CoverPlay extends AppCompatActivity {

    static ArrayList<AudioInfo> audioInfoArrayList;
    static AudioInfo currentSong;
    static int position,mediaDuration,currentPosition;
    static MediaPlayer nowPlaying=new MediaPlayer();
    static SeekBar audioSeekBar;
    static TextView titleText,durationTime,currentTime;
    static ImageView coverImage;
    static Button playPauseButton;
    Thread t;
    static Boolean flag=false;
    static boolean onCompleteFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_play);

        titleText=(TextView)findViewById(R.id.titleText);
        coverImage=(ImageView)findViewById(R.id.coverImage);
        playPauseButton=(Button)findViewById(R.id.playPauseButton);
        durationTime=(TextView)findViewById(R.id.durationTimeTextView);
        currentTime=(TextView)findViewById(R.id.currentTimeTextView);
        audioSeekBar=(SeekBar)findViewById(R.id.audioSeekBar);

        if(getIntent().getSerializableExtra("Play")!=null)
        {
            audioInfoArrayList = (ArrayList<AudioInfo>) getIntent().getSerializableExtra("Play");
            position = (int) getIntent().getIntExtra("Position", 0);
            prepareToPlay();
        }

        setBroadcastReceiver();
        prepareView();

        RecyclerView secondaryList=(RecyclerView)findViewById(R.id.secondaryList);
        secondaryList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        secondaryList.setAdapter(new CustomSecondaryListAdapter(this,audioInfoArrayList));

    }

    public void prepareToPlay() {Log.i("logText1","size of list in cp "+audioInfoArrayList.size());
        if(nowPlaying!=null)
            nowPlaying.reset();
        try {
            nowPlaying.setDataSource(audioInfoArrayList.get(position).getUrl());
            nowPlaying.prepare();
            nowPlaying.start();
            currentSong=audioInfoArrayList.get(position);
            flag=true;
            onCompleteFlag=false;
            nowPlaying.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                   @Override
                                                   public void onCompletion(MediaPlayer mp) {
                                                       flag=false;
                                                       position++;
                                                       onCompleteFlag=true;
                                                       makeBroadcast(position,flag);
                                                       prepareToPlay();
                                                       prepareView();
                                                   }
                                               }
            );
            playPauseButton.setText("Pause");
        } catch (Exception e) {
            Toast.makeText(this,"Error in playing this song "+e,Toast.LENGTH_SHORT).show();
            Log.i("logText : ","In prepareToPlay "+e);
        }

    }


    public void prepareView() {
        titleText.setText(audioInfoArrayList.get(position).getAudioName());
        mediaDuration=nowPlaying.getDuration()/1000;
        if((mediaDuration%60)>=10)
            durationTime.setText((int)(mediaDuration/60)+":"+(int)(mediaDuration%60));
        else
            durationTime.setText((int)(mediaDuration/60)+":0"+(int)(mediaDuration%60));
        byte[] b=audioInfoArrayList.get(position).getCoverImageByteArray(this);
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = 1;
        if(b!=null) {
            coverImage.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions));
            RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.coverPlayRelativeLayout);
            Drawable backgroundDrawable= new BitmapDrawable(this.getResources(),BlurBuilder.blur(this,BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions)));
            backgroundDrawable.setAlpha(200);
            relativeLayout.setBackground(backgroundDrawable);
        }

        if(nowPlaying.isPlaying())
            playPauseButton.setText("Pause");
        else
            playPauseButton.setText("Play");

        prepareSeekBar();
    }


    public void prepareSeekBar() {
        audioSeekBar.setMax(mediaDuration*1000);
        audioSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        nowPlaying.seekTo(audioSeekBar.getProgress());
                        if(flag)
                            nowPlaying.start();
                    }
                }
        );

        Runnable r=new Runnable(){
            public void run()
            {
                audioSeekBar.setProgress(0);
                h.sendEmptyMessage(0);
                while (!onCompleteFlag) {
                    if(flag==false)
                        continue;
                    try {  h.sendEmptyMessage(0);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.i("logText","error in seekbar thread "+e);
                    }
                }
            }
        };
        t=new Thread(r);
        t.start();
    }

    Handler h=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            currentPosition=nowPlaying.getCurrentPosition()/1000;
            audioSeekBar.setProgress(currentPosition*1000);
            if(currentPosition%60>=10)
                currentTime.setText(currentPosition/60+":"+currentPosition%60);
            else
                currentTime.setText(currentPosition/60+":0"+currentPosition%60);
        }
    };


    @Override
    public void onBackPressed() {
        makeBroadcast(position,flag);
        super.onBackPressed();

    }

    public void makeBroadcast(int position,boolean flag) {
        Intent i=new Intent("currentAudio");
        i.putExtra("url",audioInfoArrayList.get(position).getUrl());
        if(onCompleteFlag==false)
            i.putExtra("flag",flag);
        else
            i.putExtra("flag",true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    public void setBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver1=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("play", true) == true)
                {
                    nowPlaying.seekTo(nowPlaying.getCurrentPosition());
                    nowPlaying.start();
                    flag = true;
                }
                else if (intent.getBooleanExtra("play", false) == false)
                {
                    nowPlaying.pause();
                    flag = false;
                }
            }
        };
        BroadcastReceiver broadcastReceiver2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getIntExtra("position",-1)!=-1)
                {
                    position=intent.getIntExtra("position",-1);
                    prepareToPlay();
                    prepareView();
                }
            }
        };
        BroadcastReceiver broadcastReceiver3=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                for(int i=0;i<audioInfoArrayList.size();i++)
                    if(!(new File(audioInfoArrayList.get(i).getUrl())).exists())
                        audioInfoArrayList.remove(i);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver1,new IntentFilter("playPauseBroadcast"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver2,new IntentFilter("changeSong"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver3,new IntentFilter("songs deleted"));
    }

    public void onNowPlayingButtonsClicked(View v) { Log.i("logText3","in btn view");
        if(v.getId()==R.id.playPauseButton)
        {
            if(!nowPlaying.isPlaying())
            {Log.i("logText3","in btn view if");
                playPauseButton.setText("Pause");
                nowPlaying.seekTo(nowPlaying.getCurrentPosition());
                nowPlaying.start();
                flag=true;
            }
            else
            {Log.i("logText3","in btn view if");
                playPauseButton.setText("Play");
                nowPlaying.pause();
                flag=false;
            }
        }
        else if(v.getId()==R.id.nextButton)
        {
            nowPlaying.stop();
            position++;
            prepareToPlay();
            prepareView();
        }
        else if(v.getId()==R.id.prevButton)
        {
            nowPlaying.stop();
            position--;
            prepareToPlay();
            prepareView();
        }
    }

}
