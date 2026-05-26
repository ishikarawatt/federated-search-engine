package com.example.federatedsearch.service;

import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SpamDetectionService {
    
    // Common spam indicators
    private static final List<Pattern> SPAM_PATTERNS = List.of(
        Pattern.compile("(?i)(click here|buy now|limited time|act now|winner|congratulations)")
    );
    
    /**
     * Filters out spam or low-quality search results
     */
    public List<SearchResult> filterSpam(List<SearchResult> results) {
        List<SearchResult> cleanResults = new ArrayList<>();
        
        for (SearchResult result : results) {
            if (!isSpam(result)) {
                cleanResults.add(result);
            }
        }
        
        return cleanResults;
    }
    
    /**
     * Checks if a search result is spam
     */
    private boolean isSpam(SearchResult result) {
        // Check for spam patterns in title and description
        for (Pattern pattern : SPAM_PATTERNS) {
            if (pattern.matcher(result.getTitle()).find() || 
                pattern.matcher(result.getDescription()).find()) {
                return true;
            }
        }
        
        // Check for suspiciously high view/like ratio
        if (result.getViews() > 0) {
            double likeRatio = (double) result.getLikes() / result.getViews();
            if (likeRatio > 0.5) { // More than 50% like ratio is suspicious
                return true;
            }
        }
        
        // Check for empty or very short descriptions
        if (result.getDescription() == null || result.getDescription().trim().length() < 10) {
            return true;
        }
        
        return false;
    }
}
