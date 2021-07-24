package com.example.testapp2;

import android.net.Uri;

public class AvatarModel {
    private String name;
    private String story;
    private Uri imageUri;
    public AvatarModel() {
    }
    public AvatarModel(String name, String story, Uri imageUriString) {
        this.name = name;
        this.story = story;
        this.imageUri = imageUriString;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStory() {
        return story;
    }
    public void setStory(String story) {
        this.story = story;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
