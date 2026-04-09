package com.example.webcrawler.service;

import com.example.webcrawler.core.LinkParser;
import com.example.webcrawler.model.UrlTask;
import com.example.webcrawler.cache.LRUCacheWithTTL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CrawlerService {

    @Autowired
    private ExecutorService executorService;

    private BlockingQueue<UrlTask> urlQueue = new LinkedBlockingQueue<>();

    private LinkParser parser = new LinkParser();

    private LRUCacheWithTTL cache = new LRUCacheWithTTL(1000, 5 * 60 * 1000);
    private String baseDomain;
    private Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

   public void startCrawling(String url, int maxDepth) {
    System.out.println("Starting crawl for: " + url);

    try {
        baseDomain = new java.net.URL(url).getHost();
    } catch (Exception e) {
        e.printStackTrace();
    }

    urlQueue.offer(new UrlTask(url, 0));

    for (int i = 0; i < 5; i++) {
        executorService.submit(() -> processUrls(maxDepth));
    }
}

   private void processUrls(int maxDepth) {
    while (!urlQueue.isEmpty()) {  // ✅ stop when queue is empty
        try {
            UrlTask task = urlQueue.poll();

            if (task == null) {
                Thread.sleep(300); // small wait
                continue;
            }

            String currentUrl = task.getUrl();
            int currentDepth = task.getDepth();

            // ✅ Depth check
            if (currentDepth > maxDepth) {
                continue;
            }

            // ✅ Cache check
            if (cache.contains(currentUrl)) {
                System.out.println("Skipping (cached): " + currentUrl);
                continue;
            }

            cache.put(currentUrl);

            // ✅ Limit total crawl size (avoid huge output)
            if (visitedUrls.size() > 500) {
                System.out.println("Limit reached, stopping crawl");
                return;
            }

            visitedUrls.add(currentUrl);

            System.out.println(Thread.currentThread().getName() +
                    " crawling (depth " + currentDepth + "): " + currentUrl);

            Set<String> links = parser.extractLinks(currentUrl);

            for (String link : links) {
                try {
                    // ✅ Skip invalid links
                    if (link == null || !link.startsWith("http")) continue;

                    String linkDomain = new java.net.URL(link).getHost();

                    // ✅ Domain restriction
                    if (linkDomain.equals(baseDomain) || linkDomain.endsWith("." + baseDomain)) {
                        urlQueue.offer(new UrlTask(link, currentDepth + 1));
                    }

                } catch (Exception e) {
                    // ignore invalid URLs
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    public Set<String> getVisitedUrls() {
    return visitedUrls;
}
}