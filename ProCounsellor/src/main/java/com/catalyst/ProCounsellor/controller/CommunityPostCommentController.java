package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPostComment;
import com.catalyst.ProCounsellor.service.CommunityPostCommentService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/community-post-comments")
public class CommunityPostCommentController {

    @Autowired
    private CommunityPostCommentService commentService;

    @PostMapping
    public CommunityPostComment createComment(@RequestBody CommunityPostComment comment) throws ExecutionException, InterruptedException {
        return commentService.createComment(comment);
    }

    @GetMapping("/{commentId}")
    public CommunityPostComment getCommentById(@PathVariable String commentId) throws ExecutionException, InterruptedException {
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/post/{postId}")
    public List<CommunityPostComment> getCommentsByPostId(@PathVariable String postId) throws ExecutionException, InterruptedException {
        return commentService.getCommentsByPostId(postId);
    }

    @PutMapping("/{commentId}")
    public CommunityPostComment updateComment(@PathVariable String commentId, @RequestBody CommunityPostComment updatedComment) throws ExecutionException, InterruptedException {
        return commentService.updateComment(commentId, updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable String commentId) throws ExecutionException, InterruptedException {
        return commentService.deleteComment(commentId);
    }
}
