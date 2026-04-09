package com.example.webcrawler.model;

import lombok.Data;

@Data
public class CrawlRequest {
    private String url;
    private int depth;
}