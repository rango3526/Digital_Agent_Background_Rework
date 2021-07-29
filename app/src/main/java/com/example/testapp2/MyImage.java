package com.example.testapp2;

import android.net.Uri;

public class MyImage {
    public final String uriString;
    public final String fileName;
    public final int size;
    public final int dateTaken;
    public String objectDetected;
    public final long imageID;
    public boolean bookmarked = false;
    // This is associated with the page the lesson pops up on
    public long sessionID = -1;

    public MyImage(Uri uri, String name, int size, int dateTaken, String objectDetected, long imageID, long sessionID) {
        this.uriString = uri.toString();
        this.fileName = name;
        this.size = size;
        this.dateTaken = dateTaken;
        this.objectDetected = objectDetected;
        this.imageID = imageID;
        this.sessionID = sessionID;
    }
}