package com.example.hp.myplayer;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SongInfoEdit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info_edit);

        AudioInfo audioInfo = (AudioInfo)getIntent().getSerializableExtra("songInfo");

        if (audioInfo!=null) {
            EditText titleEditText=(EditText) findViewById(R.id.titleEditText);
            EditText artistEditText=(EditText) findViewById(R.id.artistEditText);
            EditText albumEditText=(EditText) findViewById(R.id.albumEditText);
            TextView sizeTextView=(TextView)findViewById(R.id.sizeTextView);
            TextView pathTextView=(TextView)findViewById(R.id.pathTextView);
            ImageView coverArtImageView=(ImageView)findViewById(R.id.coverArtImageView);
            RelativeLayout songInfoEditRelativrLayout=(RelativeLayout)findViewById(R.id.songInfoEditRelativeLayout);

            titleEditText.setText(audioInfo.getAudioName());
            artistEditText.setText(audioInfo.getArtist());
            albumEditText.setText(audioInfo.getAlbum());
            pathTextView.setText(audioInfo.getUrl());
            sizeTextView.setText(audioInfo.getSize()+" mB");

            BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
            bitmapOptions.inSampleSize=1;
            byte[] b=audioInfo.getCoverImageByteArray(this);
            if(b!=null) {
                coverArtImageView.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions));
                Drawable backgroundDrawable = new BitmapDrawable(this.getResources(), BlurBuilder.blur(this, BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions)));
                backgroundDrawable.setAlpha(200);
                songInfoEditRelativrLayout.setBackground(backgroundDrawable);
            }
        }

    }
}
