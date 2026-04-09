package com.example.webcrawler.model;

public class UrlTask {
    private String url;
    private int depth;

    public UrlTask(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() { return url; }
    public int getDepth() { return depth; }
}