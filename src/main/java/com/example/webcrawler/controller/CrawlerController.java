package com.example.webcrawler.controller;

import com.example.webcrawler.model.CrawlRequest;
import com.example.webcrawler.service.CrawlerService;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crawl")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @PostMapping
    public String startCrawl(@RequestBody CrawlRequest request) {
        crawlerService.startCrawling(request.getUrl(), request.getDepth());
        return "Crawling started for: " + request.getUrl();
    }
      @GetMapping("/results")
public List<String> getResults() {
    return crawlerService.getVisitedUrls()
            .stream()
            .limit(50) // 👈 limit output
            .toList();
}
}