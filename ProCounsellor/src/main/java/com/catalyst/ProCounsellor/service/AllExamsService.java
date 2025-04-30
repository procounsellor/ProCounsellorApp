package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.AllExams;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AllExamsService {

    private static final String COLLECTION_NAME = "allExams";

    public AllExams createExam(AllExams exam) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        exam.setExamId(docRef.getId());
        ApiFuture<WriteResult> future = docRef.set(exam);
        future.get();
        return exam;
    }
    
    public List<AllExams> createExamsBulk(List<AllExams> exams) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        WriteBatch batch = firestore.batch();

        for (AllExams exam : exams) {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            exam.setExamId(docRef.getId());
            batch.set(docRef, exam);
        }

        batch.commit().get(); // Wait for batch commit

        return exams;
    }

    public AllExams getExamById(String examId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(examId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(AllExams.class);
        } else {
            return null; // or throw custom NotFoundException
        }
    }

    public List<AllExams> getAllExams() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                        .map(doc -> doc.toObject(AllExams.class))
                        .collect(Collectors.toList());
    }

    public AllExams updateExam(String examId, AllExams updatedExam) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(examId);
        updatedExam.setExamId(examId); // set ID
        ApiFuture<WriteResult> future = docRef.set(updatedExam);
        future.get(); // wait for completion
        return updatedExam;
    }

    public String deleteExam(String examId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(examId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get(); // wait
        return "Exam with ID " + examId + " deleted successfully.";
    }
}
