package com.example.hhoo7.udacitycoursep8;

public class Paper {
    private String mTitle;
    private String mPublicationDate;
    private String mWebUrl;

    public Paper(String mTitle, String mPublicationDate, String mWebUrl) {
        this.mTitle = mTitle;
        this.mPublicationDate = mPublicationDate;
        this.mWebUrl = mWebUrl;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmPublicationDate() {
        return mPublicationDate;
    }

    public String getmWebUrl() {
        return mWebUrl;
    }

}
