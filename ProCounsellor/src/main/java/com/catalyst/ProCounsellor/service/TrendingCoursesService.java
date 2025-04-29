package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.TrendingCourses;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TrendingCoursesService {

    private static final String COLLECTION_NAME = "trendingCourses";

    public TrendingCourses createCourse(TrendingCourses course) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        course.setCourseId(docRef.getId()); // Auto-generate courseId
        ApiFuture<WriteResult> future = docRef.set(course);
        future.get(); // wait
        return course;
    }

    public TrendingCourses getCourseById(String courseId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(courseId);
        DocumentSnapshot document = docRef.get().get();
        return document.exists() ? document.toObject(TrendingCourses.class) : null;
    }

    public List<TrendingCourses> getAllCourses() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME).get().get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(TrendingCourses.class)).collect(Collectors.toList());
    }

    public TrendingCourses updateCourse(String courseId, TrendingCourses updatedCourse) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        updatedCourse.setCourseId(courseId);
        firestore.collection(COLLECTION_NAME).document(courseId).set(updatedCourse).get();
        return updatedCourse;
    }

    public String deleteCourse(String courseId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(COLLECTION_NAME).document(courseId).delete().get();
        return "Course with ID " + courseId + " deleted successfully.";
    }
}
