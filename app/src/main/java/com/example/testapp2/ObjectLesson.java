package com.example.testapp2;

import android.util.Log;

import java.util.HashMap;

public class ObjectLesson {
    private String objectID;
    private String objectDisplayName;
    private String objectDefinition;
    private String lessonTopic;
    private String videoLink;

    private HashMap<String, String> hashmapRepresentation;

    public enum hashmapKeys {objectID, objectDisplayName, definition, lessonTopic, videoLink};

    public HashMap<String, String> getHashmapRepresentation() {
        if (hashmapRepresentation == null) {
            //throw new RuntimeException("CUSTOM EXCEPTION: Hashmap was null");
            Log.w("Firebase stuff", "Hashmap was null; internet connection probably bad");
        }
        return hashmapRepresentation;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
        updateHashmapRepresentation();
    }

    public String getObjectDisplayName() {
        return objectDisplayName;
    }

    public void setObjectDisplayName(String objectDisplayName) {
        this.objectDisplayName = objectDisplayName;
        updateHashmapRepresentation();
    }

    public String getObjectDefinition() {
        return objectDefinition;
    }

    public void setObjectDefinition(String objectDefinition) {
        this.objectDefinition = objectDefinition;
        updateHashmapRepresentation();
    }

    public String getLessonTopic() {
        return lessonTopic;
    }

    public void setLessonTopic(String lessonTopic) {
        this.lessonTopic = lessonTopic;
        updateHashmapRepresentation();
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
        updateHashmapRepresentation();
    }




    public ObjectLesson() {

    }

    public ObjectLesson(String objectID, String objectDisplayName, String objectDefinition, String lessonTopic, String videoLink) {
        this.objectID = objectID;
        this.objectDisplayName = objectDisplayName;
        this.objectDefinition = objectDefinition;
        this.lessonTopic = lessonTopic;
        this.videoLink = videoLink;
        updateHashmapRepresentation();
    }

    public ObjectLesson(HashMap<String,String> hashmap) {
        try {
            this.objectID = hashmap.get(hashmapKeys.objectID.name());
            this.objectDisplayName = hashmap.get(hashmapKeys.objectDisplayName.name());
            this.objectDefinition = hashmap.get(hashmapKeys.definition.name());
            this.lessonTopic = hashmap.get(hashmapKeys.lessonTopic.name());
            this.videoLink = hashmap.get(hashmapKeys.videoLink.name());
            updateHashmapRepresentation();
        }
        catch (Exception e) {
            Log.e("Hashap Issue", e.getMessage());
        }
    }

    private void updateHashmapRepresentation() {
        HashMap<String,String> updatedHM = new HashMap<>();
        updatedHM.put(hashmapKeys.lessonTopic.name(), this.lessonTopic);
        updatedHM.put(hashmapKeys.definition.name(), this.objectDefinition);
        updatedHM.put(hashmapKeys.objectID.name(), this.objectID);
        updatedHM.put(hashmapKeys.objectDisplayName.name(), this.objectDisplayName);
        updatedHM.put(hashmapKeys.videoLink.name(), this.videoLink);
        this.hashmapRepresentation = updatedHM;
    }
}
