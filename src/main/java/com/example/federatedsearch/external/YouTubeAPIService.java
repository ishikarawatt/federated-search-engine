package com.example.federatedsearch.external;

import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeAPIService {
    
    public List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // Simulated YouTube search results
        // In production, this would call the YouTube Data API v3
        results.add(new SearchResult(
                "Video: " + query + " - Full Tutorial",
                "Complete video tutorial covering " + query + " with practical examples",
                "https://youtube.com/watch?v=example1&query=" + query.replace(" ", "+"),
                "YouTube",
                (int)(Math.random() * 100000),
                (int)(Math.random() * 10000)
        ));
        
        results.add(new SearchResult(
                query + " Explained in 10 Minutes",
                "Quick and concise explanation of " + query,
                "https://youtube.com/watch?v=example2&query=" + query.replace(" ", "+"),
                "YouTube",
                (int)(Math.random() * 80000),
                (int)(Math.random() * 8000)
        ));
        
        results.add(new SearchResult(
                "Advanced " + query + " Techniques",
                "Deep dive into advanced concepts and best practices for " + query,
                "https://youtube.com/watch?v=example3&query=" + query.replace(" ", "+"),
                "YouTube",
                (int)(Math.random() * 60000),
                (int)(Math.random() * 6000)
        ));
        
        return results;
    }
}
