package com.example.webcrawler.service;

import com.example.webcrawler.core.LinkParser;
import com.example.webcrawler.model.PageResult;
import com.example.webcrawler.model.UrlTask;
import com.example.webcrawler.cache.LRUCacheWithTTL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Service
public class CrawlerService {

    @Autowired
    private ExecutorService executorService;

    private BlockingQueue<UrlTask> urlQueue = new LinkedBlockingQueue<>();

    private LinkParser parser = new LinkParser();

    private LRUCacheWithTTL cache =
            new LRUCacheWithTTL(1000, 5 * 60 * 1000);

    private String baseDomain;

    // prevents duplicate crawling
    private Set<String> visitedUrls =
            ConcurrentHashMap.newKeySet();

    // stores title + url for dashboard
    private List<PageResult> results =
            new CopyOnWriteArrayList<>();

    private boolean crawling = false;

    private int cacheHits = 0;
    private int cacheMisses = 0;

    private static final int MAX_PAGES = 500;

    // 🚀 Start Crawl
    public void startCrawling(String url, int maxDepth) {

        System.out.println("Starting crawl for: " + url);

        // reset
        visitedUrls.clear();
        results.clear();
        urlQueue.clear();

        cacheHits = 0;
        cacheMisses = 0;

        crawling = true;

        try {
            baseDomain = new java.net.URL(url).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }

        urlQueue.offer(new UrlTask(url, 0));

        // 5 worker threads
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> processUrls(maxDepth));
        }
    }

    // 🔁 Worker Thread Logic
    private void processUrls(int maxDepth) {

        while (true) {

            try {

                UrlTask task = urlQueue.poll();

                if (task == null) {

                    Thread.sleep(300);

                    // stop if queue empty
                    if (urlQueue.isEmpty()) {
                        crawling = false;
                        return;
                    }

                    continue;
                }

                String currentUrl = task.getUrl();
                int currentDepth = task.getDepth();

                // depth limit
                if (currentDepth > maxDepth)
                    continue;

                // max page limit
                if (visitedUrls.size() >= MAX_PAGES) {
                    crawling = false;
                    return;
                }

                // cache check
                if (cache.contains(currentUrl)) {
                    cacheHits++;
                    continue;
                } else {
                    cacheMisses++;
                    cache.put(currentUrl);
                }

                // duplicate check
                if (!visitedUrls.add(currentUrl))
                    continue;

                System.out.println(
                        Thread.currentThread().getName()
                                + " crawling: "
                                + currentUrl
                );

                // 🔥 Fetch title
                String title = "No Title";

                try {
                    Document doc =
                            Jsoup.connect(currentUrl)
                                    .timeout(5000)
                                    .get();

                    title = doc.title();

                    if (title == null || title.isBlank()) {
                        title = currentUrl;
                    }

                } catch (Exception ignored) {
                }

                // store dashboard result
                results.add(
                        new PageResult(title, currentUrl)
                );

                // extract child links
                Set<String> links;

                try {
                    links = parser.extractLinks(currentUrl);
                } catch (Exception e) {
                    continue;
                }

                // 🔥 enqueue children (CLEAN VERSION)
                for (String link : links) {

                    try {

                        if (link == null || !link.startsWith("http"))
                            continue;

                        // ✅ remove fragment (#section)
                        if (link.contains("#")) {
                            link = link.split("#")[0];
                        }

                        // ✅ remove trailing slash
                        if (link.endsWith("/")) {
                            link = link.substring(0, link.length() - 1);
                        }

                        // ✅ restrict to same domain
                        if (link.contains(baseDomain)) {

                            urlQueue.offer(
                                    new UrlTask(
                                            link,
                                            currentDepth + 1
                                    )
                            );
                        }

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Dashboard Methods

    public List<PageResult> getResults() {
        return results;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getCacheMisses() {
        return cacheMisses;
    }

    public boolean isCrawling() {
        return crawling;
    }
}