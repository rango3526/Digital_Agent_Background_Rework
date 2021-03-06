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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;


public class FirebaseManager {
    static boolean initialized = false;
    static FirebaseStorage storage;
    static StorageReference storageReference;
    static FirebaseFirestore firestoreReference;
    static FirebaseDatabase realtimeDatabase;

    static HashMap<String, HashMap<String, Object>> objectLessonsHashMap = new HashMap<>();

    static boolean firestoreObjectLessonsGathered = false; // Means that it has stored ObjectLesson data from the database (aka it's not going to be null or something bad)

    public static void initFirebaseManager(Context context) {
        if (!initialized) {
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            firestoreReference = FirebaseFirestore.getInstance();
            realtimeDatabase = FirebaseDatabase.getInstance();
            initialized = true;

            String objectLessonsHashMapJson = HelperCode.getSharedPrefsObj(context).getString(GlobalVars.OBJECT_LESSONS_PREF_KEY, "");
            if (!objectLessonsHashMapJson.equals("")) {
                objectLessonsHashMap = HelperCode.jsonToObjectLessonsHashMap(objectLessonsHashMapJson);
                firestoreObjectLessonsGathered = true;
            }
        }
    }

    public static void updateFirestoreObjectLessons(Context context) {
        initFirebaseManager(context);

//        Log.w("Stuff", "Updating Firestore objectLessons...");

        firestoreReference.collection("objectLessons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String docID = "";
                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d("FirestoreResult", document.getId() + " => " + document.getData());
                        //String[] values = {"objectDisplayName", "definition", "lessonTopic", "videoLink"};
                        docID = document.getId();
                        objectLessonsHashMap.put(docID, new HashMap<String, Object>());
                        for (ObjectLesson.hashmapKeys enumVal : ObjectLesson.hashmapKeys.values()) {
                            if (enumVal == ObjectLesson.hashmapKeys.objectID) {
                                objectLessonsHashMap.get(docID).put(enumVal.name(), (String) document.getId());
                                continue;
                            }
                            if (enumVal == ObjectLesson.hashmapKeys.facts) {
                                objectLessonsHashMap.get(docID).put(enumVal.name(), (ArrayList<String>) document.getData().get(enumVal.name()));
                                continue;
                            }
//                            Log.w("Firebase val", enumVal + ": " + (String) document.getData().get(enumVal.name()));
                            objectLessonsHashMap.get(docID).put(enumVal.name(), (String) document.getData().get(enumVal.name()));
                        }
                    }

                    firestoreObjectLessonsGathered = true;
                    HelperCode.getSharedPrefsObj(context).edit().putString(GlobalVars.OBJECT_LESSONS_PREF_KEY, HelperCode.hashMapToJson(objectLessonsHashMap)).apply();
                } else {
                    Log.e("Stuff", "Error getting documents.", task.getException());
                }
            }
        });

//        Log.e("Stuff", "After update Firestore objectLessons");
    }

    public static boolean firestoreObjectLessonsGathered() {
        return firestoreObjectLessonsGathered;
    }

    public static boolean firestoreObjectNameExists(String objectName) {
        return objectLessonsHashMap.containsKey(objectName);
    }

    public static ObjectLesson getFirestoreObjectData(String objectName) {
        try {
            HashMap<String, Object> objectLessonHM = objectLessonsHashMap.get(objectName);
            ObjectLesson ol = new ObjectLesson(objectLessonHM);
            return ol;
        }
        catch (Exception e) {
            Log.e("Firebase stuff", "Hashmap null, internet probably bad");
            return null;
        }
    }

    public static void populateManagerDTM(Context context, String participantID) {
        initFirebaseManager(context);

        DataTrackingManager.lockUpload();
        DataTrackingModel dtm = new DataTrackingModel();
        dtm.setParticipantID(participantID);
        dtm.setParticipantName("Firstname Lastname");
        dtm.setTimeAppFirstStarted(String.valueOf(System.currentTimeMillis()));
        DataTrackingManager.setDTM(dtm);
        // This will be overwritten/merged when the below code runs (asynchronously)
        // If the code below finds no previous DTM stored in Firebase, then this is the default and will be kept as-is

        DatabaseReference dbRef = realtimeDatabase.getReference("dataTracking").child(participantID);
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    Log.e("Stuff", "Data change event; populating DTM...");
                    if (!snapshot.exists()) {
                        Log.e("Stuff", "No records on firebase, so default DTM will be used");
                        DataTrackingManager.unlockUpload();
                        DataTrackingManager.dtmChanged(context);
                        return;
                    }
                    DataTrackingModel onlineDtm = snapshot.getValue(DataTrackingModel.class);
                    Log.e("Stuff", "Online DTM is: " + (new Gson().toJson(onlineDtm)));
                    // Merges the offline version with the online version, once online is retrieved
                    DataTrackingManager.unlockUpload();
                    // TODO: Fix merge! For some reason it deletes all previous history when merging!
                    DataTrackingManager.setDTM(DataTrackingManager.mergeDtms(onlineDtm, DataTrackingManager.getDTM()));
                    Log.e("Stuff", "DTMs done merging and DTM is set");
                }
                else {
                    Log.e("Stuff", "Realtime database access failed; trying again");
                    populateManagerDTM(context, participantID);
                }
            }
        });

    }

    public static void uploadDTM(Context context, DataTrackingModel dtm) {
        initFirebaseManager(context);

        realtimeDatabase.getReference("dataTracking").child(dtm.getParticipantID()).setValue(dtm);
    }

    private static void uploadImage(Uri filePath, Context context, String name)
    {
        initFirebaseManager(context);

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
}
