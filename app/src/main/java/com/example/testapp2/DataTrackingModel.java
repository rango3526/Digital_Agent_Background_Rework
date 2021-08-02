package com.example.testapp2;

import java.util.ArrayList;

public class DataTrackingModel {
    // All times are in milliseconds (since 12am January 1st, 1970, if relevant)

    public DataTrackingModel() {

    }

    private String participantID = "-1";
    private String participantName = "Firstname Lastname";
    private String timeAppFirstStarted = "";

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


    public String getTimeAppFirstStarted() {
        return timeAppFirstStarted;
    }

    public void setTimeAppFirstStarted(String timeAppFirstStarted) {
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

    public ArrayList<LessonEntry> getLessonsDiscovered() {
        return lessonsDiscovered;
    }

    public void setLessonsDiscovered(ArrayList<LessonEntry> lessonsDiscovered) {
        this.lessonsDiscovered = lessonsDiscovered;
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

    public ArrayList<NotificationEntry> getNotificationClickHistory() {
        return notificationClickHistory;
    }

    public void setNotificationClickHistory(ArrayList<NotificationEntry> notificationClickHistory) {
        this.notificationClickHistory = notificationClickHistory;
    }

    public void pushToPageHistory(PageEntry pageEntry) {
        pageHistory.add(pageEntry);
    }

    public void pushToAvatarHistory(AvatarEntry avatarEntry) {
        avatarHistory.add(avatarEntry);
    }

    public void pushToNotificationClickedHistory(NotificationEntry notificationClickEntry) {
        notificationClickHistory.add(notificationClickEntry);
    }

    public void pushToLessonsDiscovered(LessonEntry lessonEntry) {
        lessonsDiscovered.add(lessonEntry);
    }

    public void pushToStopRefreshEntryHistory(StopRefreshEntry stopRefreshEntry) {
        stopRefreshHistory.add(stopRefreshEntry);
    }

    public void pushToForgetLessonsHistory(ForgetLessonsEntry forgetLessonsEntry) {
        forgetLessonsHistory.add(forgetLessonsEntry);
    }

    public static class AvatarEntry {
        public String avatarName = "";
        public String endTime = "";
        public String entryTime = "";
    }
    private ArrayList<AvatarEntry> avatarHistory = new ArrayList<>();

    public static class PageEntry {
        public String pageName = "";
        public String sessionID = "";
        public String lessonID = "";
        public String endTime = "";
        public String entryTime = "";
    }
    private ArrayList<PageEntry> pageHistory = new ArrayList<>();

    public static class LessonEntry {
        public String objectDetected = "";
        public String lessonID = "";
        public String discoverTime = "";
        public String foundInteresting = "";

        public static class BookmarkEntry {
            public boolean isNowBookmarked = false;
            public String entryTime = "";
        }
        public ArrayList<BookmarkEntry> bookmarkHistory = new ArrayList<>();
    }
    private ArrayList<LessonEntry> lessonsDiscovered = new ArrayList<>();

    public static class StopRefreshEntry {
        public String entryTime = "";
        public String sessionID = "";
    }
    private ArrayList<StopRefreshEntry> stopRefreshHistory = new ArrayList<>();

    public static class ForgetLessonsEntry {
        public String entryTime = "";
        public String sessionID = "";
    }
    private ArrayList<ForgetLessonsEntry> forgetLessonsHistory = new ArrayList<>();

    public static class AppUseEntry {
        public String entryTime = "";
        public String exitTime = "";
        // TODO: Include something about suspending vs closing out completely
    }
    private ArrayList<AppUseEntry> appUseHistory = new ArrayList<>();

    public static class NotificationEntry {
        public String sentTime = "";
        public String clickTime = "";
        public String notificationID = "";
        public String leadsToSessionID = "";
        public String objectDetected = "";
    }
    private ArrayList<NotificationEntry> notificationClickHistory = new ArrayList<>();

    public ArrayList<ForgetLessonsEntry> getForgetLessonsHistory() {
        return forgetLessonsHistory;
    }

    public void setForgetLessonsHistory(ArrayList<ForgetLessonsEntry> forgetLessonsHistory) {
        this.forgetLessonsHistory = forgetLessonsHistory;
    }
}
