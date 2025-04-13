package com.catalyst.ProCounsellor.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PhotoService {

    // 🔧 Replace these with your bucket and path to Firebase JSON in GCS
    private static final String CONFIG_BUCKET = " pro_counsellor_firebase_config";
    private static final String CONFIG_PATH = "firebase/credentials/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json";

    private Storage getFirebaseStorage() throws IOException {
        // Load the Firebase service account JSON from GCS
        Storage gcsStorage = StorageOptions.getDefaultInstance().getService();
        Blob configBlob = gcsStorage.get(CONFIG_BUCKET, CONFIG_PATH);

        if (configBlob == null) {
            throw new IOException("❌ Firebase config file not found in GCS.");
        }

        InputStream serviceAccountStream = new ByteArrayInputStream(configBlob.getContent());

        // Create a Firebase Storage client with those credentials
        return StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(serviceAccountStream))
                .build()
                .getService();
    }

    public String uploadPhoto(String userId, byte[] photoBytes, String fileType, String role) throws IOException {
        Storage storage = getFirebaseStorage();

        String bucketName = "procounsellor-71824.firebasestorage.app"; // ✅ corrected bucket name
        String fileName;

        if ("user".equals(role)) {
            fileName = "users/" + userId + "/photo." + fileType;
        } else if ("counsellor".equals(role)) {
            fileName = "counsellors/" + userId + "/photo." + fileType;
        } else {
            throw new IllegalArgumentException("❌ Invalid role: " + role);
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileType).build();
        Blob blob = storage.create(blobInfo, photoBytes);
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    public String uploadPhotoToNews(String newsId, byte[] photoBytes, String fileType) throws IOException {
        Storage storage = getFirebaseStorage();

        String bucketName = "procounsellor-71824.firebasestorage.app";
        String fileName = "news/" + newsId + "/photo." + fileType;

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileType).build();
        Blob blob = storage.create(blobInfo, photoBytes);
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }
}
