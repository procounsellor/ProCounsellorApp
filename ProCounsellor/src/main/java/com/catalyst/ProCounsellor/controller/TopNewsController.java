package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.model.TopNews;
import com.catalyst.ProCounsellor.service.TopNewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/news")
public class TopNewsController {
	@Autowired
    private TopNewsService newsService;

	@PostMapping(consumes = {"multipart/form-data"})
	public CompletableFuture<ResponseEntity<String>> addNewsWithImage(
	        @RequestParam("news") String newsJson,
	        @RequestParam("image") MultipartFile imageFile) {
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        TopNews news = objectMapper.readValue(newsJson, TopNews.class);

	        // Call the service to handle the news and image
	        return newsService.addNewsWithImage(imageFile, news)
	                .thenApply(result -> ResponseEntity.ok("News added successfully with image"));
	    } catch (Exception e) {
	        return CompletableFuture.completedFuture(ResponseEntity.badRequest()
	                .body("Invalid request: " + e.getMessage()));
	    }
	}

    // Endpoint to fetch all news
	@GetMapping
	public ResponseEntity<List<TopNews>> getAllNews() {
	    try {
	        List<TopNews> newsList = newsService.getAllNews();
	        return ResponseEntity.ok(newsList);
	    } catch (ExecutionException | InterruptedException e) {
	        return ResponseEntity.status(500).body(new ArrayList<>()); // Return empty list on error
	    }
	}

    // Endpoint to fetch specific news by ID
    @GetMapping("/{newsId}")
    public CompletableFuture<ResponseEntity<TopNews>> getNewsById(@PathVariable String newsId) {
        return newsService.getNewsById(newsId)
                .thenApply(news -> {
                    if (news == null) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(news);
                });
    }
    
    @PutMapping(value = "/{newsId}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updateNewsWithImage(
            @PathVariable String newsId,
            @RequestParam(value = "news", required = false) TopNews updatedNews,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (updatedNews == null && (imageFile == null || imageFile.isEmpty())) {
                return ResponseEntity.badRequest().body("No data provided to update");
            }
            newsService.updateNewsWithImage(newsId, updatedNews, imageFile);
            return ResponseEntity.ok("News updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating news: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{newsId}")
    public ResponseEntity<String> deleteNews(@PathVariable String newsId) {
        try {
            newsService.deleteNews(newsId);
            return ResponseEntity.ok("News deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting news: " + e.getMessage());
        }
    }

}