package com.giz.bmob;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CollectionDB {

    private static CollectionDB sCollectionDB;
    private SQLiteDatabase mDatabase;

    public static CollectionDB get(Context context){
        if(sCollectionDB == null){
            sCollectionDB = new CollectionDB(context);
        }
        return sCollectionDB;
    }

    private CollectionDB(Context context){
        mDatabase = new CollectionDBHelper(context.getApplicationContext()).getWritableDatabase();
    }

    public List<StarMuseum> getStarredMuseums(){
        List<StarMuseum> museumList = new ArrayList<>();
        Cursor cursor = mDatabase.query(CollectionDBSchema.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            museumList.add(getMuseumFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return museumList;
    }

    public void addStarMuseum(Museum museum){
        mDatabase.insert(CollectionDBSchema.TABLE_NAME, null,
                getContentValuesFromMuseum(museum));
    }

    public void removeStarMuseum(String id){
        mDatabase.delete(CollectionDBSchema.TABLE_NAME, CollectionDBSchema.Cols.ID + "=?",
                new String[]{id});
    }

    public boolean hasStarred(String id){
        Cursor cursor = mDatabase.query(CollectionDBSchema.TABLE_NAME, null,
                CollectionDBSchema.Cols.ID + "=?",
                new String[]{id},
                null,
                null,
                null);
        cursor.moveToFirst();
        boolean b = cursor.getCount() > 0;
        cursor.close();
        return b;
    }

    private String formatDate(Date date){
        String format = "yyyy-MM-dd";
        return DateFormat.format(format, date).toString();
    }

    private StarMuseum getMuseumFromCursor(Cursor cursor){
        StarMuseum museum = new StarMuseum();
        museum.setMuseumId(cursor.getString(cursor.getColumnIndex(CollectionDBSchema.Cols.ID)));
        museum.setName(cursor.getString(cursor.getColumnIndex(CollectionDBSchema.Cols.NAME)));
        museum.setAddress(cursor.getString(cursor.getColumnIndex(CollectionDBSchema.Cols.ADDRESS)));
        museum.setCollectionDate(cursor.getString(cursor.getColumnIndex(CollectionDBSchema.Cols.DATE)));

        return museum;
    }

    private ContentValues getContentValuesFromMuseum(Museum museum){
        ContentValues values = new ContentValues();

        values.put(CollectionDBSchema.Cols.ID, museum.getMuseumId());
        values.put(CollectionDBSchema.Cols.NAME, museum.getName());
        values.put(CollectionDBSchema.Cols.ADDRESS, museum.getAddress());
        values.put(CollectionDBSchema.Cols.DATE, formatDate(new Date()));

        return values;
    }

    private class CollectionDBHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "CollectionDb";
        private static final int VERSION = 1;

        private CollectionDBHelper(Context context){
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("create table %s(%s, %s, %s, %s)", CollectionDBSchema.TABLE_NAME,
                    CollectionDBSchema.Cols.ID, CollectionDBSchema.Cols.NAME, CollectionDBSchema.Cols.ADDRESS,
                    CollectionDBSchema.Cols.DATE);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private static class CollectionDBSchema{
        private static final String TABLE_NAME = "c_museums";

        private static final class Cols{
            private static final String ID = "id";
            private static final String NAME = "name";
            private static final String ADDRESS = "address";
            private static final String DATE = "date";
        }
    }
}
