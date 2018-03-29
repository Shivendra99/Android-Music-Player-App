package com.example.hp.myplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hp on 17-Feb-18.
 */

public class CustomListViewAdapter extends ArrayAdapter<String> {

    ArrayList<String> playlistList;
    ArrayList<Long> songCount;

    public CustomListViewAdapter(Context context, ArrayList<String> playlistList, ArrayList<Long> songCount ) {
        super(context,R.layout.custom_list_layout,playlistList);
        this.playlistList=playlistList;
        this.songCount=songCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater myInflator=LayoutInflater.from(getContext());
        View customView=myInflator.inflate(R.layout.custom_list_layout, parent, false);

        TextView playlistNameTextView=customView.findViewById(R.id.titleText3);
        TextView songCountTextView=customView.findViewById(R.id.infoText);

        playlistNameTextView.setText(playlistList.get(position));
        songCountTextView.setText(songCount.get(position)+" songs");

        return customView;
    }
}
