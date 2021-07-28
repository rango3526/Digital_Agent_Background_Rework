package com.example.testapp2;

import android.provider.Settings;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class DataTrackingModel {
    // All times are in milliseconds (since 12am January 1st, 1970, if relevant)

    public DataTrackingModel() {

    }

    private String participantID = "-1";
    private String participantName = "Firstname Lastname";
    private long timeAppFirstStarted = -1;

    public String getParticipantID() {
        return participantID;
    }

    public void setParticipantID(String participantID) {
        this.participantID = participantID;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }


    public long getTimeAppFirstStarted() {
        return timeAppFirstStarted;
    }

    public void setTimeAppFirstStarted(long timeAppFirstStarted) {
        this.timeAppFirstStarted = timeAppFirstStarted;
    }

    public ArrayList<AvatarEntry> getAvatarHistory() {
        return avatarHistory;
    }

    public void setAvatarHistory(ArrayList<AvatarEntry> avatarHistory) {
        this.avatarHistory = avatarHistory;
    }

    public ArrayList<PageEntry> getPageHistory() {
        return pageHistory;
    }

    public void setPageHistory(ArrayList<PageEntry> pageHistory) {
        this.pageHistory = pageHistory;
    }

    public ArrayList<LessonEntry> getLessonHistory() {
        return lessonHistory;
    }

    public void setLessonHistory(ArrayList<LessonEntry> lessonHistory) {
        this.lessonHistory = lessonHistory;
    }

    public ArrayList<StopRefreshEntry> getStopRefreshHistory() {
        return stopRefreshHistory;
    }

    public void setStopRefreshHistory(ArrayList<StopRefreshEntry> stopRefreshHistory) {
        this.stopRefreshHistory = stopRefreshHistory;
    }

    public ArrayList<AppUseEntry> getAppUseHistory() {
        return appUseHistory;
    }

    public void setAppUseHistory(ArrayList<AppUseEntry> appUseHistory) {
        this.appUseHistory = appUseHistory;
    }

    public ArrayList<NotificationClickEntry> getNotificationClickHistory() {
        return notificationClickHistory;
    }

    public void setNotificationClickHistory(ArrayList<NotificationClickEntry> notificationClickHistory) {
        this.notificationClickHistory = notificationClickHistory;
    }

    public void pushToPageHistory(PageEntry pageEntry) {
        pageHistory.add(pageEntry);
    }

    public void pushToAvatarHistory(AvatarEntry avatarEntry) {
        avatarHistory.add(avatarEntry);
    }

    public void pushToNotificationClickedHistory(NotificationClickEntry notificationClickEntry) {
        notificationClickHistory.add(notificationClickEntry);
    }

    public void pushToLessonHistory(LessonEntry lessonEntry) {
        lessonHistory.add(lessonEntry);
    }

    public void pushToStopRefreshEntryHistory(StopRefreshEntry stopRefreshEntry) {
        stopRefreshHistory.add(stopRefreshEntry);
    }

    public void pushToForgetLessonsHistory(ForgetLessonsEntry forgetLessonsEntry) {
        forgetLessonsHistory.add(forgetLessonsEntry);
    }

    public static class AvatarEntry {
        public String avatarName = "";
        public long millisecondsKept = -1;
        public long entryTime = -1;
    }
    private ArrayList<AvatarEntry> avatarHistory = new ArrayList<>();

    public static class PageEntry {
        public String pageName = "";
        public long sessionID = -1;
        public long millisecondsOnPage = -1;
        public long entryTime = -1;
    }
    private ArrayList<PageEntry> pageHistory = new ArrayList<>();

    public static class LessonEntry {
        public String objectDetected = "";
        public long sessionID = -1;
        public long millisecondsOnLesson = -1;
        public long entryTime = -1;

        public static class BookmarkEntry {
            public boolean isNowBookmarked = false;
            public long entryTime = -1;
        }
        public ArrayList<BookmarkEntry> bookmarkHistory = new ArrayList<>();
    }
    private ArrayList<LessonEntry> lessonHistory = new ArrayList<>();

    public static class StopRefreshEntry {
        public long entryTime = -1;
        public long sessionID = -1;
    }
    private ArrayList<StopRefreshEntry> stopRefreshHistory = new ArrayList<>();

    public static class ForgetLessonsEntry {
        public long entryTime = -1;
        public long sessionID = -1;
    }
    private ArrayList<ForgetLessonsEntry> forgetLessonsHistory = new ArrayList<>();

    public static class AppUseEntry {
        public long entryTime = -1;
        public long exitTime = -1;
        // TODO: Include something about suspending vs closing out completely
    }
    private ArrayList<AppUseEntry> appUseHistory = new ArrayList<>();

    public static class NotificationClickEntry {
        public long clickTime = -1;
        public long leadsToSessionID = -1;
        public String objectDetected = "";
    }
    private ArrayList<NotificationClickEntry> notificationClickHistory = new ArrayList<>();
}
