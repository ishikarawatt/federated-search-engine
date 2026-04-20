package com.example.federatedsearch.repository;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SearchRepository {
    
    // In-memory storage for search history
    private final Map<String, List<String>> searchHistory = new ConcurrentHashMap<>();
    
    // Popular queries tracker
    private final Map<String, Integer> popularQueries = new ConcurrentHashMap<>();
    
    /**
     * Saves a search query to history
     */
    public void saveSearchQuery(String userId, String query) {
        searchHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(query);
        
        // Update popular queries
        popularQueries.merge(query.toLowerCase(), 1, Integer::sum);
    }
    
    /**
     * Gets search history for a user
     */
    public List<String> getSearchHistory(String userId) {
        return searchHistory.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Gets top N popular queries
     */
    public List<String> getPopularQueries(int limit) {
        return popularQueries.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Clears search history for a user
     */
    public void clearSearchHistory(String userId) {
        searchHistory.remove(userId);
    }
}
