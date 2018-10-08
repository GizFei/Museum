package com.giz.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Museum {

    private UUID mMuseumId;       // 博物馆ID（唯一，用来传递信息）
    private String mName;     // 博物馆名称
    private List<String> mCatalog;  // 博物馆类别（如：综合）
    private int mLogo;        // 博物馆Logo（资源id：R.drawable.xxx）
    private String mPicFolder; // 博物馆图片的文件夹名称（位于assets中）

    public Museum(){
        this(UUID.randomUUID());
    }

    private Museum(UUID uuid){
        this.mMuseumId = uuid;
        mName = "";
        mCatalog = new ArrayList<>();
        mLogo = 0;
    }

    public UUID getMuseumId() {
        return mMuseumId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<String> getCatalog() {
        return mCatalog;
    }

    public void setCatalog(List<String> catalog) {
        mCatalog = catalog;
    }

    public int getLogo() {
        return mLogo;
    }

    public void setLogo(int logo) {
        mLogo = logo;
    }

    public String getPicFolder() {
        return mPicFolder;
    }

    public void setPicFolder(String picFolder) {
        mPicFolder = picFolder;
    }
}
