package com.giz.database;

public class StarMuseum {

    private String mMuseumId;
    private String mName;
    private String mAddress;
    private String mCollectionDate;

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

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getCollectionDate() {
        return mCollectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        mCollectionDate = collectionDate;
    }

    public String getLogoCacheKey(){
        return mMuseumId + "_logo";
    }
}
