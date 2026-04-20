package com.example.federatedsearch.service;

import com.example.federatedsearch.external.InternalDatabaseService;
import com.example.federatedsearch.external.WebSearchAPIService;
import com.example.federatedsearch.external.YouTubeAPIService;
import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AggregationService {
    
    private final WebSearchAPIService webSearchAPIService;
    private final YouTubeAPIService youTubeAPIService;
    private final InternalDatabaseService internalDatabaseService;
    
    public AggregationService(WebSearchAPIService webSearchAPIService, 
                             YouTubeAPIService youTubeAPIService,
                             InternalDatabaseService internalDatabaseService) {
        this.webSearchAPIService = webSearchAPIService;
        this.youTubeAPIService = youTubeAPIService;
        this.internalDatabaseService = internalDatabaseService;
    }
    
    /**
     * Aggregates search results from all sources
     */
    public List<SearchResult> aggregateResults(String query) {
        List<SearchResult> aggregatedResults = new ArrayList<>();
        
        try {
            // Fetch from Web Search API
            List<SearchResult> webResults = webSearchAPIService.search(query);
            aggregatedResults.addAll(webResults);
        } catch (Exception e) {
            System.err.println("Error fetching web results: " + e.getMessage());
        }
        
        try {
            // Fetch from YouTube API
            List<SearchResult> youtubeResults = youTubeAPIService.search(query);
            aggregatedResults.addAll(youtubeResults);
        } catch (Exception e) {
            System.err.println("Error fetching YouTube results: " + e.getMessage());
        }
        
        try {
            // Fetch from Internal Database
            List<SearchResult> internalResults = internalDatabaseService.searchFallback(query);
            aggregatedResults.addAll(internalResults);
        } catch (Exception e) {
            System.err.println("Error fetching internal results: " + e.getMessage());
        }
        
        return aggregatedResults;
    }
}
