package com.example.testapp2;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;


public class FirebaseManager {
    static boolean initialized = false;
    static FirebaseStorage storage;
    static StorageReference storageReference;
    static FirebaseFirestore firestoreReference;

    static HashMap<String, HashMap<String, String>> objectLessons = new HashMap<>();


    public static void initFirebaseManager() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestoreReference = FirebaseFirestore.getInstance();
    }

    private static void uploadImage(Uri filePath, Context context, String name)
    {
        if (!initialized)
            initFirebaseManager();

        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + name +"-" + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(context,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(context,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }

    public static void updateFirestoreObjectLessons() {
        if (!initialized)
            initFirebaseManager();

        Log.w("Stuff", "Updating Firestore objectLessons...");

        firestoreReference.collection("objectLessons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("FirestoreResult", document.getId() + " => " + document.getData());
                        //String[] values = {"objectDisplayName", "definition", "lessonTopic", "videoLink"};
                        objectLessons.put(document.getId(), new HashMap<String, String>());
                        for (ObjectLesson.hashmapKeys enumVal : ObjectLesson.hashmapKeys.values()) {
                            if (enumVal == ObjectLesson.hashmapKeys.objectID) {
                                objectLessons.get(document.getId()).put(enumVal.name(), (String) document.getId());
                                continue;
                            }
                            Log.w("Firebase val", enumVal + ": " + (String) document.getData().get(enumVal.name()));
                            objectLessons.get(document.getId()).put(enumVal.name(), (String) document.getData().get(enumVal.name()));
                        }
                    }
                } else {
                    Log.w("Stuff", "Error getting documents.", task.getException());
                }
            }
        });

        Log.w("Stuff", "After update Firestore objectLessons");
    }

    public static boolean firestoreObjectNameExists(String objectName) {
        return objectLessons.containsKey(objectName);
    }

    public static ObjectLesson getFirestoreObjectData(String objectName) {
        try {
            HashMap<String, String> objectLessonHM = objectLessons.get(objectName);
            ObjectLesson ol = new ObjectLesson(objectLessonHM);
            return ol;
        }
        catch (Exception e) {
            Log.w("Firebase stuff", "Hashmap null, internet probably bad");
            return null;
        }
    }
}
