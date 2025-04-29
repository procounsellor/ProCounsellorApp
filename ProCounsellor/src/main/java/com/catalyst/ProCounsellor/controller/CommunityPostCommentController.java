package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPostComment;
import com.catalyst.ProCounsellor.service.CommunityPostCommentService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/community-post-comments")
@CrossOrigin
public class CommunityPostCommentController {

    @Autowired
    private CommunityPostCommentService commentService;

    @PostMapping
    public CommunityPostComment addComment(@RequestBody CommunityPostComment comment) throws ExecutionException, InterruptedException {
        return commentService.addComment(comment);
    }

    @GetMapping("/{postId}")
    public List<CommunityPostComment> getComments(@PathVariable String postId) throws ExecutionException, InterruptedException {
        return commentService.getCommentsByPostId(postId);
    }

    @DeleteMapping("/{postId}/{commentIndex}")
    public String deleteComment(@PathVariable String postId, @PathVariable int commentIndex) throws ExecutionException, InterruptedException {
        return commentService.deleteComment(postId, commentIndex);
    }
   }