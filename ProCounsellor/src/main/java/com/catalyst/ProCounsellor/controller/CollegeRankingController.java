package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.CollegeRanking;
import com.catalyst.ProCounsellor.service.CollegeRankingService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/colleges")
public class CollegeRankingController {

    @Autowired
    private CollegeRankingService collegeRankingService;

    @PostMapping
    public CollegeRanking createCollege(@RequestBody CollegeRanking college) throws ExecutionException, InterruptedException {
        return collegeRankingService.createCollege(college);
    }

    @GetMapping("/{collegeId}")
    public CollegeRanking getCollegeById(@PathVariable String collegeId) throws ExecutionException, InterruptedException {
        return collegeRankingService.getCollegeById(collegeId);
    }

    @GetMapping
    public List<CollegeRanking> getAllColleges() throws ExecutionException, InterruptedException {
        return collegeRankingService.getAllColleges();
    }

    @PutMapping("/{collegeId}")
    public CollegeRanking updateCollege(@PathVariable String collegeId, @RequestBody CollegeRanking updatedCollege) throws ExecutionException, InterruptedException {
        return collegeRankingService.updateCollege(collegeId, updatedCollege);
    }

    @DeleteMapping("/{collegeId}")
    public String deleteCollege(@PathVariable String collegeId) throws ExecutionException, InterruptedException {
        return collegeRankingService.deleteCollege(collegeId);
    }
}
