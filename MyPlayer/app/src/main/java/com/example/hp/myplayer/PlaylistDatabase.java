package com.example.hp.myplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by hp on 05-Feb-18.
 */

public class PlaylistDatabase extends SQLiteOpenHelper {

    static int DATABASE_VERSION=1;
    static String DATABASE_NAME="PlaylistDatabase";
    static String PLAYLIST_TABLE=new String();
    static String COL_URL="URL";


    public PlaylistDatabase(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query="CREATE TABLE IF NOT EXISTS "+PLAYLIST_TABLE+"("+COL_URL+" TEXT);";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+PLAYLIST_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void addTable(String table){
        PLAYLIST_TABLE=table;
        onCreate(getWritableDatabase());
    }

    public void deleteTable(String table){
        String query="DROP TABLE IF EXISTS "+table+";";
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL(query);
    }

    public void addItem(String table,String url){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COL_URL,url);
        db.insert(table,null,values);
    }

    public void deleteItem(String table,String url){}

    public ArrayList<String> getItems(String table){
        ArrayList<String> urlList=new ArrayList<>(1);

        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT "+COL_URL+" FROM "+table,null);

        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                urlList.add(c.getString(0));
                c.moveToNext();
                Log.i("logText","list item "+urlList.get(0));
            }
        }

        return urlList;
    }

    public ArrayList<String> getPlaylistName(){
        SQLiteDatabase db=getReadableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            ArrayList<String> playlistList=new ArrayList<String>(1);
            while ( !c.isAfterLast() ) {
                playlistList.add(c.getString(0));
                Log.i("logText","table : "+c.getString(0));
                c.moveToNext();
            }
            return playlistList;
        }

        return null;
    }

    public ArrayList<Long> getPlaylistItemCount(){
        ArrayList<String> playlistList=getPlaylistName();

        SQLiteDatabase db=getReadableDatabase();
        ArrayList<Long> playlistItemCount=new ArrayList<>(1);
        int position=0;

        while(position!=playlistList.size()){
            playlistItemCount.add(DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM "+playlistList.get(position), null));
            position++;
        }

        return playlistItemCount;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } /*catch(Exception sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }*/ catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}