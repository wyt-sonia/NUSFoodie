package com.sonia_yt.nus.nusfoodie.model;

public class Image {

    private String id;
    private String url;

    public Image() {

    }

    public Image(String id, String url) {
        if (id.trim().equals("")) {
            id = "No ID";
        }
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
