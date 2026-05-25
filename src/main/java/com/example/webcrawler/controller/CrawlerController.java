package com.example.webcrawler.controller;

import com.example.webcrawler.model.CrawlRequest;
import com.example.webcrawler.model.PageResult;
import com.example.webcrawler.service.CrawlerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "*")
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
public List<PageResult> getResults() {
    return crawlerService.getResults()
            .stream()
            .limit(50)
            .toList();
}
@GetMapping("/status")
public Map<String, Object> status() {
    Map<String, Object> data = new HashMap<>();
    data.put("totalUrls", crawlerService.getResults().size());
    data.put("isRunning", crawlerService.isCrawling());
    data.put("cacheHits", crawlerService.getCacheHits());
    data.put("cacheMisses", crawlerService.getCacheMisses());
    return data;
}

}