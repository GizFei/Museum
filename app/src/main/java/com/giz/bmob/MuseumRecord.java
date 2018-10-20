package com.giz.bmob;

import android.graphics.Bitmap;

public class MuseumRecord {

    private String mMuseumId;
    private String mName;
    private String mRecordDate;
    private String mContent;
    private String mPicturePath;

    public String getMuseumId() {
        return mMuseumId;
    }

    public void setMuseumId(String museumId) {
        mMuseumId = museumId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRecordDate() {
        return mRecordDate;
    }

    public void setRecordDate(String recordDate) {
        mRecordDate = recordDate;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getPicturePath() {
        return mPicturePath;
    }

    public void setPicturePath(String picturePath) {
        mPicturePath = picturePath;
    }
}
