package com.example.federatedsearch.service;

import com.example.federatedsearch.model.SearchResult;
import com.example.federatedsearch.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final AggregationService aggregationService;
    private final DuplicateRemovalService duplicateRemovalService;
    private final SpamDetectionService spamDetectionService;
    private final RankingService rankingService;
    private final SearchRepository searchRepository;
    
    // Constructor injection
    public SearchService(AggregationService aggregationService,
                        DuplicateRemovalService duplicateRemovalService,
                        SpamDetectionService spamDetectionService,
                        RankingService rankingService,
                        SearchRepository searchRepository) {
        this.aggregationService = aggregationService;
        this.duplicateRemovalService = duplicateRemovalService;
        this.spamDetectionService = spamDetectionService;
        this.rankingService = rankingService;
        this.searchRepository = searchRepository;
    }

    /**
     * Main search method that orchestrates the federated search process
     */
    public List<SearchResult> search(String query) {
        return search(query, "default-user", 20);
    }
    
    /**
     * Search with user tracking and result limit
     */
    public List<SearchResult> search(String query, String userId, int maxResults) {
        // Save search to history
        searchRepository.saveSearchQuery(userId, query);
        
        // Step 1: Aggregate results from all sources
        List<SearchResult> aggregatedResults = aggregationService.aggregateResults(query);
        
        // Step 2: Remove duplicates
        List<SearchResult> uniqueResults = duplicateRemovalService.removeDuplicates(aggregatedResults);
        
        // Step 3: Filter spam
        List<SearchResult> cleanResults = spamDetectionService.filterSpam(uniqueResults);
        
        // Step 4: Rank and limit results
        List<SearchResult> rankedResults = rankingService.rankAndLimit(cleanResults, query, maxResults);
        
        return rankedResults;
    }
    
    /**
     * Get search history for a user
     */
    public List<String> getSearchHistory(String userId) {
        return searchRepository.getSearchHistory(userId);
    }
    
    /**
     * Get popular queries
     */
    public List<String> getPopularQueries(int limit) {
        return searchRepository.getPopularQueries(limit);
    }
}
