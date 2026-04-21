package com.example.federatedsearch.util;

import com.example.federatedsearch.model.SearchResult;

public class RankingAlgorithm {
    
    /**
     * Calculates a relevance score for a search result
     * Enhanced scoring: TF-IDF-like + Source Credibility + Engagement
     */
    public static double calculateScore(SearchResult result, String query) {
        double score = 0.0;
        
        // 1. Query Relevance (0-40 points) - MOST IMPORTANT
        score += getQueryRelevanceScore(result, query) * 40;
        
        // 2. Source Credibility (0-25 points)
        score += getSourceCredibilityScore(result.getSource()) * 25;
        
        // 3. Engagement Metrics (0-20 points)
        double engagementScore = getEngagementScore(result);
        score += engagementScore * 20;
        
        // 4. Content Quality (0-15 points)
        score += getContentQualityScore(result) * 15;
        
        return score;
    }
    
    /**
     * Returns credibility score based on source type (normalized 0-1)
     */
    private static double getSourceCredibilityScore(String source) {
        if (source == null) return 0.5;
        
        switch (source.toLowerCase()) {
            case "wikipedia":
                return 1.0; // Highest credibility
            case "internal":
                return 0.95;
            case "youtube":
                return 0.85;
            case "duckduckgo":
                return 0.80;
            case "web":
                return 0.75;
            case "research":
                return 0.95;
            case "github":
                return 0.90;
            default:
                return 0.70;
        }
    }
    
    /**
     * Calculates query relevance based on TF-IDF-like matching (normalized 0-1)
     */
    private static double getQueryRelevanceScore(SearchResult result, String query) {
        if (query == null || query.isEmpty()) return 0.0;
        
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\s+");
        
        // Title matching (60% weight)
        if (result.getTitle() != null) {
            String lowerTitle = result.getTitle().toLowerCase();
            
            // Exact phrase match (highest priority)
            if (lowerTitle.contains(lowerQuery)) {
                score += 0.6;
            } else {
                // Individual word matching
                int matchedWords = 0;
                for (String word : queryWords) {
                    if (lowerTitle.contains(word)) {
                        matchedWords++;
                    }
                }
                score += (matchedWords / (double) queryWords.length) * 0.5;
            }
        }
        
        // Description matching (40% weight)
        if (result.getDescription() != null) {
            String lowerDesc = result.getDescription().toLowerCase();
            
            if (lowerDesc.contains(lowerQuery)) {
                score += 0.4;
            } else {
                int matchedWords = 0;
                for (String word : queryWords) {
                    if (lowerDesc.contains(word)) {
                        matchedWords++;
                    }
                }
                score += (matchedWords / (double) queryWords.length) * 0.3;
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Calculates engagement score based on views and likes (normalized 0-1)
     */
    private static double getEngagementScore(SearchResult result) {
        double viewsScore = 0.0;
        double likesScore = 0.0;
        
        // Logarithmic scaling for views (prevents extreme values)
        if (result.getViews() > 0) {
            viewsScore = Math.log10(result.getViews() + 1) / 6.0; // log(1,000,000) = 6
        }
        
        // Logarithmic scaling for likes
        if (result.getLikes() > 0) {
            likesScore = Math.log10(result.getLikes() + 1) / 5.0; // log(100,000) = 5
        }
        
        // Weighted combination (60% views, 40% likes)
        return (viewsScore * 0.6 + likesScore * 0.4);
    }
    
    /**
     * Calculates content quality score (normalized 0-1)
     */
    private static double getContentQualityScore(SearchResult result) {
        double score = 0.0;
        
        // Description length indicates quality
        if (result.getDescription() != null) {
            int descLength = result.getDescription().length();
            if (descLength > 100) {
                score += 0.5;
            } else if (descLength > 50) {
                score += 0.3;
            } else if (descLength > 20) {
                score += 0.1;
            }
        }
        
        // Title length indicates quality
        if (result.getTitle() != null) {
            int titleLength = result.getTitle().length();
            if (titleLength > 20 && titleLength < 100) {
                score += 0.5;
            } else if (titleLength > 10) {
                score += 0.3;
            }
        }
        
        return Math.min(score, 1.0);
    }
}
