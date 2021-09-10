package com.gulehri.edu.pk.screeny.model;

public class Model {
    private int id;
    private String imageUrl;

    private String originalUrl;
    private String mediumUrl;
    private String portraitUrl;

    public Model() {
    }

    public Model(int id, String imageUrl, String originalUrl, String mediumUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.originalUrl = originalUrl;
        this.mediumUrl = mediumUrl;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getMediumUrl() {
        return mediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        this.mediumUrl = mediumUrl;
    }
}
