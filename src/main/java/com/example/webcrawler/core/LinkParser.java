package com.example.webcrawler.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class LinkParser {

    public Set<String> extractLinks(String url) {

        Set<String> links = new HashSet<>();

        try {

            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .get();

            Elements elements = doc.select("a[href]");

            for (Element element : elements) {

                String link = element.absUrl("href").trim();

                // Skip empty links
                if (link.isEmpty())
                    continue;

                // Only allow HTTP/HTTPS links
                if (!(link.startsWith("http://") || link.startsWith("https://")))
                    continue;

                // Remove fragments
                int hash = link.indexOf('#');
                if (hash != -1) {
                    link = link.substring(0, hash);
                }

                // Remove trailing slash
                if (link.endsWith("/")) {
                    link = link.substring(0, link.length() - 1);
                }

                links.add(link);
            }

        } catch (Exception e) {
            System.out.println("Error fetching: " + url);
        }

        return links;
    }
}