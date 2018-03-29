package com.example.hp.myplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hp on 04-Jan-18.
 */

public class CustomSecondaryListAdapter extends RecyclerView.Adapter<CustomSecondaryListAdapter.SecondaryListViewHolder> {

    ArrayList<AudioInfo> secondaryAdioInfoArrayList;
    Context context;

    public CustomSecondaryListAdapter(Context context, ArrayList<AudioInfo> secondaryAdioInfoArrayList) {
        try {
            this.context=context;
            this.secondaryAdioInfoArrayList = secondaryAdioInfoArrayList;
        } catch (Exception e) {
            Log.i("logText"," in SecondaryListLayoutAdapter const error"+e);
        }
    }

    @Override
    public SecondaryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.custom_secondary_list_layout,parent,false);
        return new SecondaryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SecondaryListViewHolder holder, final int position)
    {
        AudioInfo audioInfo=secondaryAdioInfoArrayList.get(position);
        holder.secondaryListTitleName.setText(audioInfo.getAudioName());
        BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
        bitmapOptions.inSampleSize=2;
        byte[] b=audioInfo.getCoverImageByteArray(context);
        if(b!=null)
            holder.secondaryListCoverImage.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions));

        holder.secondaryListRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent("changeSong");
                i.putExtra("position",position);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return secondaryAdioInfoArrayList.size();
    }

    public class SecondaryListViewHolder extends RecyclerView.ViewHolder{
        ImageView secondaryListCoverImage;
        TextView secondaryListTitleName;
        RelativeLayout secondaryListRelativeLayout;
        public SecondaryListViewHolder(View itemView) {
            super(itemView);
            try {
                secondaryListCoverImage=(ImageView)itemView.findViewById(R.id.secondaryListCoverImage);
                secondaryListTitleName=(TextView)itemView.findViewById(R.id.secondaryListTitleName);
                secondaryListRelativeLayout=(RelativeLayout) itemView.findViewById(R.id.secondaryListRelativeLayout);
            } catch (Exception e) {
                Log.i("logText"," in SecondaryListViewHolder const error"+e);
            }
        }
    }

}