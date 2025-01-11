package com.catalyst.ProCounsellor.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class PhotoService {
	 public String uploadPhoto(String userId, byte[] photoBytes, String fileType, String role) throws IOException {
		    // Load the service account key file
		    FileInputStream serviceAccount = new FileInputStream("src/main/resources/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json");

		    // Initialize Firebase Storage with the credentials
		    Storage storage = StorageOptions.newBuilder()
		            .setCredentials(ServiceAccountCredentials.fromStream(serviceAccount))
		            .build()
		            .getService();

		    String bucketName = "procounsellor-71824.firebasestorage.app";
		    String fileName = null;
		    // Generate a unique file name for the photo
		    if(role.equals("user")) {
		    	fileName = "users/" + userId + "/photo." + fileType;
		    }
		    else if(role.equals("counsellor")) {
		    	fileName = "counsellors/" + userId + "/photo." + fileType;
		    }

		    // Upload the photo
		    BlobId blobId = BlobId.of(bucketName, fileName);
		    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileType).build();
		    Blob blob = storage.create(blobInfo, photoBytes);

		    blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));//Make URL publicly accessible.

		    String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName + "?alt=media";

		    return photoUrl; 
		}
	 
	 public String uploadPhotoToNews(String newsId, byte[] photoBytes, String fileType) throws IOException {
		    // Load the service account key file
		    FileInputStream serviceAccount = new FileInputStream("src/main/resources/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json");

		    // Initialize Firebase Storage with the credentials
		    Storage storage = StorageOptions.newBuilder()
		            .setCredentials(ServiceAccountCredentials.fromStream(serviceAccount))
		            .build()
		            .getService();

		    String bucketName = "procounsellor-71824.firebasestorage.app";
		    String fileName = "news/" + newsId + "/photo." + fileType;
		   
		    // Upload the photo
		    BlobId blobId = BlobId.of(bucketName, fileName);
		    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileType).build();
		    Blob blob = storage.create(blobInfo, photoBytes);

		    blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));//Make URL publicly accessible.

		    String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName + "?alt=media";

		    return photoUrl; 
		}
}
