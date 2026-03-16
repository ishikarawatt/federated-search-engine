package com.example.federatedsearch.model;

public class SearchResult {

    private String title;
    private String description;
    private String url;
    private String source;
    private int views;
    private int likes;

    public SearchResult() {}

    public SearchResult(String title, String description, String url, String source, int views, int likes) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.source = source;
        this.views = views;
        this.likes = likes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public int getViews() {
        return views;
    }

    public int getLikes() {
        return likes;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}