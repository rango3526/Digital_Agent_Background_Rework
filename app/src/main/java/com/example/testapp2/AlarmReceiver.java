package com.example.testapp2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.testapp2.ui.gallery.LessonListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

//    List<MyImage> allKnownImages;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            alarmTriggered(context);
        }
        catch (Exception e) {
            Log.w("Stuff", "Found exception: " + e.getMessage());
        }
    }

    // Setting up notification channel for whole app
    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MyNotifChannel";
            String description = "MyNotifChannelDescr";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(GlobalVars.NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void sendNotification(Context context, MyImage mi) {
        if (!FirebaseManager.firestoreObjectLessonsGathered()) {
            HelperCode.callInSeconds(context, () -> sendNotification(context, mi), 5);
            return;
        }

        String notificationID = HelperCode.generateLongID();
        String lessonID = mi.lessonData.lessonID;

        Intent intent = AlarmReceiver.getIntentForObjectLesson(notificationID, lessonID, context, mi);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (new Random().nextInt()), intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GlobalVars.NOTIF_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setContentTitle(" ")
                .setContentText("Time to learn science facts about that " + mi.objectDetected + "!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(new Random().nextInt(), builder.build());

        DataTrackingManager.notificationSent(notificationID, lessonID, intent);
    }


    private static List<MyImage> getAllImages(Context context) {
        // for preventing duplicates
        HashMap<Long, Integer> idTable = new HashMap<Long, Integer>();

        List<MyImage> imageList = new ArrayList<MyImage>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            //collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
        };
        //String selection = MediaStore.Images.Media.DURATION + " >= ?";
        String selection = null;
        String[] selectionArgs = new String[] {
            //String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))
        };
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        long startTime = SystemClock.elapsedRealtime();
        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

//            Log.w("MyImageBegin", "Before while");
            while (cursor.moveToNext()) {
//                Log.w("MyImageBegin", "In while");
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int size = cursor.getInt(sizeColumn);
                int dateTaken = Math.abs(cursor.getInt(dateTakenColumn));

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                if (!idTable.containsKey(id)) {
                    imageList.add(new MyImage(contentUri, name, size, dateTaken, "", id, null));
                    idTable.put(id, 1);
                }
//                Log.w("MyImage Stuff", contentUri.toString() + " " + name + " " + size + " " + dateTaken);
            }
        }
        catch (Exception e) {
            Log.w("MyImageError", "Caught error: " + e.getMessage());
        }
        long endTime = SystemClock.elapsedRealtime();

//        Log.w("Timing", "Time to find images: " + (endTime-startTime));

        return imageList;
    }

    private static void analyzeImages(Context context, List<MyImage> images) {
        for (MyImage mi : images) {
            Log.w("Stuff", "Analyzing new image: " + mi.fileName + " at " + mi.uriString);
            String result = MachineLearningManager.AnalyzeImage(context, Uri.parse(mi.uriString));
            if (FirebaseManager.firestoreObjectNameExists(result)) {
                mi.objectDetected = result;
                String lessonID = HelperCode.generateLongID(); // origin of each lessonID
                Log.e("Stuff", "Newly generated: " + lessonID);
                mi.lessonData = new LessonData(lessonID);
                LessonListFragment.addMyImage(context, mi);
                LessonListFragment.setThisFactIndex(context, mi.imageID);
                sendNotification(context, mi);
                DataTrackingManager.lessonDiscovered(lessonID, result);
            }
        }
    }

    public static void alarmTriggered(Context context) {
        if (!FirebaseManager.firestoreObjectLessonsGathered()) { // Needs to have this info to do a lot of things; this will prevent bugs
//            HelperCode.callInSeconds(context, () -> alarmTriggered(context), 5);
            // Probably no need to call callInSeconds since it's already on an alarm
            FirebaseManager.updateFirestoreObjectLessons(context);
            return;
        }

        new Thread(new Runnable() { // Executes in worker thread so it doesn't block the main/ui thread
            public void run() {
                long latestDate = 0;

                createNotificationChannel(context);
//        Toast.makeText(context, "Checking for new photos...", Toast.LENGTH_SHORT).show();

                List<MyImage> updatedImages = getAllImages(context);
                List<MyImage> newlyTaken = new ArrayList<>();

                SharedPreferences sharedPreferences = HelperCode.getSharedPrefsObj(context);
                latestDate = sharedPreferences.getLong(GlobalVars.LATEST_DATE_PREF_KEY, System.currentTimeMillis()/1000);

                long newLatestDate = latestDate;
                long maxNewLatestDate = latestDate;

                Log.e("Stuff", "Latest date: " + latestDate);

                // TODO: Optimize this by checking from latest to earliest, then stopping when appropriate (they are already sorted by date_taken in getAllImages(), though idk which direction)
                for (MyImage mi : updatedImages) {
                    if (mi.dateTaken > latestDate) {
                        newLatestDate = mi.dateTaken;
                        if (newLatestDate > maxNewLatestDate)
                            maxNewLatestDate = newLatestDate;
                        newlyTaken.add(mi);
//                        Log.e("Stuff", "Added new image taken at " + mi.dateTaken + " and latest was at: " + latestDate);
                    }
                }

                latestDate = maxNewLatestDate;
                sharedPreferences.edit().putLong(GlobalVars.LATEST_DATE_PREF_KEY, latestDate).apply();
                analyzeImages(context, newlyTaken);

                FirebaseManager.updateFirestoreObjectLessons(context);
            }
        }).start();
    }

    public static Intent getIntentForObjectLesson(String notificationID, String sessionID, Context context, MyImage mi) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("objectFound", mi.objectDetected);
        intent.putExtra("imageUri", mi.uriString);
        intent.putExtra("myImageID", mi.imageID);
        intent.putExtra("sessionID", sessionID);
        intent.putExtra("notificationID", notificationID);

        return intent;
    }
}