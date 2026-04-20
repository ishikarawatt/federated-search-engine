package com.example.federatedsearch.controller;

import com.example.federatedsearch.model.SearchResult;
import com.example.federatedsearch.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    // Constructor injection
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Main search endpoint
     */
    @GetMapping
    public List<SearchResult> search(@RequestParam String query,
                                     @RequestParam(defaultValue = "default-user") String userId,
                                     @RequestParam(defaultValue = "20") int maxResults) {
        return searchService.search(query, userId, maxResults);
    }
    
    /**
     * Get search history for a user
     */
    @GetMapping("/history")
    public List<String> getSearchHistory(@RequestParam(defaultValue = "default-user") String userId) {
        return searchService.getSearchHistory(userId);
    }
    
    /**
     * Get popular queries
     */
    @GetMapping("/popular")
    public List<String> getPopularQueries(@RequestParam(defaultValue = "10") int limit) {
        return searchService.getPopularQueries(limit);
    }
}