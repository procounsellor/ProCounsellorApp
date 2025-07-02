package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.model.Course;
import com.catalyst.ProCounsellor.model.States;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/courseAndState")
public class CourseAndStatesController {

	@PostMapping("/upload-course")
    public ResponseEntity<String> uploadCourses(@RequestBody List<Course> courseList) {
        Firestore db = FirestoreClient.getFirestore();

        try {
            for (Course course : courseList) {
                String docId = course.getName().toLowerCase().replace(" ", "_").replaceAll("[^a-z_]", "");
                course.setCourseId(docId);
                db.collection("courseTypes").document(docId).set(course);
            }
            return ResponseEntity.ok("Courses uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading courses: " + e.getMessage());
        }
    }
	@GetMapping("/all-courses")
	public ResponseEntity<List<Course>> getAllCourses() throws Exception {
	    Firestore db = FirestoreClient.getFirestore();
	    ApiFuture<QuerySnapshot> future = db.collection("courseTypes").get();

	    List<Course> courseList = new ArrayList<>();
	    for (DocumentSnapshot doc : future.get().getDocuments()) {
	        Course course = doc.toObject(Course.class);
	        courseList.add(course);
	    }

	    return ResponseEntity.ok(courseList);
	}
	@GetMapping("/course/{courseId}")
	public ResponseEntity<Course> getCourseById(@PathVariable String courseId) throws Exception {
	    Firestore db = FirestoreClient.getFirestore();
	    DocumentReference docRef = db.collection("courseTypes").document(courseId);

	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot doc = future.get();

	    if (!doc.exists()) {
	        return ResponseEntity.notFound().build();
	    }

	    Course course = doc.toObject(Course.class);
	    return ResponseEntity.ok(course);
	}
	
	@PostMapping("/upload-state")
	public ResponseEntity<String> uploadStates(@RequestBody List<States> statesList) {
	    Firestore db = FirestoreClient.getFirestore();

	    try {
	        for (States state : statesList) {
	            String docId = state.getName().toLowerCase().replace(" ", "_").replaceAll("[^a-z_]", "");
	            state.setStateId(docId);
	            db.collection("states").document(docId).set(state);
	        }
	        return ResponseEntity.ok("States uploaded successfully.");
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("Error uploading states: " + e.getMessage());
	    }
	}
	
	@GetMapping("/all-states")
	public ResponseEntity<List<States>> getAllStates() throws Exception {
	    Firestore db = FirestoreClient.getFirestore();
	    ApiFuture<QuerySnapshot> future = db.collection("states").get();

	    List<States> stateList = new ArrayList<>();
	    for (DocumentSnapshot doc : future.get().getDocuments()) {
	        States state = doc.toObject(States.class);
	        stateList.add(state);
	    }

	    return ResponseEntity.ok(stateList);
	}
	
	@GetMapping("/state/{stateId}")
	public ResponseEntity<States> getStateById(@PathVariable String stateId) throws Exception {
	    Firestore db = FirestoreClient.getFirestore();
	    DocumentReference docRef = db.collection("states").document(stateId);

	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot doc = future.get();

	    if (!doc.exists()) {
	        return ResponseEntity.notFound().build();
	    }

	    States state = doc.toObject(States.class);
	    return ResponseEntity.ok(state);
	}
}
