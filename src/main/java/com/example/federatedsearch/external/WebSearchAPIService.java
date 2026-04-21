package com.example.federatedsearch.external;

import com.example.federatedsearch.model.SearchResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@Service
public class WebSearchAPIService {
    
    private final RestTemplate restTemplate;
    
    public WebSearchAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Search using DuckDuckGo HTML scraping (free, no API key needed)
     */
    public List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            // DuckDuckGo HTML search
            String url = "https://html.duckduckgo.com/html/?q=" + 
                java.net.URLEncoder.encode(query, "UTF-8");
            
            // Fetch HTML with better settings
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://duckduckgo.com/")
                .timeout(10000)
                .maxBodySize(0)
                .get();
            
            // Parse results with multiple selectors for better coverage
            Elements resultElements = doc.select(".result");
            
            if (resultElements.isEmpty()) {
                // Try alternative selectors
                resultElements = doc.select("div[class*='result']");
            }
            
            if (resultElements.isEmpty()) {
                // Try another pattern
                resultElements = doc.select("div.result");
            }
            
            System.out.println("DuckDuckGo found " + resultElements.size() + " results for: " + query);
            
            for (Element element : resultElements) {
                try {
                    Element titleElement = element.selectFirst(".result__a");
                    Element snippetElement = element.selectFirst(".result__snippet");
                    
                    if (titleElement != null && snippetElement != null) {
                        String title = titleElement.text();
                        String rawUrl = titleElement.attr("href");
                        
                        // DuckDuckGo uses redirect URLs, extract the actual URL
                        String finalUrl = extractRealUrl(rawUrl);
                        String description = snippetElement.text();
                        
                        // Skip if URL is empty or invalid
                        if (finalUrl == null || finalUrl.isEmpty() || finalUrl.equals("#")) {
                            continue;
                        }
                        
                        // Skip duplicate URLs
                        boolean isDuplicate = results.stream()
                            .anyMatch(r -> r.getUrl().equals(finalUrl));
                        if (isDuplicate) {
                            continue;
                        }
                        
                        results.add(new SearchResult(
                            title,
                            description,
                            finalUrl,
                            "DuckDuckGo",
                            0,  // No fake views
                            0   // No fake likes
                        ));
                        
                        // Limit to top 10 results
                        if (results.size() >= 10) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Skip malformed results
                    System.err.println("Error parsing result: " + e.getMessage());
                }
            }
            
            System.out.println("DuckDuckGo returning " + results.size() + " valid results");
            
        } catch (Exception e) {
            System.err.println("DuckDuckGo search error: " + e.getMessage());
            // Return empty list instead of fake results
        }
        
        return results;
    }
    
    /**
     * Extract real URL from DuckDGo redirect URL
     */
    private String extractRealUrl(String duckduckgoUrl) {
        if (duckduckgoUrl == null || duckduckgoUrl.isEmpty()) {
            return "";
        }
        
        try {
            // DuckDuckGo URLs look like: /uddg?ActualURL
            if (duckduckgoUrl.contains("/uddg?")) {
                int idx = duckduckgoUrl.indexOf("/uddg?");
                String actualUrl = duckduckgoUrl.substring(idx + 6);
                
                // URL decode if needed
                return java.net.URLDecoder.decode(actualUrl, "UTF-8");
            }
            
            // If it's already a full URL
            if (duckduckgoUrl.startsWith("http")) {
                return duckduckgoUrl;
            }
            
            return duckduckgoUrl;
        } catch (Exception e) {
            return duckduckgoUrl;
        }
    }
}
