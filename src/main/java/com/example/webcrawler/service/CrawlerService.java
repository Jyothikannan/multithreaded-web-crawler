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
import java.util.concurrent.atomic.AtomicInteger;
@Service
public class CrawlerService {

    @Autowired
    private ExecutorService executorService;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);
  

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

    private volatile boolean crawling = false;

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

    activeWorkers.set(0);   // <-- Add this

    cacheHits = 0;
    cacheMisses = 0;

    crawling = true;

    try {
        baseDomain = new java.net.URL(url).getHost();
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }

    urlQueue.offer(new UrlTask(url, 0));

    // Start 5 worker threads
    for (int i = 0; i < 5; i++) {
        executorService.submit(() -> processUrls(maxDepth));
    }
}
    // 🔁 Worker Thread Logic
  private void processUrls(int maxDepth) {

    while (true) {

        try {

            UrlTask task = urlQueue.poll(2, TimeUnit.SECONDS);

            // No work available
            if (task == null) {

                // Stop only if nobody is working AND queue is empty
                if (urlQueue.isEmpty() && activeWorkers.get() == 0) {
                    crawling = false;
                    break;
                }

                continue;
            }

            activeWorkers.incrementAndGet();

            try {

                String currentUrl = task.getUrl();
                int currentDepth = task.getDepth();

                if (currentDepth > maxDepth)
                    continue;

                if (visitedUrls.size() >= MAX_PAGES) {
                    crawling = false;
                    break;
                }

                // Skip duplicate pages
                if (!visitedUrls.add(currentUrl))
                    continue;

                // Cache check
                if (cache.contains(currentUrl)) {
                    cacheHits++;
                    continue;
                } else {
                    cacheMisses++;
                    cache.put(currentUrl);
                }

                System.out.println(
                        Thread.currentThread().getName()
                                + " crawling: "
                                + currentUrl
                );

                String title = currentUrl;

                try {
                    Document doc = Jsoup.connect(currentUrl)
                            .timeout(5000)
                            .get();

                    if (!doc.title().isBlank()) {
                        title = doc.title();
                    }

                } catch (Exception ignored) {}

                results.add(new PageResult(title, currentUrl));

                Set<String> links = parser.extractLinks(currentUrl);

                for (String link : links) {

                    if (link == null || !link.startsWith("http"))
                        continue;

                    if (link.contains("#"))
                        link = link.split("#")[0];

                    if (link.endsWith("/"))
                        link = link.substring(0, link.length() - 1);

                    if (link.contains(baseDomain)
                            && !visitedUrls.contains(link)) {

                        urlQueue.offer(
                                new UrlTask(link, currentDepth + 1)
                        );
                    }
                }

            } finally {
                activeWorkers.decrementAndGet();
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