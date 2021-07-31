package com.example.testapp2;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class DataTrackingManager {

    private static DataTrackingModel dtm;
    private static boolean currentlyTracking = true;
    // Uploading DTM is locked until it has synced with the Firebase
    private static boolean uploadLocked = true;

    private static Context context;

    public static void startTracking(Context _context, String participantID) {
        context = _context;
        HelperCode.getSharedPrefsObj(context).edit().putString(GlobalVars.PARTICIPANT_ID_PREF_KEY, participantID).apply();
        FirebaseManager.populateManagerDTM(participantID);
        currentlyTracking = true;
    }

    public static void stopTracking() {
        currentlyTracking = false;
    }

    public static void dtmChanged() {
        if (!uploadLocked)
            FirebaseManager.uploadDTM(dtm);
    }

    public static void pageChange(long sessionID, String pageName) {
        if (!currentlyTracking)
            return;

        pageChange(sessionID, System.currentTimeMillis(), -1, pageName);
    }

    public static void pageChange(long sessionID, long entryTime, long millisecondsOnPage, String pageName) {
        if (!currentlyTracking)
            return;

        if (dtm == null) { // If the dtm hasn't been created (so the system hasn't had time to connect to firebase yet), it will just keep waiting until it does
            Log.e("Stuff", "Waiting 1 second and trying again");

            HelperCode.callInSeconds(() -> pageChange(sessionID, entryTime, millisecondsOnPage, pageName), 1);

//            new java.util.Timer().schedule(
//                    new java.util.TimerTask() {
//                        @Override
//                        public void run() {
//                            pageChange(sessionID, entryTime, millisecondsOnPage, pageName);
//                        }
//                    },
//                    1000
//            );
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
        if (!currentlyTracking)
            return;

        if (dtm.getAvatarHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getAvatarHistory().size()-1;
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

    public static void lessonBookmarked(long sessionID, boolean isNowBookmarked) { // TODO: actually call this function, requires rework of the way Lessons get assigned sessionIDs
        if (!currentlyTracking)
            return;

        DataTrackingModel.LessonEntry.BookmarkEntry bookmarkEntry = new DataTrackingModel.LessonEntry.BookmarkEntry();
        bookmarkEntry.entryTime = System.currentTimeMillis();
        bookmarkEntry.isNowBookmarked = isNowBookmarked;

        // TODO: this is inefficient, but fixing this means reorganizing the entire database structure. Not much data, so probably not worth it.
        ArrayList<DataTrackingModel.LessonEntry> lessonHistory = dtm.getLessonHistory();
        for (DataTrackingModel.LessonEntry lessonEntry : lessonHistory) {
            if (lessonEntry.sessionID == sessionID) {
                lessonEntry.bookmarkHistory.add(bookmarkEntry);
                break;
            }
        }

        dtmChanged();
    }

    public static void lessonOpened(long sessionID, String objectDetected) {
        if (!currentlyTracking)
            return;

        if (dtm.getLessonHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getLessonHistory().size()-1;
            DataTrackingModel.LessonEntry lessonEntry = dtm.getLessonHistory().get(index);
            lessonEntry.millisecondsOnLesson = System.currentTimeMillis() - lessonEntry.entryTime;
        }
        DataTrackingModel.LessonEntry lessonEntry = new DataTrackingModel.LessonEntry();
        lessonEntry.bookmarkHistory = new ArrayList<>();
        lessonEntry.entryTime = System.currentTimeMillis();
        lessonEntry.objectDetected = objectDetected;
        lessonEntry.sessionID = sessionID;
        lessonEntry.millisecondsOnLesson = -1;
        dtm.pushToLessonHistory(lessonEntry);

        dtmChanged();
    }

    public static void notificationClicked(long sessionID, MyImage myImage, ObjectLesson objectLesson) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.NotificationClickEntry notificationClickEntry = new DataTrackingModel.NotificationClickEntry();
        notificationClickEntry.clickTime = System.currentTimeMillis();
        notificationClickEntry.leadsToSessionID = -99;
        notificationClickEntry.objectDetected = myImage.objectDetected;
        dtm.pushToNotificationClickedHistory(notificationClickEntry);

        dtmChanged();
    }

    public static void appOpened() {
        if (!currentlyTracking)
            return;

        dtmChanged();
    }

    public static void appClosing() {
        if (!currentlyTracking)
            return;

        dtmChanged();
    }

    public static void appSuspending() {
        if (!currentlyTracking)
            return;

        dtmChanged();
    }

    public static void stopRefreshClicked(long sessionID) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.StopRefreshEntry stopRefreshEntry = new DataTrackingModel.StopRefreshEntry();
        stopRefreshEntry.entryTime = System.currentTimeMillis();
        stopRefreshEntry.sessionID = sessionID;
        dtm.pushToStopRefreshEntryHistory(stopRefreshEntry);

        dtmChanged();
    }

    public static void setDTM(DataTrackingModel _dtm) {
        dtm = _dtm;
        dtmChanged();
    }

    public static DataTrackingModel getDTM() {
        return dtm;
    }

    public static void forgetLessonsClicked(long sessionID) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.ForgetLessonsEntry forgetLessonsEntry = new DataTrackingModel.ForgetLessonsEntry();
        forgetLessonsEntry.entryTime = System.currentTimeMillis();
        forgetLessonsEntry.sessionID = sessionID;
        dtm.pushToForgetLessonsHistory(forgetLessonsEntry);

        dtmChanged();
    }

    public static DataTrackingModel mergeDtms(DataTrackingModel dtm1, DataTrackingModel dtm2) {
        Log.e("Stuff", "DTMs merging...");

        DataTrackingModel earliestDtm = dtm2;
        DataTrackingModel latestDtm = dtm1;
        if (dtm1.getTimeAppFirstStarted() < dtm2.getTimeAppFirstStarted()) { // prefer the base data in the earliest version
            earliestDtm = dtm1;
            latestDtm = dtm2;
        }

        earliestDtm.getAvatarHistory().addAll(latestDtm.getAvatarHistory());
        earliestDtm.getLessonHistory().addAll(latestDtm.getLessonHistory());
        earliestDtm.getForgetLessonsHistory().addAll(latestDtm.getForgetLessonsHistory());
        earliestDtm.getNotificationClickHistory().addAll(latestDtm.getNotificationClickHistory());
        earliestDtm.getPageHistory().addAll(latestDtm.getPageHistory());
        earliestDtm.getAppUseHistory().addAll(latestDtm.getAppUseHistory());

        return earliestDtm;
    }

    public static void unlockUpload() {
        uploadLocked = false;
    }
}
