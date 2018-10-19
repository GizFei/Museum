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
    private String mCoverUrl;
    private Drawable mLogo;   // Logo图片
    private Drawable mCover;  // 用于列表ViewPager的图片

    private double[] mLocation; // 用于定位的经纬度，0是经度，1是纬度

    private String mAddress;  // 博物馆地址
    private String mTicket;   // 博物馆门票
    private String mOpenTime; // 博物馆开放时间
    private String mIntro;   // 简介

    public Museum(){
        mMuseumId = "";
        mName = "";
        mCatalog = new ArrayList<>();
        mLogoUrl = "";
        mLocation = new double[2];
        this.setTableName("museum");
    }

    public Museum(String uuid){
        this.mMuseumId = uuid;
        mName = "";
        mCatalog = new ArrayList<>();
        mLogoUrl = "";
        mLocation = new double[2];
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

    public void setLogo(Drawable logo) {
        mLogo = logo;
    }

    public Drawable getLogo() {
        return mLogo;
    }

    public Drawable getCover() {
        return mCover;
    }

    public void setCover(Drawable cover) {
        mCover = cover;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public double[] getLocation() {
        return mLocation;
    }

    public void setLocation(double[] location) {
        mLocation = location;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getTicket() {
        return mTicket;
    }

    public void setTicket(String ticket) {
        mTicket = ticket;
    }

    public String getOpenTime() {
        return mOpenTime;
    }

    public void setOpenTime(String openTime) {
        mOpenTime = openTime;
    }

    public String getIntro() {
        return mIntro;
    }

    public void setIntro(String intro) {
        mIntro = intro;
    }
}
