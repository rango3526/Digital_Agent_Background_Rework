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

    private static MyFragmentInterface mostRecentPage;

    private static Context context;

    // TODO: cache DTM in shared preferences

    public static void startTracking(Context _context, String participantID) {
        context = _context;
        HelperCode.getSharedPrefsObj(context).edit().putString(GlobalVars.PARTICIPANT_ID_PREF_KEY, participantID).apply();
        FirebaseManager.populateManagerDTM(context, participantID);
        currentlyTracking = true;
    }

    public static void stopTracking() {
        currentlyTracking = false;
    }

    public static void dtmChanged(Context context) {
        if (!uploadLocked)
            FirebaseManager.uploadDTM(context, dtm);
    }

    public static void pageChange(MyFragmentInterface obj, String sessionID, String pageName, String lessonID) {
        if (!currentlyTracking)
            return;

        pageChange(obj, sessionID, String.valueOf(System.currentTimeMillis()), "", pageName, lessonID);
    }

    public static void pageChange(MyFragmentInterface obj, String sessionID, String pageName) {
        if (!currentlyTracking)
            return;

        pageChange(obj, sessionID, String.valueOf(System.currentTimeMillis()), "", pageName, "");
    }

    public static void pageChange(MyFragmentInterface obj, String sessionID, String entryTime, String millisecondsOnPage, String pageName, String lessonID) {
        if (!currentlyTracking)
            return;

        // TODO: MAJOR Fix this non-ideal solution. Because I don't want to have to put this for everything that updates the dtm
        // For example, if the user installs the app without wifi for a long time, then the app will crash
        // TODO: Unknown behavior all the time without internet connection

        mostRecentPage = obj;

        if (dtm == null) { // If the dtm hasn't been created (so the system hasn't had time to connect to firebase yet), it will just keep waiting until it does
            Log.e("Stuff", "Waiting 1 second and trying again");
            HelperCode.callInSeconds(context, () -> pageChange(obj, sessionID, entryTime, millisecondsOnPage, pageName, lessonID), 1);
            return;
        }
        Log.e("Stuff", "Page change info being added to DTM");

        finishPrevPage();

        DataTrackingModel.PageEntry pe = new DataTrackingModel.PageEntry();
        pe.entryTime = entryTime;
        pe.endTime = millisecondsOnPage;
        pe.pageName = pageName;
        pe.sessionID = sessionID;
        pe.lessonID = lessonID;
        dtm.pushToPageHistory(pe);

        dtmChanged(context);
    }

    public static void avatarChosen(String avatarName) {
        if (!currentlyTracking)
            return;

        finishPrevAvatar();

        DataTrackingModel.AvatarEntry avatarEntry = new DataTrackingModel.AvatarEntry();
        avatarEntry.avatarName = avatarName;
        avatarEntry.entryTime = String.valueOf(System.currentTimeMillis());
        avatarEntry.endTime = "";
        dtm.pushToAvatarHistory(avatarEntry);

        dtmChanged(context);
    }

    public static void lessonBookmarked(String lessonID, boolean isNowBookmarked) { // TODO: actually call this function, requires rework of the way Lessons get assigned sessionIDs
        if (!currentlyTracking)
            return;

        DataTrackingModel.LessonEntry.BookmarkEntry bookmarkEntry = new DataTrackingModel.LessonEntry.BookmarkEntry();
        bookmarkEntry.entryTime = String.valueOf(System.currentTimeMillis());
        bookmarkEntry.isNowBookmarked = isNowBookmarked;

        // TODO: this is inefficient, but fixing this means reorganizing the entire database structure. Not much data, so probably not worth it.
        ArrayList<DataTrackingModel.LessonEntry> lessonHistory = dtm.getLessonsDiscovered();
        for (DataTrackingModel.LessonEntry lessonEntry : lessonHistory) {
            if (lessonEntry.lessonID.equals(lessonID)) {
                lessonEntry.bookmarkHistory.add(bookmarkEntry);
                break;
            }
        }

        dtmChanged(context);
    }

    public static void lessonFactAssigned(String lessonID, String fact) {
        // No check for currently tracking because this is just updating a previously tracked item
        //
        // The only reason this is needed is because this fact isn't assigned until the lesson is viewed,
        // but it could've been assigned any time (like at its "discovery")

        ArrayList<DataTrackingModel.LessonEntry> lessonHistory = dtm.getLessonsDiscovered();

        for (DataTrackingModel.LessonEntry lessonEntry : lessonHistory) {
            if (lessonEntry.lessonID.equals(lessonID)) {
                lessonEntry.fact = fact;
                break;
            }
        }

        dtmChanged(context);
    }

    public static void lessonDiscovered(String lessonID, String objectDetected) { // when the system finds this object in a photo
        if (!currentlyTracking)
            return;

        // TODO: prevent discovery of same lesson multiple times? Maybe??

        DataTrackingModel.LessonEntry lessonEntry = new DataTrackingModel.LessonEntry();
        lessonEntry.bookmarkHistory = new ArrayList<>();
        lessonEntry.discoverTime = String.valueOf(System.currentTimeMillis());
        lessonEntry.objectDetected = objectDetected;
        lessonEntry.lessonID = lessonID;
        dtm.pushToLessonsDiscovered(lessonEntry);

        dtmChanged(context);
    }

    public static void notificationSent(String notificationID, String sessionID, Intent intent) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.NotificationEntry notificationEntry = new DataTrackingModel.NotificationEntry();
        notificationEntry.clickTime = "";
        notificationEntry.leadsToSessionID = sessionID;
        notificationEntry.notificationID = notificationID;
        notificationEntry.objectDetected = intent.getStringExtra("objectFound");
        notificationEntry.sentTime = String.valueOf(System.currentTimeMillis());
        dtm.pushToNotificationClickedHistory(notificationEntry);

        dtmChanged(context);
    }

    public static void notificationClicked(String notificationID) {
        if (!currentlyTracking)
            return;

        ArrayList<DataTrackingModel.NotificationEntry> notificationHistory = dtm.getNotificationClickHistory();

        boolean notifEntryFound = false;
        for (DataTrackingModel.NotificationEntry notificationEntry : notificationHistory) {
            if (notificationEntry.notificationID.equals(notificationID)) {
                notificationEntry.clickTime = String.valueOf(System.currentTimeMillis());
                notifEntryFound = true;
            }
        }

        // this is expected, since the DB most certainly won't have time to connect before processing this
        if (!notifEntryFound) {
            Log.e("Stuff", "No notif entry found; making a placeholder");
            DataTrackingModel.NotificationEntry notificationEntry = new DataTrackingModel.NotificationEntry();
            notificationEntry.clickTime = String.valueOf(System.currentTimeMillis());
            notificationEntry.sentTime = "";
            notificationEntry.notificationID = notificationID;
            dtm.pushToNotificationClickedHistory(notificationEntry);
        }

        dtmChanged(context);
    }

    public static void appMovedToForeground() {
        if (!currentlyTracking)
            return;

        appMovedToForeground(String.valueOf(System.currentTimeMillis()));
    }

    private static void appMovedToForeground(String entryTime) {
        if (!currentlyTracking)
            return;

        if (dtm == null) {
            Log.e("Stuff", "Trying foreground again");
            HelperCode.callInSeconds(context, () -> appMovedToForeground(entryTime), 1);
            return;
        }

        Log.e("Stuff", "Foreground successful");
        DataTrackingModel.AppUseEntry appUseEntry = new DataTrackingModel.AppUseEntry();
        appUseEntry.entryTime = entryTime;
        dtm.pushToAppUseHistory(appUseEntry);

        reinitializeTrackingOfCurrentPage();

        dtmChanged(context);
    }

    private static void reinitializeTrackingOfCurrentPage() {
        if (mostRecentPage != null) {
            mostRecentPage.notifyTrackerOfPage();
            Log.e("Stuff", "Re-notified of: " + mostRecentPage.getClass().getName());
        }
    }

    public static void appMovedToBackground() {
        if (!currentlyTracking)
            return;

        appMovedToBackground(String.valueOf(System.currentTimeMillis()));
    }

    public static void appMovedToBackground(String entryTime) {
        if (!currentlyTracking)
            return;

        if (dtm == null) {
            Log.e("Stuff", "Trying foreground again");
            HelperCode.callInSeconds(context, () -> appMovedToBackground(entryTime), 1);
            return;
        }

        finishPrevPage();
        finishPrevAvatar();
        finishPrevAppUse();

        dtmChanged(context);
    }

    public static void stopRefreshClicked(String sessionID) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.StopRefreshEntry stopRefreshEntry = new DataTrackingModel.StopRefreshEntry();
        stopRefreshEntry.entryTime = String.valueOf(System.currentTimeMillis());
        stopRefreshEntry.sessionID = sessionID;
        dtm.pushToStopRefreshEntryHistory(stopRefreshEntry);

        dtmChanged(context);
    }

    public static void setDTM(DataTrackingModel _dtm) {
        dtm = _dtm;
        dtmChanged(context);
    }

    public static DataTrackingModel getDTM() {
        return dtm;
    }

    public static void forgetLessonsClicked(String sessionID) {
        if (!currentlyTracking)
            return;

        DataTrackingModel.ForgetLessonsEntry forgetLessonsEntry = new DataTrackingModel.ForgetLessonsEntry();
        forgetLessonsEntry.entryTime = String.valueOf(System.currentTimeMillis());
        forgetLessonsEntry.sessionID = sessionID;
        dtm.pushToForgetLessonsHistory(forgetLessonsEntry);

        dtmChanged(context);
    }

    public static void lessonInterestingClick(String lessonID, boolean interesting) {
        if (!currentlyTracking)
            return;

        Log.e("Stuff", "Yes interesting, lessonID: " + lessonID);
        for (DataTrackingModel.LessonEntry lessonEntry : dtm.getLessonsDiscovered()) {
            if (lessonEntry.lessonID.equals(lessonID)) {
                lessonEntry.foundInteresting = interesting ? "yes" : "no";
            }
        }

        dtmChanged(context);
    }

    public static void finishPrevPage() {
        if (dtm.getPageHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getPageHistory().size()-1;
            DataTrackingModel.PageEntry prevPageEntry = dtm.getPageHistory().get(index);
            if (prevPageEntry.endTime.equals(""))
                prevPageEntry.endTime = String.valueOf(System.currentTimeMillis());
        }
    }

    public static void finishPrevAvatar() {
        if (dtm.getAvatarHistory().size() > 0) { // Since prev is ending, set its duration in tracking data
            int index = dtm.getAvatarHistory().size()-1;
            DataTrackingModel.AvatarEntry prevAvatarEntry = dtm.getAvatarHistory().get(index);
            if (prevAvatarEntry.endTime.equals(""))
                prevAvatarEntry.endTime = String.valueOf(System.currentTimeMillis());
        }
    }

    public  static void finishPrevAppUse() {
        if (dtm.getAppUseHistory().size() > 0) {
            int index = dtm.getAppUseHistory().size()-1;
            DataTrackingModel.AppUseEntry appUseEntry = dtm.getAppUseHistory().get(index);
            if (appUseEntry.exitTime.equals(""))
                appUseEntry.exitTime = String.valueOf(System.currentTimeMillis());
        }
    }

    public static DataTrackingModel mergeDtms(DataTrackingModel dtm1, DataTrackingModel dtm2) {
        // This is made to be used for merging a temporary (offline) dtm with one that is found when the firebase query is complete
        // For example, when the app opens each time and must record data before firebase can be queried
        Log.e("Stuff", "DTMs merging...");

        DataTrackingModel earliestDtm = dtm2;
        DataTrackingModel latestDtm = dtm1;
        if (Long.parseLong(dtm1.getTimeAppFirstStarted()) < Long.parseLong(dtm2.getTimeAppFirstStarted())) { // prefer the base data in the earliest version
            earliestDtm = dtm1;
            latestDtm = dtm2;
        }

        // Simply add all the histories together
        earliestDtm.getAvatarHistory().addAll(latestDtm.getAvatarHistory());
        earliestDtm.getLessonsDiscovered().addAll(latestDtm.getLessonsDiscovered());
        earliestDtm.getForgetLessonsHistory().addAll(latestDtm.getForgetLessonsHistory());
        earliestDtm.getNotificationClickHistory().addAll(latestDtm.getNotificationClickHistory());
        earliestDtm.getPageHistory().addAll(latestDtm.getPageHistory());
        earliestDtm.getAppUseHistory().addAll(latestDtm.getAppUseHistory());

        // Handle unmatched duplicate entries in notifications
        ArrayList<DataTrackingModel.NotificationEntry> unmatchedClicked = new ArrayList<>();
        ArrayList<DataTrackingModel.NotificationEntry> unmatchedSent = new ArrayList<>();
        for (DataTrackingModel.NotificationEntry notificationEntry : earliestDtm.getNotificationClickHistory()) {
            if (notificationEntry.clickTime.equals("")) {
                unmatchedSent.add(notificationEntry);
                continue;
            }
            if (notificationEntry.sentTime.equals("")) {
                unmatchedClicked.add(notificationEntry);
            }
        }

        if (unmatchedClicked.size() > 0) {
            if (unmatchedSent.size() > 0) {
                for (DataTrackingModel.NotificationEntry clicked : unmatchedClicked) {
                    boolean handled = false;
                    for (DataTrackingModel.NotificationEntry sent : unmatchedSent) {
                        if (sent.notificationID.equals(clicked.notificationID)) {
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
