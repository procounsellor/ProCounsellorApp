package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.feedingModel.TrendingCourses;
import com.catalyst.ProCounsellor.service.TrendingCoursesService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/trending-courses")
@CrossOrigin
public class TrendingCoursesController {

    @Autowired
    private TrendingCoursesService trendingCoursesService;

    @PostMapping
    public TrendingCourses createCourse(@RequestBody TrendingCourses course) throws ExecutionException, InterruptedException {
        return trendingCoursesService.createCourse(course);
    }

    @GetMapping("/{courseId}")
    public TrendingCourses getCourseById(@PathVariable String courseId) throws ExecutionException, InterruptedException {
        return trendingCoursesService.getCourseById(courseId);
    }

    @GetMapping
    public List<TrendingCourses> getAllCourses() throws ExecutionException, InterruptedException {
        return trendingCoursesService.getAllCourses();
    }

    @PutMapping("/{courseId}")
    public TrendingCourses updateCourse(@PathVariable String courseId, @RequestBody TrendingCourses updatedCourse) throws ExecutionException, InterruptedException {
        return trendingCoursesService.updateCourse(courseId, updatedCourse);
    }

    @DeleteMapping("/{courseId}")
    public String deleteCourse(@PathVariable String courseId) throws ExecutionException, InterruptedException {
        return trendingCoursesService.deleteCourse(courseId);
    }
}
