package com.giz.utils;

import java.util.List;

public class Museum {

    private String mName;     // 博物馆名称
    private List<String> mCatalog;  // 博物馆类别（如：综合）
    private int mLogo;        // 博物馆Logo（资源id：R.drawable.xxx）

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
}
