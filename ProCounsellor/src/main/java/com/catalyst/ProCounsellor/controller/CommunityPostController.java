package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPost;
import com.catalyst.ProCounsellor.service.CommunityPostService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/community-posts")
public class CommunityPostController {

    @Autowired
    private CommunityPostService postService;

    @PostMapping
    public CommunityPost createPost(@RequestBody CommunityPost post) throws ExecutionException, InterruptedException {
        return postService.createPost(post);
    }

    @GetMapping("/{postId}")
    public CommunityPost getPostById(@PathVariable String postId) throws ExecutionException, InterruptedException {
        return postService.getPostById(postId);
    }

    @GetMapping
    public List<CommunityPost> getAllPosts() throws ExecutionException, InterruptedException {
        return postService.getAllPosts();
    }

    @PutMapping("/{postId}")
    public CommunityPost updatePost(@PathVariable String postId, @RequestBody CommunityPost updatedPost) throws ExecutionException, InterruptedException {
        return postService.updatePost(postId, updatedPost);
    }

    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable String postId) throws ExecutionException, InterruptedException {
        return postService.deletePost(postId);
    }
}
