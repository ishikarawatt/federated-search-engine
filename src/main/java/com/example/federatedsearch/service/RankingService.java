package com.example.federatedsearch.service;

import com.example.federatedsearch.model.SearchResult;
import com.example.federatedsearch.util.RankingAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {
    
    /**
     * Ranks search results by relevance score
     */
    public List<SearchResult> rankResults(List<SearchResult> results, String query) {
        return results.stream()
            .sorted(Comparator.comparingDouble(
                (SearchResult result) -> RankingAlgorithm.calculateScore(result, query)
            ).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Ranks and limits results to top N
     */
    public List<SearchResult> rankAndLimit(List<SearchResult> results, String query, int limit) {
        List<SearchResult> ranked = rankResults(results, query);
        
        if (ranked.size() <= limit) {
            return ranked;
        }
        
        return ranked.subList(0, limit);
    }
}
