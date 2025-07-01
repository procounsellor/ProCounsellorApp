package com.catalyst.ProCounsellor.controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMasterData() {
        try {
            Firestore db = FirestoreClient.getFirestore();

            List<String> courses = List.of(
            	    "HSC",
            	    "Engineering",
            	    "Medical",
            	    "Law",
            	    "BBA",
            	    "MBA",
            	    "B.Com",
            	    "M.Com",
            	    "B.A.",
            	    "M.A.",
            	    "B.Sc",
            	    "M.Sc",
            	    "BCA",
            	    "MCA",
            	    "Diploma in Engineering",
            	    "Polytechnic",
            	    "ITI",
            	    "Design",
            	    "Architecture",
            	    "Pharmacy",
            	    "Nursing",
            	    "Journalism and Mass Communication",
            	    "Hotel Management",
            	    "Aviation",
            	    "Defense Services",
            	    "Chartered Accountancy",
            	    "Company Secretary",
            	    "Cost and Management Accounting",
            	    "Fashion Designing",
            	    "Animation and Multimedia",
            	    "B.Ed",
            	    "M.Ed",
            	    "Agriculture",
            	    "Veterinary Sciences",
            	    "Dental",
            	    "Social Work",
            	    "Psychology",
            	    "Banking and Finance",
            	    "UPSC",
            	    "Others"
            	);

            for (String course : courses) {
                db.collection("courseTypes")
                        .document(course.toLowerCase().replace(" ", "_"))
                        .set(Map.of("name", course));
            }

            List<String> states = List.of(
                    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
                    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
                    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
                    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
                    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
                    "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Jammu and Kashmir",
                    "Ladakh", "Puducherry", "Andaman and Nicobar Islands", "Dadra and Nagar Haveli and Daman and Diu",
                    "Chandigarh", "Lakshadweep", "Outside India"
            );

            for (String state : states) {
                db.collection("states")
                        .document(state.toLowerCase().replace(" ", "_").replaceAll("[^a-z_]", ""))
                        .set(Map.of("name", state));
            }

            return ResponseEntity.ok("Static master data uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading master data: " + e.getMessage());
        }
    }
}
