package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.Community;
import com.catalyst.ProCounsellor.service.CommunityService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping
    public Community createCommunity(@RequestBody Community community) throws ExecutionException, InterruptedException {
        return communityService.createCommunity(community);
    }

    @GetMapping("/{communityId}")
    public Community getCommunityById(@PathVariable String communityId) throws ExecutionException, InterruptedException {
        return communityService.getCommunityById(communityId);
    }

    @GetMapping
    public List<Community> getAllCommunities() throws ExecutionException, InterruptedException {
        return communityService.getAllCommunities();
    }

    @PutMapping("/{communityId}")
    public Community updateCommunity(@PathVariable String communityId, @RequestBody Community updatedCommunity) throws ExecutionException, InterruptedException {
        return communityService.updateCommunity(communityId, updatedCommunity);
    }

    @DeleteMapping("/{communityId}")
    public String deleteCommunity(@PathVariable String communityId) throws ExecutionException, InterruptedException {
        return communityService.deleteCommunity(communityId);
    }
}
