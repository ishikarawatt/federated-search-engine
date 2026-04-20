package com.example.federatedsearch.service;

import com.example.federatedsearch.external.InternalDatabaseService;
import com.example.federatedsearch.external.WebSearchAPIService;
import com.example.federatedsearch.external.YouTubeAPIService;
import com.example.federatedsearch.external.WikipediaAPIService;
import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AggregationService {
    
    private final WebSearchAPIService webSearchAPIService;
    private final YouTubeAPIService youTubeAPIService;
    private final InternalDatabaseService internalDatabaseService;
    private final WikipediaAPIService wikipediaAPIService;
    
    public AggregationService(WebSearchAPIService webSearchAPIService, 
                             YouTubeAPIService youTubeAPIService,
                             InternalDatabaseService internalDatabaseService,
                             WikipediaAPIService wikipediaAPIService) {
        this.webSearchAPIService = webSearchAPIService;
        this.youTubeAPIService = youTubeAPIService;
        this.internalDatabaseService = internalDatabaseService;
        this.wikipediaAPIService = wikipediaAPIService;
    }
    
    /**
     * Aggregates search results from all sources (PARALLEL FEDERATION)
     */
    public List<SearchResult> aggregateResults(String query) {
        List<SearchResult> aggregatedResults = new ArrayList<>();
        
        // 1. DuckDuckGo Web Search
        try {
            List<SearchResult> webResults = webSearchAPIService.search(query);
            aggregatedResults.addAll(webResults);
        } catch (Exception e) {
            System.err.println("Error fetching web results: " + e.getMessage());
        }
        
        // 2. Wikipedia Knowledge
        try {
            List<SearchResult> wikiResults = wikipediaAPIService.search(query);
            aggregatedResults.addAll(wikiResults);
        } catch (Exception e) {
            System.err.println("Error fetching Wikipedia results: " + e.getMessage());
        }
        
        // 3. YouTube Videos
        try {
            List<SearchResult> youtubeResults = youTubeAPIService.search(query);
            aggregatedResults.addAll(youtubeResults);
        } catch (Exception e) {
            System.err.println("Error fetching YouTube results: " + e.getMessage());
        }
        
        // 4. Internal Database
        try {
            List<SearchResult> internalResults = internalDatabaseService.searchFallback(query);
            aggregatedResults.addAll(internalResults);
        } catch (Exception e) {
            System.err.println("Error fetching internal results: " + e.getMessage());
        }
        
        return aggregatedResults;
    }
}
