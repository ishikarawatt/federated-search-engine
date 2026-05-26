package com.example.federatedsearch.service;

import com.example.federatedsearch.external.WebSearchAPIService;
import com.example.federatedsearch.external.YouTubeAPIService;
import com.example.federatedsearch.external.WikipediaAPIService;
import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AggregationService {
    
    private final WebSearchAPIService webSearchAPIService;
    private final YouTubeAPIService youTubeAPIService;
    private final WikipediaAPIService wikipediaAPIService;
    
    // Thread pool for parallel execution
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public AggregationService(WebSearchAPIService webSearchAPIService, 
                             YouTubeAPIService youTubeAPIService,
                             WikipediaAPIService wikipediaAPIService) {
        this.webSearchAPIService = webSearchAPIService;
        this.youTubeAPIService = youTubeAPIService;
        this.wikipediaAPIService = wikipediaAPIService;
    }
    
    /**
     * Aggregates search results from all sources (PARALLEL FEDERATION)
     * Uses CompletableFuture for async parallel execution
     */
    public List<SearchResult> aggregateResults(String query) {
        List<SearchResult> aggregatedResults = new ArrayList<>();
        
        System.out.println("=== Starting Federated Search for: " + query + " ===");
        
        // Create parallel tasks for all sources
        CompletableFuture<List<SearchResult>> webFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching DuckDuckGo results...");
                List<SearchResult> results = webSearchAPIService.search(query);
                System.out.println("DuckDuckGo returned: " + results.size() + " results");
                return results;
            } catch (Exception e) {
                System.err.println("Error fetching web results: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<SearchResult>();
            }
        }, executor);
        
        CompletableFuture<List<SearchResult>> wikiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching Wikipedia results...");
                List<SearchResult> results = wikipediaAPIService.search(query);
                System.out.println("Wikipedia returned: " + results.size() + " results");
                return results;
            } catch (Exception e) {
                System.err.println("Error fetching Wikipedia results: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<SearchResult>();
            }
        }, executor);
        
        CompletableFuture<List<SearchResult>> youtubeFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching YouTube results...");
                List<SearchResult> results = youTubeAPIService.search(query);
                System.out.println("YouTube returned: " + results.size() + " results");
                return results;
            } catch (Exception e) {
                System.err.println("Error fetching YouTube results: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<SearchResult>();
            }
        }, executor);
        
        // Wait for all tasks to complete and combine results
        try {
            CompletableFuture.allOf(webFuture, wikiFuture, youtubeFuture).join();
            
            // Combine all results
            List<SearchResult> webResults = webFuture.get();
            List<SearchResult> wikiResults = wikiFuture.get();
            List<SearchResult> youtubeResults = youtubeFuture.get();
            
            aggregatedResults.addAll(webResults);
            aggregatedResults.addAll(wikiResults);
            aggregatedResults.addAll(youtubeResults);
            
            System.out.println("=== Total aggregated results: " + aggregatedResults.size() + " ===");
            System.out.println("  - Web: " + webResults.size());
            System.out.println("  - Wikipedia: " + wikiResults.size());
            System.out.println("  - YouTube: " + youtubeResults.size());
            
        } catch (Exception e) {
            System.err.println("Error combining results: " + e.getMessage());
            e.printStackTrace();
        }
        
        return aggregatedResults;
    }
}
