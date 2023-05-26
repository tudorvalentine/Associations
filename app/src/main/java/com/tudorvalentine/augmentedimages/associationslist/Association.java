package com.tudorvalentine.augmentedimages.associationslist;

import com.tudorvalentine.augmentedimages.app.AppConfig;

public class Association {
    private int source_arrow;
    private String url_image, url_prev, user;
    private String image_name, doc_name, doc_prev;
    private long id;
    public Association(String image_name, String doc_name, String doc_prev, int source_arrow, String user){
        this.user = user;
        this.image_name = image_name;
        this.doc_name = doc_name;
        this.doc_prev = doc_prev;
        this.url_image = AppConfig.URL_IMAGES + image_name + "/" + user;
        this.url_prev = AppConfig.URL_PREV + doc_prev + "/" + user;
        this.source_arrow = source_arrow;
    }
    public Association(int id, String image_name, String doc_name, String doc_prev){
        this.id = id;
        this.image_name = image_name;
        this.doc_name = doc_name;
        this.doc_prev = doc_prev;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSource_arrow(int source_arrow) {
        this.source_arrow = source_arrow;
    }


    public int getSource_arrow() {
        return source_arrow;
    }

    public String getUrl_image() {
        return url_image;
    }

    public void setUrl_image(String url_image) {
        this.url_image = url_image;
    }

    public String getUrl_prev() {
        return url_prev;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getDoc_name() {
        return doc_name;
    }

    public String getDoc_prev() {
        return doc_prev;
    }

    public void setUrl_prev(String url_prev) {
        this.url_prev = url_prev;
    }
}
