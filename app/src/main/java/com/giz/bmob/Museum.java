package com.giz.bmob;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobObject;

public class Museum extends BmobObject {
    /**
     * 博物馆类，拥有博物馆的所有信息
     */
    private String mMuseumId;       // 博物馆ID（唯一，用来传递信息）
    private String mName;     // 博物馆名称
    private List<String> mCatalog;  // 博物馆类别（如：综合）
    private String mLogoUrl;        // 博物馆Logo（资源url）
    private String mPicFolder; // 博物馆图片的文件夹名称（位于assets中）
    private Drawable mLogo;   // Logo图片

    public Museum(){
        mMuseumId = "";
        mName = "";
        mCatalog = new ArrayList<>();
        mPicFolder = "";
        mLogoUrl = "";
        this.setTableName("museum");
    }

    public Museum(String uuid){
        this.mMuseumId = uuid;
        mName = "";
        mCatalog = new ArrayList<>();
        mPicFolder = "";
        mLogoUrl = "";
        this.setTableName("museum");
    }

    public String getMuseumId() {
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

    public String getLogoUrl() {
        return mLogoUrl;
    }

    public void setLogoUrl(String logo) {
        mLogoUrl = logo;
    }

    public String getPicFolder() {
        return mPicFolder;
    }

    public void setPicFolder(String picFolder) {
        mPicFolder = picFolder;
    }

    public void setLogo(Drawable logo) {
        mLogo = logo;
    }

    public Drawable getLogo() {
        return mLogo;
    }
}
