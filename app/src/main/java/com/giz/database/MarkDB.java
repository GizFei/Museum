package com.giz.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MarkDB {

    private static MarkDB sMarkDB;
    private SQLiteDatabase mDatabase;

    private ContentResolver mResolver;

    public static MarkDB get(Context context){
        if(sMarkDB == null){
            sMarkDB = new MarkDB(context);
        }
        return sMarkDB;
    }

    private MarkDB(Context context){
        mDatabase = new MarkDBHelper(context.getApplicationContext()).getWritableDatabase();
    }

    /**
     * 增加一个打卡的博物馆
     * @param museum
     */
    public void addMarkMuseum(Museum museum){
        mDatabase.insert(MarkDBSchema.TABLE_NAME, null,
                getContentValuesFromMuseum(museum));
    }

    public boolean hasMarked(String id){
        Cursor cursor = mDatabase.query(MarkDBSchema.TABLE_NAME, null,
                MarkDBSchema.Cols.ID + "=?",
                new String[]{id},
                null,
                null,
                null);
        cursor.moveToFirst();
        boolean b = cursor.getCount() > 0;
        cursor.close();
        return b;
    }

    private ContentValues getContentValuesFromMuseum(Museum museum){
        ContentValues values = new ContentValues();

        values.put(MarkDBSchema.Cols.ID, museum.getMuseumId());

        return values;
    }

    private class MarkDBHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "MarkDb";
        private static final int VERSION = 1;

        private MarkDBHelper(Context context){
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("create table %s(%s)", MarkDBSchema.TABLE_NAME,
                    MarkDBSchema.Cols.ID);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private static class MarkDBSchema{
        private static final String TABLE_NAME = "m_museums";

        private static final class Cols{
            private static final String ID = "id";
        }
    }
}
