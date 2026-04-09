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
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("a[href]");

            for (Element element : elements) {
                String link = element.absUrl("href");
                if (!link.isEmpty()) {
                    links.add(link);
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching: " + url);
        }

        return links;
    }
}