package com.example.testapp2;

import androidx.fragment.app.Fragment;

public class DataTrackingManager {

    private static DataTrackingModel dtm;
    private static boolean currentlyTracking = true;

    public static void startTracking() {
        currentlyTracking = true;
    }

    public static void stopTracking() {
        currentlyTracking = false;
    }

    public static void activityStarted() {

    }

    public static void fragmentStarted(Fragment fragment) {
        DataTrackingModel.PageEntry pe = new DataTrackingModel.PageEntry();
        pe.entryTime = System.currentTimeMillis();
        pe.millisecondsOnPage = 0;
        pe.pageName = fragment.getClass().getName();
        pe.sessionID = -99;
        dtm.pushToPageHistory(pe);
    }

    public static void avatarChosen(String avatarName) {

    }

    public static void lessonBookmarked(/*lesson info*/) {

    }

    public static void lessonOpened(/*lesson info*/) {

    }

    public static void notificationClicked() {

    }

    public static void appOpened() {

    }

    public static void appClosing() {

    }

    public static void appSuspending() {

    }

    public static void stopRefreshClicked() {

    }

    public static void setDTM(DataTrackingModel _dtm) {
        dtm = _dtm;
    }
}
