package com.example.federatedsearch.service;

import com.example.federatedsearch.model.SearchResult;
import com.example.federatedsearch.util.HashUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DuplicateRemovalService {
    
    /**
     * Removes duplicate search results based on hash comparison
     */
    public List<SearchResult> removeDuplicates(List<SearchResult> results) {
        List<SearchResult> uniqueResults = new ArrayList<>();
        Set<String> seenHashes = new HashSet<>();
        
        for (SearchResult result : results) {
            String hash = HashUtil.generateHash(result.getTitle(), result.getUrl());
            
            if (!seenHashes.contains(hash)) {
                seenHashes.add(hash);
                uniqueResults.add(result);
            }
        }
        
        return uniqueResults;
    }
}
