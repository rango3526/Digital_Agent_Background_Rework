package com.example.testapp2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;
import com.example.testapp2.ui.gallery.LessonListFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Custom here
        AvatarSelectFragment.prepareAvatarData(getApplicationContext());
        DataTrackingManager.startTracking(getApplicationContext());
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

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

        // if avatar hasn't been chosen yet, chose it now
        if (!HelperCode.getSharedPrefsObj(getApplicationContext()).contains(GlobalVars.AVATAR_NAME_PREF_KEY))
            navController.navigate(R.id.nav_avatarSelect);

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra("objectFound") != null) { // This is true if you got here from clicking a notification
//            Log.e("Stuff", "Should send to lesson now");
            long myImageID = intent.getLongExtra("myImageID", -1);
            long sessionID = intent.getLongExtra("sessionID", -1);
            if (myImageID == -1) {
                throw new RuntimeException("Did not find proper image ID in notification intent");
            }
            MyImage curImage = LessonListFragment.getMyImageByID(getApplicationContext(), myImageID);
            LessonFragment lessonFragment = new LessonFragment();
            lessonFragment.setLessonData(curImage);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main_layout, lessonFragment, "fragmentTag")
                    .addToBackStack(null)
                    .commit();

            DataTrackingManager.notificationClicked(sessionID, curImage, FirebaseManager.getFirestoreObjectData(curImage.objectDetected));
        }

        FirebaseManager.updateFirestoreObjectLessons();
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