package com.example.testapp2;

import android.content.Context;
import android.content.Intent;
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

    public static void notificationSent(long notificationID, long sessionID, Intent intent) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.NotificationEntry notificationEntry = new DataTrackingModel.NotificationEntry();
        notificationEntry.clickTime = -1;
        notificationEntry.leadsToSessionID = sessionID;
        notificationEntry.notificationID = notificationID;
        notificationEntry.objectDetected = intent.getStringExtra("objectFound");
        notificationEntry.sentTime = System.currentTimeMillis();
        dtm.pushToNotificationClickedHistory(notificationEntry);

        dtmChanged();
    }

    public static void notificationClicked(long notificationID) {
        if (!currentlyTracking)
            return;

        ArrayList<DataTrackingModel.NotificationEntry> notificationHistory = dtm.getNotificationClickHistory();

        boolean notifEntryFound = false;
        for (DataTrackingModel.NotificationEntry notificationEntry : notificationHistory) {
            if (notificationEntry.notificationID == notificationID) {
                notificationEntry.clickTime = System.currentTimeMillis();
                notifEntryFound = true;
            }
        }

        // this is expected, since the DB most certainly won't have time to connect before processing this
        if (!notifEntryFound) {
            Log.e("Stuff", "No notif entry found; making a placeholder");
            DataTrackingModel.NotificationEntry notificationEntry = new DataTrackingModel.NotificationEntry();
            notificationEntry.clickTime = System.currentTimeMillis();
            notificationEntry.sentTime = -1;
            notificationEntry.notificationID = notificationID;
            dtm.pushToNotificationClickedHistory(notificationEntry);
            // TODO: handle this by making a new entry and making it merge when connection is established -- DONE, but bugged
            // this is a likely error because right when the notification is clicked this will run, so app won't have time to query firebase (if app was closed)
        }

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
        // This is made to be used for merging a temporary (offline) dtm with one that is found when the firebase query is complete
        // For example, when the app opens each time and must record data before firebase can be queried
        Log.e("Stuff", "DTMs merging...");

        DataTrackingModel earliestDtm = dtm2;
        DataTrackingModel latestDtm = dtm1;
        if (dtm1.getTimeAppFirstStarted() < dtm2.getTimeAppFirstStarted()) { // prefer the base data in the earliest version
            earliestDtm = dtm1;
            latestDtm = dtm2;
        }

        // Simply add all the histories together
        earliestDtm.getAvatarHistory().addAll(latestDtm.getAvatarHistory());
        earliestDtm.getLessonHistory().addAll(latestDtm.getLessonHistory());
        earliestDtm.getForgetLessonsHistory().addAll(latestDtm.getForgetLessonsHistory());
        earliestDtm.getNotificationClickHistory().addAll(latestDtm.getNotificationClickHistory());
        earliestDtm.getPageHistory().addAll(latestDtm.getPageHistory());
        earliestDtm.getAppUseHistory().addAll(latestDtm.getAppUseHistory());

        // Handle unmatched duplicate entries in notifications
        ArrayList<DataTrackingModel.NotificationEntry> unmatchedClicked = new ArrayList<>();
        ArrayList<DataTrackingModel.NotificationEntry> unmatchedSent = new ArrayList<>();
        for (DataTrackingModel.NotificationEntry notificationEntry : earliestDtm.getNotificationClickHistory()) {
            if (notificationEntry.clickTime == -1) {
                unmatchedSent.add(notificationEntry);
                continue;
            }
            if (notificationEntry.sentTime == -1) {
                unmatchedClicked.add(notificationEntry);
            }
        }

        if (unmatchedClicked.size() > 0) {
            if (unmatchedSent.size() > 0) {
                for (DataTrackingModel.NotificationEntry clicked : unmatchedClicked) {
                    boolean handled = false;
                    for (DataTrackingModel.NotificationEntry sent : unmatchedSent) {
                        if (sent.notificationID == clicked.notificationID) {
                            sent.clickTime = clicked.clickTime;
                            if (!earliestDtm.getNotificationClickHistory().remove(clicked)) {
                                Log.e("Stuff", "Unable to removed duplicated notification click entry");
                            }
                            else {
                                Log.e("Stuff", "Merged notification successfully");
                            }
                            handled = true;
                            break;
                        }
                        else {
                            Log.e("Stuff", "Not a match; sent: " + sent.notificationID + ", clicked: " + clicked.notificationID);
                        }
                    }
                    if (!handled) {
                        if (!earliestDtm.getNotificationClickHistory().remove(clicked)) {
                            Log.e("Stuff", "Unable to remove duplicated (and NONMATCHED) notification click entry");
                        }
                        else {
                            Log.e("Stuff", "Unable to find match for clicked notification, so it has been deleted (successfully)");
                        }
                    }
                }
            }
            else {
                Log.e("Stuff", "Can't match any unmatched clicked notifs because there are no unmatched sent notifs!");
            }
        }

//        for (DataTrackingModel.NotificationEntry clicked : unmatchedClicked) {
//            if (!dtm.getNotificationClickHistory().remove(clicked)) {
//                Log.e("Stuff", "COULD NOT REMOVE FROM HISTORY");
//            }
//        }

        return earliestDtm;
    }

    public static void lockUpload() {
        uploadLocked = true;
    }

    public static void unlockUpload() {
        uploadLocked = false;
    }
}
