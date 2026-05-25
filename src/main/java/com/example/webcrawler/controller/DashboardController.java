package com.example.webcrawler.controller;

import com.example.webcrawler.service.CrawlerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    @Autowired
    private CrawlerService crawlerService;

    @GetMapping("/")
    public String dashboard(Model model, HttpSession session) {

        model.addAttribute("results", crawlerService.getResults());
        model.addAttribute("hits", crawlerService.getCacheHits());
        model.addAttribute("misses", crawlerService.getCacheMisses());
        model.addAttribute("crawling", crawlerService.isCrawling());

        String savedUrl = (String) session.getAttribute("enteredUrl");
        model.addAttribute("enteredUrl", savedUrl);

        return "dashboard";
    }

    @PostMapping("/crawl")
    public String startCrawl(@RequestParam String url, HttpSession session) {

        session.setAttribute("enteredUrl", url);

        crawlerService.startCrawling(url, 2);

        return "redirect:/";
    }
}