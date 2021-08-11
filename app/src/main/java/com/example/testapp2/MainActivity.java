package com.example.testapp2;

import androidx.lifecycle.ProcessLifecycleOwner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;
import com.example.testapp2.ui.gallery.LessonListFragment;
import com.example.testapp2.ui.home.HomeFragment;
import com.example.testapp2.ui.lesson.LessonFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp2.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static NavController navController;
    AppLifeCycleObserver appLifeCycleObserver;

    boolean firstTimeAppUse = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Custom here
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifeCycleObserver); // For detecting when app is moved between foreground and background, etc

        firstTimeAppUse = !HelperCode.getSharedPrefsObj(getApplicationContext()).contains(GlobalVars.APP_INSTALLED_PREF_KEY);
        if (firstTimeAppUse)
            HelperCode.getSharedPrefsObj(getApplicationContext()).edit().putBoolean(GlobalVars.APP_INSTALLED_PREF_KEY, true).apply();
        AvatarSelectFragment.prepareAvatarData(getApplicationContext());

        String participantID = HelperCode.getSharedPrefsObj(getApplicationContext()).getString(GlobalVars.PARTICIPANT_ID_PREF_KEY, "");
        if (participantID.equals("")) {
            Intent prevIntent = getIntent();
            participantID = prevIntent.getStringExtra("participantID");
            if (participantID != null) {
                DataTrackingManager.startTracking(getApplicationContext(), participantID);
            }
        }
        else {
            DataTrackingManager.startTracking(getApplicationContext(), participantID);
        }

        // if any of these are true, then it will have to pass over Home to go to the right place, but the user will not actually be seeing Home, so it should not be recorded
        Intent prevIntent = getIntent();
        if (prevIntent != null && prevIntent.getStringExtra("objectFound") != null ||
                !HelperCode.getSharedPrefsObj(getApplicationContext()).contains(GlobalVars.AVATAR_NAME_PREF_KEY) ||
                !HelperCode.getSharedPrefsObj(getApplicationContext()).contains(GlobalVars.PARTICIPANT_ID_PREF_KEY))
        {
            HomeFragment.passOverFragment();
        }

        // End custom

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_lesson, R.id.nav_learnMore, R.id.nav_avatarSelect)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // My addition
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull @NotNull NavController controller, @NonNull @NotNull NavDestination destination, @Nullable @org.jetbrains.annotations.Nullable Bundle arguments) {
//                Log.w("Stuff", "Controller: " + controller.toString() + ", destination: " + destination.getLabel());

                if (destination.getLabel().equals(getResources().getString(R.string.menu_gallery_bookmarked))) {
//                    Log.e("Stuff", "Now in bookmark section");
                    LessonListFragment.trySetBookmarkFilter(true);
                }
                else if (destination.getLabel().equals(getResources().getString(R.string.menu_gallery))) {
//                    Log.e("Stuff", "Now in section: " + destination.getLabel());
                    LessonListFragment.trySetBookmarkFilter(false);
                }
            }
        });

        // End my addition
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // ^^^ Above is the default code

        checkForStartupSettings(getApplicationContext(), MainActivity.this);

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra("objectFound") != null) { // This is true if you got here from clicking a notification
//            Log.e("Stuff", "Should send to lesson now");
            long myImageID = intent.getLongExtra("myImageID", -1);
            long sessionID = intent.getLongExtra("sessionID", -1);
            String notificationID = intent.getStringExtra("notificationID");
            Log.e("Stuff", "Notif id found in intent: " + notificationID);
            if (notificationID.equals("")) {
                Log.e("Stuff", "No notificationID found in intent");
            }
            if (myImageID == -1) {
//                throw new RuntimeException("Did not find proper image ID in notification intent");
                Log.e("Stuff", "Something apparently really bad: Did not find proper image ID in notification intent");
            }

            MyImage curImage = LessonListFragment.getMyImageByID(getApplicationContext(), myImageID);
            LessonFragment lessonFragment = new LessonFragment();
            lessonFragment.setLessonData(curImage);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main_layout, lessonFragment, "fragmentTag")
                    .addToBackStack(null)
                    .commit();

            // TODO: Fix; this line causes a crash (apparently DataTrackingManager is null???)
            // Okay it's because the DTM hasn't loaded yet; it's too quick
            // TODO: Need to make app work offline -- Fixed? At least for DTM stuff maybe
            DataTrackingManager.notificationClicked(notificationID);
        }

        FirebaseManager.updateFirestoreObjectLessons(getApplicationContext());
    }

    public static void checkForStartupSettings(Context context, Activity activity) {
        // if ID hasn't been chosen yet, chose it now
        if (!HelperCode.getSharedPrefsObj(context).contains(GlobalVars.PARTICIPANT_ID_PREF_KEY)) {
            navController.navigate(R.id.nav_signIn);
        }
        // if avatar hasn't been chosen yet, chose it now
        else if (!HelperCode.getSharedPrefsObj(context).contains(GlobalVars.AVATAR_NAME_PREF_KEY)) {
            navController.navigate(R.id.nav_avatarSelect);
        }
        // Make sure we have the right permissions to access photos in  the background
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(context, "Please allow access to continue", Toast.LENGTH_LONG).show();
                HelperCode.callInSeconds(context, () -> askForExternalStoragePermission(context), 2);
            }
        }
        else if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (activity.shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            Toast.makeText(context, "Please allow access to continue", Toast.LENGTH_LONG).show();
            HelperCode.callInSeconds(context, () -> activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GlobalVars.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE), 2);
            ;

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
        }
        // if this is the first time using the app, show the App Info page
        else if (!HelperCode.getSharedPrefsObj(context).contains(GlobalVars.APP_INFO_SHOWN)) {
            HelperCode.getSharedPrefsObj(context).edit().putBoolean(GlobalVars.APP_INFO_SHOWN, true).apply();
            navController.navigate(R.id.nav_on_boarding);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static void askForExternalStoragePermission(Context context) {
        String appID = BuildConfig.APPLICATION_ID;
        Uri uri = Uri.parse("package:"+appID);

        context.startActivity(
                new Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                )
        );
    }

    // TODO: this; need to find this class implementation online: https://stackoverflow.com/questions/5593115/run-code-when-android-app-is-closed-sent-to-background
//    @Override
//    public void onAppForegroundStateChange(AppForegroundStateManager.AppForegroundState newState) {
//        if (AppForegroundStateManager.AppForegroundState.IN_FOREGROUND == newState) {
//            // App just entered the foreground. Do something here!
//        } else {
//            // App just entered the background. Do something here!
//        }
//    }
}