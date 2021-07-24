package com.example.testapp2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.testapp2.ui.lesson.LessonFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HelperCode {
    public static PendingIntent alarmPendingIntent;
    public static AlarmManager alarmManager;

    public static Bitmap GetBitmapFromUri(Context context, Uri imageUri) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        catch (FileNotFoundException e) {
            Log.e("MyError", "Got file not found exception here");
            e.printStackTrace();
        }

        return bitmap;
    }

    public static SharedPreferences getSharedPrefsObj(Context context) {
        return context.getSharedPreferences(GlobalVars.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public static <T> String arrayListToJson(ArrayList<T> arrayList) {
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);

//        Log.w("Stuff", "Json is: " + json);

        return json;
    }

    public static ArrayList<MyImage> reverseMyImageList(ArrayList<MyImage> al) {
        ArrayList<MyImage> reversedList = new ArrayList<>();
        int size = al.size();

        for (int i = 0; i < size; i++) {
            reversedList.add(al.get(size-i-1));
        }

        return reversedList;
    }

    public static ArrayList<MyImage> jsonToMyImageArrayList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<MyImage>>() {}.getType();

        ArrayList<MyImage> arrayList = gson.fromJson(json, type);

        if (arrayList == null)
            arrayList = new ArrayList<>();

        return arrayList;
    }

    public static Intent getIntentForObjectLesson(Context context, MyImage mi) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("objectFound", mi.objectDetected);
        intent.putExtra("imageUri", mi.uriString);
        intent.putExtra("myImageID", mi.imageID);

        return intent;
    }

    public static String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static void initializeAlarmManager(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static void setTestAlarm(Context context) {
        AlarmReceiver.alarmTriggered(context);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, 5000, alarmPendingIntent);
//        Toast.makeText(context,"I'll analyze your photos periodically in the background", Toast.LENGTH_SHORT).show();

        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void cancelAlarm(Context context) {
        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);

            ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            Toast.makeText(context,"Background refresh stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setAvatarNamePref(Context context, String name) {
        getSharedPrefsObj(context).edit().putString(GlobalVars.AVATAR_NAME_PREF_KEY, name).apply();
    }
}
