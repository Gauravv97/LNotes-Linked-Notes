package com.links.gaurav.lnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

import data.entry;


public class Dbhandler extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="indx.db";
    public static final String TABLE_NAME = "entry";

    public static final String ID="_id";

    public static final String COLUMN1_NAME_TITLE_TABLE1 = "Total";
    public static final String COLUMN2_NAME_TITLE_TABLE1 = "Name";
    public static final String COLUMN3_NAME_TITLE_TABLE1 = "DateTime";

    public static final String TABLE2_NAME="paths_of_images";
    public static final String COLUMN1_NAME_TITLE_TABLE2 = "Serial";
    public static final String COLUMN2_NAME_TITLE_TABLE2 = "Image";
    public static final String COLUMN3_NAME_TITLE_TABLE2 = "Scanned_Image";
    public static final String COLUMN4_NAME_TITLE_TABLE2 = "HasMarker";
    public static final String COLUMN5_NAME_TITLE_TABLE2 = "Marker";
    public static final String COLUMN6_NAME_TITLE_TABLE2 = "KeyPoints";
    public static final String COLUMN7_NAME_TITLE_TABLE2 = "KPRows";
    public static final String COLUMN8_NAME_TITLE_TABLE2 = "KPCols";



    public Dbhandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String Query= "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER ," +
                COLUMN1_NAME_TITLE_TABLE1 + " INTEGER," +
                COLUMN2_NAME_TITLE_TABLE1 + " TEXT,"+
                COLUMN3_NAME_TITLE_TABLE1 + " TEXT);";
        db.execSQL(Query);
        Query= "CREATE TABLE " + TABLE2_NAME + " (" +
                ID + " INTEGER ," +
                COLUMN1_NAME_TITLE_TABLE2 + " INTEGER,"+
                COLUMN2_NAME_TITLE_TABLE2 + " TEXT,"+
                COLUMN3_NAME_TITLE_TABLE2 + " TEXT,"+
                COLUMN4_NAME_TITLE_TABLE2 + " TEXT,"+
                COLUMN5_NAME_TITLE_TABLE2 + " INTEGER,"+
                COLUMN6_NAME_TITLE_TABLE2 + " TEXT,"+
                COLUMN7_NAME_TITLE_TABLE2 + " INTEGER,"+
                COLUMN8_NAME_TITLE_TABLE2 + " INTEGER);";
        db.execSQL(Query);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public void addRow(entry e)
    {
        int id=rCount();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues x=new ContentValues();
        x.put(ID,id);
        x.put(COLUMN1_NAME_TITLE_TABLE1,e.get_im().length);
        x.put(COLUMN2_NAME_TITLE_TABLE1,e.getName());
        x.put(COLUMN3_NAME_TITLE_TABLE1,e.getDateTime());
        db.insert(TABLE_NAME,null,x);


        for(int i=0;i<e.get_im().length;i++)
        {   x.clear();
            x.put(ID,id);
            x.put(COLUMN1_NAME_TITLE_TABLE2,i);
            x.put(COLUMN2_NAME_TITLE_TABLE2,e.get_im()[i]);
            x.put(COLUMN3_NAME_TITLE_TABLE2,e.get_im()[i]);
            x.put(COLUMN4_NAME_TITLE_TABLE2,0);
            db.insert(TABLE2_NAME,null,x);
        }
    }
    public entry getresult(int x){

        entry e=new entry();
        SQLiteDatabase db = getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE "+ID+"="+x+";";
        Cursor c=db.rawQuery(query,null);
        c.moveToFirst();
        if(!c.isAfterLast())
        {
            if(c.getString(c.getColumnIndex(ID))!=null)
                e.set_id(c.getInt(c.getColumnIndex(ID)));
            if(c.getString(c.getColumnIndex(COLUMN2_NAME_TITLE_TABLE1))!=null)
                e.setName(c.getString(c.getColumnIndex(COLUMN2_NAME_TITLE_TABLE1)));
            if(c.getString(c.getColumnIndex(COLUMN3_NAME_TITLE_TABLE1))!=null)
                e.setDateTime(c.getString(c.getColumnIndex(COLUMN3_NAME_TITLE_TABLE1)));
        }
        query="SELECT * FROM "+TABLE2_NAME+" WHERE "+ID+"="+x+" ORDER BY "+COLUMN1_NAME_TITLE_TABLE2+";";
        c=db.rawQuery(query,null);
        c.moveToFirst();
        String Images[]=new String[c.getCount()],Scanned[]=new String[c.getCount()],marker[]=new String[c.getCount()],KeyPoints[]=new String[c.getCount()];
        int[] Serial=new int[c.getCount()],KPRows=new int[c.getCount()],KPCols=new int[c.getCount()];
        boolean[] hasMarker=new boolean[c.getCount()];
        int i=0;
        while (!c.isAfterLast())
        {
            Serial[i]=c.getInt(c.getColumnIndex(COLUMN1_NAME_TITLE_TABLE2));
            Images[i]=c.getString(c.getColumnIndex(COLUMN2_NAME_TITLE_TABLE2));
            Scanned[i]=c.getString(c.getColumnIndex(COLUMN3_NAME_TITLE_TABLE2));
            hasMarker[i]=c.getInt(c.getColumnIndex(COLUMN4_NAME_TITLE_TABLE2))==1;
            if(hasMarker[i]){
                marker[i]=c.getString(c.getColumnIndex(COLUMN5_NAME_TITLE_TABLE2));
                KeyPoints[i]=c.getString(c.getColumnIndex(COLUMN6_NAME_TITLE_TABLE2));
                KPRows[i]=c.getInt(c.getColumnIndex(COLUMN7_NAME_TITLE_TABLE2));
                KPCols[i]=c.getInt(c.getColumnIndex(COLUMN8_NAME_TITLE_TABLE2));
            }
            i++;
            c.moveToNext();
        }
        e.set_im(Images);
        e.set_total(Images.length);
        e.set_sc(Scanned);
        e.set_serial(Serial);
        e.setHasMarker(hasMarker);
        e.set_mr(marker);
        e.set_KeyPoints(KeyPoints);
        e.set_KPRows(KPRows);
        e.set_KPCols(KPCols);
        c.close();
        return e;
    }
    public int rCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db =getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
    public int set_scannedImage(String image,String scanned_image){
        SQLiteDatabase db = getWritableDatabase();
        String Query="SELECT * FROM "+TABLE2_NAME+" WHERE "+COLUMN2_NAME_TITLE_TABLE2+"='"+image+"'";
        Cursor c=db.rawQuery(Query,null);
        c.moveToFirst();
        if(!c.isAfterLast())
        {
            String path=c.getString(c.getColumnIndex(COLUMN3_NAME_TITLE_TABLE2));
            if(!path.equals(image))
                new File(path).delete();
            Query = "UPDATE " + TABLE2_NAME + " SET " + COLUMN3_NAME_TITLE_TABLE2 + "='" + scanned_image + "' WHERE " + COLUMN2_NAME_TITLE_TABLE2 + "='" + image + "';";
            db.execSQL(Query);
            return c.getInt(c.getColumnIndex(ID));
        }
        return -1;
    }
    public void renameEntry(int id,String newName){
        SQLiteDatabase db = getWritableDatabase();
        String Query = "UPDATE "+TABLE_NAME+" SET "+COLUMN2_NAME_TITLE_TABLE1+"='"+newName+"' WHERE "+ID+"="+id+";";
        db.execSQL(Query);
    }
    public void setMarker(String image,String marker,String _keyPoints,int KPRows,int KPCols){
        SQLiteDatabase db = getWritableDatabase();
        String Query="SELECT * FROM "+TABLE2_NAME+" WHERE "+COLUMN2_NAME_TITLE_TABLE2+"='"+image+"'";
        Cursor c=db.rawQuery(Query,null);
        c.moveToFirst();
        if(!c.isAfterLast()) {
            if(c.getInt(c.getColumnIndex(COLUMN4_NAME_TITLE_TABLE2))==1){
                String path=c.getString(c.getColumnIndex(COLUMN5_NAME_TITLE_TABLE2));
                new File(path).delete();
                path=c.getString(c.getColumnIndex(COLUMN6_NAME_TITLE_TABLE2));
                new File(path).delete();
            }
            Query = "UPDATE " + TABLE2_NAME + " SET " + COLUMN4_NAME_TITLE_TABLE2 + "=1 WHERE " + COLUMN2_NAME_TITLE_TABLE2 + "='" + image + "';";
            db.execSQL(Query);
            Query = "UPDATE " + TABLE2_NAME + " SET " + COLUMN5_NAME_TITLE_TABLE2 + "='" + marker + "' WHERE " + COLUMN2_NAME_TITLE_TABLE2 + "='" + image + "';";
            db.execSQL(Query);
            Query = "UPDATE "+TABLE2_NAME+" SET "+COLUMN6_NAME_TITLE_TABLE2+"='"+_keyPoints+"' WHERE "+COLUMN5_NAME_TITLE_TABLE2+"='"+marker+"';";
            db.execSQL(Query);
            Query = "UPDATE "+TABLE2_NAME+" SET "+COLUMN7_NAME_TITLE_TABLE2+"="+KPRows+" WHERE "+COLUMN5_NAME_TITLE_TABLE2+"='"+marker+"';";
            db.execSQL(Query);
            Query = "UPDATE "+TABLE2_NAME+" SET "+COLUMN8_NAME_TITLE_TABLE2+"="+KPCols+" WHERE "+COLUMN5_NAME_TITLE_TABLE2+"='"+marker+"';";
            db.execSQL(Query);
        }
    }

    public void move_entry(int from,int to){
        String Query;
        SQLiteDatabase db = getWritableDatabase();
        Query="UPDATE "+TABLE_NAME+" SET "+ID+"=-1 WHERE "+ID+"="+from+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+ID+"=-1 WHERE "+ID+"="+from+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE_NAME+" SET "+ID+"="+ID+"-1 WHERE "+ID+">"+from+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+ID+"="+ID+"-1 WHERE "+ID+">"+from+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE_NAME+" SET "+ID+"="+ID+"+1 WHERE "+ID+">"+(to-1)+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+ID+"="+ID+"+1 WHERE "+ID+">"+(to-1)+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE_NAME+" SET "+ID+"="+to+" WHERE "+ID+"="+(-1)+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+ID+"="+to+" WHERE "+ID+"="+(-1)+";";
        db.execSQL(Query);
    }

    public Boolean CheckIfFileExists(String image){
        SQLiteDatabase db = getWritableDatabase();
        String Query="SELECT * FROM "+TABLE2_NAME+" WHERE "+COLUMN2_NAME_TITLE_TABLE2+"='"+image+"'";
        Cursor c=db.rawQuery(Query,null);
        c.moveToFirst();
        if(c.getCount()>0) {
            c.close();
            return true;
        }
        else {
            c.close();
            return false;
        }
    }
    public void DeleteEntry(int i){
        String Query;
        SQLiteDatabase db = getWritableDatabase();
        Query="DELETE FROM "+TABLE_NAME+" WHERE "+ID+"="+i+";";
        db.execSQL(Query);
        Query="DELETE FROM "+TABLE2_NAME+" WHERE "+ID+"="+i+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE_NAME+" SET "+ID+"="+ID+"-1 WHERE "+ID+">"+i+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+ID+"="+ID+"-1 WHERE "+ID+">"+i+";";
        db.execSQL(Query);
    }
    public void DeleteImage(int id,int Serial){
        String Query;
        SQLiteDatabase db = getWritableDatabase();
        Query="DELETE FROM "+TABLE2_NAME+" WHERE "+ID+"="+id+" AND "+COLUMN1_NAME_TITLE_TABLE2+"="+Serial+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+COLUMN1_NAME_TITLE_TABLE2+"="+COLUMN1_NAME_TITLE_TABLE2+"-1 WHERE "+ID+"="+id+" AND "+COLUMN1_NAME_TITLE_TABLE2+">"+Serial+";";
        db.execSQL(Query);
    }
    public void move_image(int id,int from,int to){
        String Query;
        SQLiteDatabase db = getWritableDatabase();
        Query="UPDATE "+TABLE2_NAME+" SET "+COLUMN1_NAME_TITLE_TABLE2+"=-1 WHERE "+COLUMN1_NAME_TITLE_TABLE2+"="+from+" AND "+ID+"="+id+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+COLUMN1_NAME_TITLE_TABLE2+"="+COLUMN1_NAME_TITLE_TABLE2+"-1 WHERE "+COLUMN1_NAME_TITLE_TABLE2+">"+from+" AND "+ID+"="+id+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+COLUMN1_NAME_TITLE_TABLE2+"="+COLUMN1_NAME_TITLE_TABLE2+"+1 WHERE "+COLUMN1_NAME_TITLE_TABLE2+">"+(to-1)+" AND "+ID+"="+id+";";
        db.execSQL(Query);
        Query="UPDATE "+TABLE2_NAME+" SET "+COLUMN1_NAME_TITLE_TABLE2+"="+to+" WHERE "+COLUMN1_NAME_TITLE_TABLE2+"="+(-1)+" AND "+ID+"="+id+";";
        db.execSQL(Query);
    }
    public void add_Images(int id,String []image_path){
        String Query;
        SQLiteDatabase db = getWritableDatabase();
        Query="SELECT * FROM "+TABLE2_NAME+" WHERE "+ID+"="+id+";";
        Cursor c=db.rawQuery(Query,null);
        c.moveToFirst();
        int serial=c.getCount();
        ContentValues x=new ContentValues();
        for(int i=0;i<image_path.length;i++)
        {   x.clear();
            x.put(ID,id);
            x.put(COLUMN1_NAME_TITLE_TABLE2,serial+i);
            x.put(COLUMN2_NAME_TITLE_TABLE2,image_path[i]);
            x.put(COLUMN3_NAME_TITLE_TABLE2,image_path[i]);
            x.put(COLUMN4_NAME_TITLE_TABLE2,0);
            db.insert(TABLE2_NAME,null,x);
        }
    }
}
