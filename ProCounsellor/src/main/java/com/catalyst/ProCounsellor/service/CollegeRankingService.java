package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.CollegeRanking;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CollegeRankingService {

    private static final String COLLECTION_NAME = "collegeRankings";

    public CollegeRanking createCollege(CollegeRanking college) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        college.setCollegeId(docRef.getId()); // Auto-generate ID
        ApiFuture<WriteResult> future = docRef.set(college);
        future.get(); // wait
        return college;
    }
    
    public List<CollegeRanking> createCollegeBulk(List<CollegeRanking> colleges) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        WriteBatch batch = firestore.batch();

        for (CollegeRanking college : colleges) {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            college.setCollegeId(docRef.getId());
            batch.set(docRef, college);
        }

        batch.commit().get();

        return colleges;
    }

    public CollegeRanking getCollegeById(String collegeId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(collegeId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(CollegeRanking.class);
        } else {
            return null; // or throw custom exception
        }
    }

    public List<CollegeRanking> getAllColleges() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                        .map(doc -> doc.toObject(CollegeRanking.class))
                        .collect(Collectors.toList());
    }

    public CollegeRanking updateCollege(String collegeId, CollegeRanking updatedCollege) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(collegeId);
        updatedCollege.setCollegeId(collegeId);
        ApiFuture<WriteResult> future = docRef.set(updatedCollege);
        future.get(); // wait
        return updatedCollege;
    }

    public String deleteCollege(String collegeId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(collegeId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get(); // wait
        return "College with ID " + collegeId + " deleted successfully.";
    }
}
