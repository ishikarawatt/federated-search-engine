package com.example.federatedsearch.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping
    public List<String> search(@RequestParam String query) {

        List<String> results = new ArrayList<>();

        results.add("Result 1 for " + query);
        results.add("Result 2 for " + query);
        results.add("Result 3 for " + query);

        return results;
    }
}