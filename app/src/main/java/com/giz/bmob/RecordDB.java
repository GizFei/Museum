package com.giz.bmob;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordDB {

    private static final int MAX_RECORDS_OF_ONE = 5;

    private static RecordDB sRecordDB;
    private SQLiteDatabase mDatabase;

    public static RecordDB get(Context context){
        if(sRecordDB == null){
            sRecordDB = new RecordDB(context);
        }
        return sRecordDB;
    }

    private RecordDB(Context context){
        mDatabase = new RecordDBHelper(context.getApplicationContext()).getWritableDatabase();
    }

    public List<MuseumRecord> getMuseumRecords(){
        List<MuseumRecord> museumList = new ArrayList<>();
        Cursor cursor = mDatabase.query(RecordDBSchema.TABLE_NAME,
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

    public boolean isMuseumRecordFull(String id){
        Cursor cursor = mDatabase.query(RecordDBSchema.TABLE_NAME,
                null,
                RecordDBSchema.Cols.ID + "=?",
                new String[]{id},
                null,
                null,
                null);
        cursor.moveToFirst();
        boolean full = cursor.getCount() > MAX_RECORDS_OF_ONE;
        cursor.close();
        return full;
    }

    public void addMuseumRecord(MuseumRecord museum){
        mDatabase.insert(RecordDBSchema.TABLE_NAME, null,
                getContentValuesFromMuseumRecord(museum));
    }

    public void removeMuseumRecord(String id){
        mDatabase.delete(RecordDBSchema.TABLE_NAME, RecordDBSchema.Cols.ID + "=?",
                new String[]{id});
    }

    private String formatDate(Date date){
        String format = "yyyy-MM-dd hh:mm";
        return DateFormat.format(format, date).toString();
    }

    private MuseumRecord getMuseumFromCursor(Cursor cursor){
        MuseumRecord museum = new MuseumRecord();
        museum.setMuseumId(cursor.getString(cursor.getColumnIndex(RecordDBSchema.Cols.ID)));
        museum.setName(cursor.getString(cursor.getColumnIndex(RecordDBSchema.Cols.NAME)));
        museum.setContent(cursor.getString(cursor.getColumnIndex(RecordDBSchema.Cols.CONTENT)));
        museum.setRecordDate(cursor.getString(cursor.getColumnIndex(RecordDBSchema.Cols.DATE)));
        museum.setPicturePath(cursor.getString(cursor.getColumnIndex(RecordDBSchema.Cols.PICTURE)));

        return museum;
    }

    private ContentValues getContentValuesFromMuseumRecord(MuseumRecord museum){
        ContentValues values = new ContentValues();

        values.put(RecordDBSchema.Cols.ID, museum.getMuseumId());
        values.put(RecordDBSchema.Cols.NAME, museum.getName());
        values.put(RecordDBSchema.Cols.CONTENT, museum.getContent());
        values.put(RecordDBSchema.Cols.DATE, formatDate(new Date()));
        values.put(RecordDBSchema.Cols.PICTURE, museum.getPicturePath());

        return values;
    }

    private class RecordDBHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "RecordDB";
        private static final int VERSION = 1;

        private RecordDBHelper(Context context){
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("create table %s(%s, %s, %s, %s, %s)", RecordDBSchema.TABLE_NAME,
                    RecordDBSchema.Cols.ID, RecordDBSchema.Cols.NAME, RecordDBSchema.Cols.DATE,
                    RecordDBSchema.Cols.CONTENT, RecordDBSchema.Cols.PICTURE);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private static class RecordDBSchema{
        private static final String TABLE_NAME = "r_museums";

        private static final class Cols{
            private static final String ID = "id";
            private static final String NAME = "name";
            private static final String DATE = "date";
            private static final String CONTENT = "content";
            private static final String PICTURE = "picture";
        }
    }

    private byte[] bitmap2bytes(Bitmap bitmap){
        ByteArrayOutputStream picBytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, picBytes);
        return picBytes.toByteArray();
    }

    private Bitmap bytes2bitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public List<MuseumRecord> testMuseumRecords(){
        MuseumRecord record1 = new MuseumRecord();
        record1.setMuseumId("1");
        record1.setName("Museum1");
        record1.setRecordDate("2018-10-19 23:59");
        record1.setContent("Content1");
        record1.setPicturePath(null);

        MuseumRecord record2 = new MuseumRecord();
        record2.setMuseumId("2");
        record2.setName("Museum2");
        record2.setRecordDate("2018-10-19 23:59");
        record2.setContent("建筑面积20991平方米，陈列面积7600多平方米，2009年12月22日正式开放。" +
                "目前设有“越地长歌——浙江历史文化陈列”、“钱江潮——浙江现代革命历史陈列”、“非凡的心声——世界非物质文化遗产中" +
                "的中国古琴”、“意匠生辉——浙江民间造型艺术”、“十里红妆——宁绍婚俗中的红妆家具”等常设展览，地下一层为临时展" +
                "厅，不定期举办国内外的临时展览。");
        record2.setPicturePath(null);

        MuseumRecord record3 = new MuseumRecord();
        record3.setMuseumId("3");
        record3.setName("Museum3");
        record3.setRecordDate("2018-10-19 23:59");
        record3.setContent("Content3");
        record3.setPicturePath(null);

        MuseumRecord record4 = new MuseumRecord();
        record4.setMuseumId("4");
        record4.setName("Museum4");
        record4.setRecordDate("2018-10-19 23:59");
        record4.setContent("这是第四条内容。。我也不知道写什么好。");
        record4.setPicturePath(null);

        List<MuseumRecord> museumRecords = new ArrayList<>();
        museumRecords.add(record1);
        museumRecords.add(record2);
        museumRecords.add(record3);
        museumRecords.add(record4);

        return museumRecords;
    }
}
