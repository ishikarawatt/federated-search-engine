package com.example.federatedsearch.external;

import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternalDatabaseService {
    
    // Simulated internal database
    private final List<SearchResult> internalDatabase = Arrays.asList(
        new SearchResult(
            "Internal Doc: Getting Started Guide",
            "Official getting started documentation for beginners",
            "https://internal.docs/getting-started",
            "Internal",
            25000,
            1200
        ),
        new SearchResult(
            "Internal Doc: API Reference",
            "Complete API reference documentation with examples",
            "https://internal.docs/api-reference",
            "Internal",
            35000,
            2100
        ),
        new SearchResult(
            "Internal Doc: Best Practices",
            "Industry best practices and recommended approaches",
            "https://internal.docs/best-practices",
            "Internal",
            28000,
            1800
        ),
        new SearchResult(
            "Internal Doc: Troubleshooting Guide",
            "Common issues and their solutions",
            "https://internal.docs/troubleshooting",
            "Internal",
            22000,
            950
        ),
        new SearchResult(
            "Internal Doc: Advanced Configuration",
            "Advanced setup and configuration options",
            "https://internal.docs/advanced-config",
            "Internal",
            18000,
            1100
        )
    );
    
    public List<SearchResult> search(String query) {
        // Simple text matching simulation
        // In production, this would use proper database queries or search engines like Elasticsearch
        String lowerQuery = query.toLowerCase();
        
        return internalDatabase.stream()
            .filter(result -> 
                result.getTitle().toLowerCase().contains(lowerQuery) ||
                result.getDescription().toLowerCase().contains(lowerQuery)
            )
            .collect(Collectors.toList());
    }
    
    // Fallback method that returns some results even if no match
    public List<SearchResult> searchFallback(String query) {
        List<SearchResult> results = search(query);
        
        if (results.isEmpty()) {
            // Return first few results as fallback
            return internalDatabase.subList(0, Math.min(3, internalDatabase.size()));
        }
        
        return results;
    }
}
