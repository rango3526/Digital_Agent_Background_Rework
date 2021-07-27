package com.example.testapp2;

import android.content.Context;
import android.util.Log;

import androidx.navigation.NavDestination;

import java.util.ArrayList;
import java.util.Random;

public class DataTrackingManager {

    private static DataTrackingModel dtm;
    private static boolean currentlyTracking = true;

    public static void startTracking(Context context) {
        String participantID = HelperCode.getSharedPrefsObj(context).getString(GlobalVars.PARTICIPANT_ID_PREF_KEY, "");
        if (participantID.equals("")) {
            participantID = Long.toString(new Random(System.currentTimeMillis()).nextLong());
            HelperCode.getSharedPrefsObj(context).edit().putString(GlobalVars.PARTICIPANT_ID_PREF_KEY, participantID).apply();
        }

        FirebaseManager.populateManagerDTM(participantID);
        currentlyTracking = true;
    }

    public static void stopTracking() {
        currentlyTracking = false;
    }

    public static void dtmChanged() {
        FirebaseManager.uploadDTM(dtm);
    }

    public static void pageChange(NavDestination dest) {
        pageChange(System.currentTimeMillis(), -1, dest.getLabel().toString(), -99);
    }

    public static void pageChange(String pageName, long sessionID) {
        pageChange(System.currentTimeMillis(), -1, pageName, sessionID);
    }

    public static void pageChange(long entryTime, long millisecondsOnPage, String pageName, long sessionID) {
        if (dtm == null) { // If the dtm hasn't been created (so the system hasn't had time to connect to firebase yet), it will just keep waiting until it does
            Log.e("Stuff", "Waiting 1 second and trying again");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            pageChange(entryTime, millisecondsOnPage, pageName, sessionID);
                        }
                    },
                    1000
            );
            return;
        }
        Log.e("Stuff", "Page change info being added to DTM");

        if (dtm.getPageHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getPageHistory().size()-1;
            DataTrackingModel.PageEntry prevPageEntry = dtm.getPageHistory().get(index);
            prevPageEntry.millisecondsOnPage = System.currentTimeMillis() - prevPageEntry.entryTime;
        }

        DataTrackingModel.PageEntry pe = new DataTrackingModel.PageEntry();
        pe.entryTime = entryTime;
        pe.millisecondsOnPage = millisecondsOnPage;
        pe.pageName = pageName;
        pe.sessionID = sessionID;
        dtm.pushToPageHistory(pe);

        dtmChanged();
    }

    public static void avatarChosen(String avatarName) {
        if (dtm.getAvatarHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getPageHistory().size()-1;
            DataTrackingModel.AvatarEntry prevAvatarEntry = dtm.getAvatarHistory().get(index);
            prevAvatarEntry.millisecondsKept = System.currentTimeMillis() - prevAvatarEntry.entryTime;
        }

        DataTrackingModel.AvatarEntry avatarEntry = new DataTrackingModel.AvatarEntry();
        avatarEntry.avatarName = avatarName;
        avatarEntry.entryTime = System.currentTimeMillis();
        avatarEntry.millisecondsKept = -1;
        dtm.pushToAvatarHistory(avatarEntry);

        dtmChanged();
    }

    public static void lessonBookmarked(/*lesson info*/) {

        dtmChanged();
    }

    public static void lessonOpened(String objectDetected) {
        if (dtm.getLessonHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getLessonHistory().size()-1;
            DataTrackingModel.LessonEntry lessonEntry = dtm.getLessonHistory().get(index);
            lessonEntry.millisecondsOnLesson = System.currentTimeMillis() - lessonEntry.entryTime;
        }
        DataTrackingModel.LessonEntry lessonEntry = new DataTrackingModel.LessonEntry();
        lessonEntry.bookmarkHistory = new ArrayList<>();
        lessonEntry.entryTime = System.currentTimeMillis();
        lessonEntry.objectDetected = objectDetected;
        lessonEntry.sessionID = -99;
        lessonEntry.millisecondsOnLesson = -1;
        dtm.pushToLessonHistory(lessonEntry);

        dtmChanged();
    }

    public static void notificationClicked(MyImage myImage, ObjectLesson objectLesson) {
        DataTrackingModel.NotificationClickEntry notificationClickEntry = new DataTrackingModel.NotificationClickEntry();
        notificationClickEntry.clickTime = System.currentTimeMillis();
        notificationClickEntry.leadsToSessionID = -99;
        notificationClickEntry.objectDetected = myImage.objectDetected;
        dtm.pushToNotificationClickedHistory(notificationClickEntry);

        dtmChanged();
    }

    public static void appOpened() {

        dtmChanged();
    }

    public static void appClosing() {

        dtmChanged();
    }

    public static void appSuspending() {

        dtmChanged();
    }

    public static void stopRefreshClicked() {

        dtmChanged();
    }

    public static void setDTM(DataTrackingModel _dtm) {
        dtm = _dtm;
        dtmChanged();
    }
}
