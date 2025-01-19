package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.TopNews;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TopNewsService {

    private static final String NEWS = "news";

    // Add a news item with image upload
    public CompletableFuture<Void> addNewsWithImage(MultipartFile imageFile, TopNews news) {
        String newsId = UUID.randomUUID().toString();
        news.setNewsId(newsId);
        news.setDate(Timestamp.now());

        return CompletableFuture.supplyAsync(() -> {
            try {
                String imageUrl = uploadImage(imageFile, newsId);
                news.setImageUrl(imageUrl);

                saveNewsToFirestore(news);
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Error adding news", e);
            }
        });
    }

    // Get all news from Firestore
    public List<TopNews> getAllNews() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        QuerySnapshot querySnapshot = firestore.collection(NEWS).get().get();

        List<TopNews> newsList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
        	TopNews news = document.toObject(TopNews.class);
            newsList.add(news);
        }
        return newsList;
    }

    // Get a specific news by ID from Firestore
    public CompletableFuture<TopNews> getNewsById(String newsId) {
        Firestore firestore = FirestoreClient.getFirestore();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return firestore.collection(NEWS)
                        .document(newsId)
                        .get()
                        .get()
                        .toObject(TopNews.class);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching news by ID", e);
            }
        });
    }

    private void saveNewsToFirestore(TopNews news) {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(NEWS).document(news.getNewsId()).set(news);
    }
    
    public String uploadImage(MultipartFile imageFile, String newsId) throws IOException {
	    // Load the service account key file
	    FileInputStream serviceAccount = new FileInputStream("src/main/resources/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json");

	    // Initialize Firebase Storage with the credentials
	    Storage storage = StorageOptions.newBuilder()
	            .setCredentials(ServiceAccountCredentials.fromStream(serviceAccount))
	            .build()
	            .getService();

	    String bucketName = "procounsellor-71824.firebasestorage.app";
	    String fileType = imageFile.getContentType().split("/")[1];
	    String fileName = "news/" + newsId + "/photo." + fileType;
	   
	    // Upload the photo
	    BlobId blobId = BlobId.of(bucketName, fileName);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileType).build();
	    Blob blob = storage.create(blobInfo, imageFile.getBytes());

	    blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));//Make URL publicly accessible.

	    String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName + "?alt=media";

	    return photoUrl; 
	}
    
    public void updateNewsWithImage(String newsId, TopNews updatedNews, MultipartFile imageFile)
            throws ExecutionException, InterruptedException, IOException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(NEWS).document(newsId);

        Map<String, Object> updates = new HashMap<>();
        
        if(updatedNews!=null) {
	        if (updatedNews.getDescriptionParagraph() != null) {
	            updates.put("descriptionParagraph", updatedNews.getDescriptionParagraph());
	        }
	        if (updatedNews.getFullNews() != null) {
	            updates.put("fullNews", updatedNews.getFullNews());
	        }
	       }
        
        if (imageFile != null && !imageFile.isEmpty()) {
        	String imageUrl = uploadImage(imageFile, newsId);
            updates.put("imageUrl", imageUrl);
        }
        docRef.update(updates).get();
    }
    
    public void deleteNews(String newsId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(NEWS).document(newsId).delete().get();
    }
}
