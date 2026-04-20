package com.example.federatedsearch.util;

import com.example.federatedsearch.model.SearchResult;

public class RankingAlgorithm {
    
    /**
     * Calculates a relevance score for a search result
     * Based on views, likes, and source credibility
     */
    public static double calculateScore(SearchResult result, String query) {
        double score = 0.0;
        
        // Views component (0-40 points)
        double viewsScore = Math.log10(result.getViews() + 1) * 10;
        score += Math.min(viewsScore, 40);
        
        // Likes component (0-30 points)
        double likesScore = Math.log10(result.getLikes() + 1) * 10;
        score += Math.min(likesScore, 30);
        
        // Source credibility (0-20 points)
        score += getSourceCredibilityScore(result.getSource());
        
        // Query relevance (0-10 points)
        score += getQueryRelevanceScore(result, query);
        
        return score;
    }
    
    /**
     * Returns credibility score based on source type
     */
    private static double getSourceCredibilityScore(String source) {
        if (source == null) return 5.0;
        
        switch (source.toLowerCase()) {
            case "internal":
                return 20.0; // Highest credibility
            case "youtube":
                return 15.0;
            case "web":
                return 12.0;
            case "research":
                return 18.0;
            case "github":
                return 16.0;
            default:
                return 10.0;
        }
    }
    
    /**
     * Calculates query relevance based on title and description matching
     */
    private static double getQueryRelevanceScore(SearchResult result, String query) {
        if (query == null || query.isEmpty()) return 0.0;
        
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        
        // Title match (higher weight)
        if (result.getTitle() != null && result.getTitle().toLowerCase().contains(lowerQuery)) {
            score += 6.0;
        }
        
        // Description match
        if (result.getDescription() != null && result.getDescription().toLowerCase().contains(lowerQuery)) {
            score += 4.0;
        }
        
        return Math.min(score, 10.0);
    }
}
