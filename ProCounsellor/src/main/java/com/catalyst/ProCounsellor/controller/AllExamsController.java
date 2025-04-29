package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.AllExams;
import com.catalyst.ProCounsellor.service.AllExamsService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/exams")
public class AllExamsController {

    @Autowired
    private AllExamsService allExamsService;

    @PostMapping
    public AllExams createExam(@RequestBody AllExams exam) throws ExecutionException, InterruptedException {
        return allExamsService.createExam(exam);
    }

    @GetMapping("/{examId}")
    public AllExams getExamById(@PathVariable String examId) throws ExecutionException, InterruptedException {
        return allExamsService.getExamById(examId);
    }

    @GetMapping
    public List<AllExams> getAllExams() throws ExecutionException, InterruptedException {
        return allExamsService.getAllExams();
    }

    @PutMapping("/{examId}")
    public AllExams updateExam(@PathVariable String examId, @RequestBody AllExams updatedExam) throws ExecutionException, InterruptedException {
        return allExamsService.updateExam(examId, updatedExam);
    }

    @DeleteMapping("/{examId}")
    public String deleteExam(@PathVariable String examId) throws ExecutionException, InterruptedException {
        return allExamsService.deleteExam(examId);
    }
}
