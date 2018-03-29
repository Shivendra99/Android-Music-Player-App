package com.example.hp.myplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.io.File;
import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static android.support.v4.content.ContextCompat.startActivity;
import static com.example.hp.myplayer.CoverPlay.position;

/**
 * Created by hp on 04-Jan-18.
 */

public class CustomGridAdapter extends RecyclerView.Adapter<CustomGridAdapter.CustomAudioGridViewHolder> implements SectionTitleProvider {

    private ArrayList<AudioInfo> audioInfo;
    Context context;
    RecyclerView recyclerView;
    Intent playlistIntent;

    boolean multipleSelection,action;
    ArrayList<Integer> selectedItems;
    String[] optionsListArray={"Favourite","Add To..","Play Next","Set as ringtone","Song Info","Delete"};
    int requestCode=123;

    public CustomGridAdapter(Context context,ArrayList<AudioInfo> audioInfo) {
        this.context=context;
        this.audioInfo=audioInfo;
        multipleSelection=false;
        action=false;
    }

    @Override
    public CustomAudioGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.custom_grid_layout,parent,false);
        return new CustomAudioGridViewHolder(view);
    }

    @Override
    public int getItemCount()
    {
        return audioInfo.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView=recyclerView;
    }

    @Override
    public void onBindViewHolder(final CustomAudioGridViewHolder holder, final int position) {
        final AudioInfo audioInfoObj=audioInfo.get(position);
        holder.titleText3.setText(audioInfoObj.getAudioName());
        BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
        bitmapOptions.inSampleSize=2;
        byte[] b=audioInfoObj.getCoverImageByteArray(context);
        if(b!=null)
            holder.coverImageGV.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, bitmapOptions));
        else
            holder.coverImageGV.setImageResource(R.drawable.ic_launcher_background);

        holder.gridViewRelativeLayout.setBackgroundColor(Color.WHITE);

        if(selectedItems!=null&&selectedItems.contains(position))
            holder.gridViewRelativeLayout.setBackgroundColor(Color.GRAY);
    }


    public void performAction(int choice) {

        if(choice==0){}

        else if(choice==1){ArrayList<String> selectedSongs=new ArrayList<String>(1);
            final String playlist;

            for(int pos:selectedItems)
                selectedSongs.add(audioInfo.get(pos).getUrl());

            playlistIntent=new Intent("songsForPlaylist");
            playlistIntent.putExtra("selectedSongs",selectedSongs);

            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View playlistPopUpWindowView=inflater.inflate(R.layout.heading_popup_window,null);

            int height=context.getResources().getDisplayMetrics().heightPixels/2;
            int width=context.getResources().getDisplayMetrics().widthPixels;

            final PopupWindow playlistPopUpWindow=new PopupWindow(playlistPopUpWindowView,width-40,height,true);
            playlistPopUpWindow.showAtLocation(playlistPopUpWindowView,Gravity.BOTTOM,0,0);

            TextView headingText=playlistPopUpWindowView.findViewById(R.id.heading);
            ListView playlistListView=playlistPopUpWindowView.findViewById(R.id.playlistListView);

            PlaylistDatabase playlistDatabase=WelcomeActivity.playlistDatabase;
            final ArrayList<String> playlistName=playlistDatabase.getPlaylistName();
            playlistName.add("Create New Playlist...");

            final int playlistListSize=playlistName.size();

            headingText.setText("Add To..");

            ListAdapter listAdapter=new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,playlistName);
            playlistListView.setAdapter(listAdapter);

            playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    playlistPopUpWindow.dismiss();
                    if(i==playlistListSize-1){
                        LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View editTextPopUpWindowView=layoutInflater.inflate(R.layout.edit_text_popup_window,null);

                        int height=context.getResources().getDisplayMetrics().heightPixels/2;
                        int width=context.getResources().getDisplayMetrics().widthPixels;

                        final PopupWindow editTextPopupWindow=new PopupWindow(editTextPopUpWindowView,width-40,height,true);
                        editTextPopupWindow.showAtLocation(editTextPopUpWindowView, Gravity.BOTTOM,0,0);

                        final EditText newPlaylistEditText=editTextPopUpWindowView.findViewById(R.id.newPlaylistEditText);
                        Button cancelButton=editTextPopUpWindowView.findViewById(R.id.cancelButton);
                        Button okButton=editTextPopUpWindowView.findViewById(R.id.okButton);

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                editTextPopupWindow.dismiss();
                            }
                        });
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent playlistIntentReference=CustomGridAdapter.this.playlistIntent;
                                playlistIntentReference.putExtra("playlistName",""+newPlaylistEditText.getText());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(playlistIntentReference);
                                editTextPopupWindow.dismiss();
                            }
                        });

                    }
                    else{
                        playlistIntent.putExtra("playlistName",playlistName.get(i));
                        LocalBroadcastManager.getInstance(context).sendBroadcast(playlistIntent);
                    }
                }
            });}

        else if(choice==2){
            if(askWritePermission()){
                File file;
                for(int i=0;i<selectedItems.size();i++){
                    file=new File(audioInfo.get(selectedItems.get(i)).getUrl());
                    if(file.delete()) {
                        try {
                            String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+file;
                            Uri rootUri = MediaStore.Audio.Media.getContentUriForPath( mFilePath );
                            context.getContentResolver().delete( rootUri, MediaStore.MediaColumns.DATA + "=?", new String[]{ mFilePath } );
                        } catch (Exception e) {
                            Log.i("logText","error in deletion "+e);
                        }
                    }
                }
                for(int i=0;i<audioInfo.size();i++) {
                    if(!(new File(audioInfo.get(i).getUrl())).exists()) {
                        Toast.makeText(context,audioInfo.get(i).getAudioName()+" deleted",Toast.LENGTH_SHORT).show();
                        audioInfo.remove(i);
                        i--;
                    }
                }
                Intent intent=new Intent("songs deleted");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
            else {
                Toast.makeText(context,"Write permission on storage not granted, song(s) can't be deleted",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        else if(choice==3){
            selectedItems=new ArrayList<Integer>(1);
            for(int i=0;i<audioInfo.size();i++)
                selectedItems.add(i);
            notifyDataSetChanged();
        }

        resetValues();
        Intent i=new Intent("actionCompleted");
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        notifyDataSetChanged();
    }

    public boolean askWritePermission() {
        if(checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            Log.i("logText","permission not granted, asking");
            ActivityCompat.requestPermissions((Activity)context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
            if(checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                Log.i("logText","permission granted finally");
                return true;
            }
            else {
                Log.i("logText", "permission not granted finally");
                return false;
            }
        }
        else {
            Log.i("logText","permission already granted");
            return true;
        }
    }

    public void resetValues() {
        selectedItems=null;
        action=false;
        multipleSelection=false;
    }

    @Override
    public String getSectionTitle(int position) {
        return (audioInfo.get(position).getAudioName()).substring(0, 1);
    }

    public class CustomAudioGridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener
    {
        TextView titleText3;
        ImageView coverImageGV;
        Button optionsButtonGV;
        RelativeLayout gridViewRelativeLayout;

        public CustomAudioGridViewHolder(View itemView)
        {
            super(itemView);
            titleText3=(TextView)itemView.findViewById(R.id.titleNameGV);
            coverImageGV=(ImageView)itemView.findViewById(R.id.coverImageGV);
            optionsButtonGV=(Button)itemView.findViewById(R.id.optionsButtonGV);
            gridViewRelativeLayout=(RelativeLayout)itemView.findViewById(R.id.gridViewRelativeLayout);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);

            optionsButtonGV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position=getAdapterPosition();
                    if(!multipleSelection) {
                        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View optionsPopUpWindowView=inflater.inflate(R.layout.popup_window_layout,null);

                        int height=context.getResources().getDisplayMetrics().heightPixels/2;
                        int width=context.getResources().getDisplayMetrics().widthPixels;

                        final PopupWindow optionsPopUpWindow=new PopupWindow(optionsPopUpWindowView,width-40,height,true);

                        optionsPopUpWindow.showAtLocation(optionsPopUpWindowView,Gravity.BOTTOM,0,0);

                        ListView optionsList=(ListView)optionsPopUpWindowView.findViewById(R.id.optionsList);
                        ListAdapter listAdapter=new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,optionsListArray);
                        optionsList.setAdapter(listAdapter);

                        optionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                //Toast.makeText(context,"Position "+i+" clicked",Toast.LENGTH_SHORT).show();
                                if(i==4){
                                    Intent intent=new Intent(context,SongInfoEdit.class);
                                    intent.putExtra("songInfo",audioInfo.get(position));
                                    context.startActivity(intent);
                                }
                                else if(i==5) {
                                    selectedItems=new ArrayList<Integer>(1);
                                    selectedItems.add(position);
                                    performAction(2);
                                    optionsPopUpWindow.dismiss();
                                }
                            }
                        });

                        optionsPopUpWindowView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                optionsPopUpWindow.dismiss();
                                return true;
                            }
                        });

                        Toast.makeText(context, " Position " + position, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            if(!multipleSelection) {
                Intent i = new Intent(context, CoverPlay.class);
                i.putExtra("Play", audioInfo);
                i.putExtra("Position", position);
                context.startActivity(i);
            }
            else {
                if(selectedItems.contains(position)) {
                    selectedItems.remove((Integer)position);
                }
                else {
                    selectedItems.add(position);
                }
                notifyItemChanged(position);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int position=getAdapterPosition();
            if(!multipleSelection) {
                multipleSelection=true;
                selectedItems=new ArrayList<Integer>(1);

                if(selectedItems.contains(position)) {
                    selectedItems.remove((Integer)position);
                }
                else {
                    selectedItems.add(position);
                }
                notifyItemChanged(position);

                Intent i=new Intent("selectedItems");
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);

                LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View optionsPopUpWindowView=inflater.inflate(R.layout.popup_window2_layout,null);

                int height=150;
                int width=context.getResources().getDisplayMetrics().widthPixels;

                final PopupWindow optionsPopUpWindow=new PopupWindow(optionsPopUpWindowView,width-40,height,false);

                optionsPopUpWindow.showAtLocation(optionsPopUpWindowView,Gravity.BOTTOM,0,0);

                Button favButton=(Button)optionsPopUpWindowView.findViewById(R.id.favButton);
                favButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performAction(0);
                        optionsPopUpWindow.dismiss();
                    }
                });

                Button addToButton=(Button)optionsPopUpWindowView.findViewById(R.id.addToButton);
                addToButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performAction(1);
                        optionsPopUpWindow.dismiss();
                    }
                });

                Button delButton=(Button)optionsPopUpWindowView.findViewById(R.id.delButton);
                delButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performAction(2);
                        optionsPopUpWindow.dismiss();
                    }
                });

                Button selectAllButton=(Button)optionsPopUpWindowView.findViewById(R.id.selectAllButton);
                selectAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performAction(3);
                        optionsPopUpWindow.dismiss();
                    }
                });

            }
            return true;
        }
    }

    public void filterList(ArrayList<AudioInfo> filterdList){
        audioInfo=filterdList;
        notifyDataSetChanged();
    }
}