package com.example.testapp2.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.FirebaseManager;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.LessonListRecyclerViewAdapter;
import com.example.testapp2.MainActivity;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.MyImage;
import com.example.testapp2.ObjectLesson;
import com.example.testapp2.databinding.FragmentLessonListBinding;

import java.util.ArrayList;
import java.util.Random;

public class LessonListFragment extends Fragment implements MyFragmentInterface {

    private LessonListViewModel galleryViewModel;
    private FragmentLessonListBinding binding;

    RecyclerView recyclerView;
    static SharedPreferences sharedPreferences;
    static boolean onlyBookmarks = false;
    ArrayList<MyImage> myImages = new ArrayList<>();
    ArrayList<ObjectLesson> objectLessons = new ArrayList<>();

    Context context;
    String sessionID = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(LessonListViewModel.class);

        binding = FragmentLessonListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ^^^ Above is default
        context = getActivity();
        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();
        notifyTrackerOfPage();

        binding.bookmarkSwitch.setChecked(onlyBookmarks);

        recyclerView = binding.imageRecyclerView;
        initRecyclerView(context, getActivity().getSupportFragmentManager(), onlyBookmarks);

        binding.backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.bookmarkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchBookmarkFilter(isChecked);
            }
        });

        binding.forgetLessons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataTrackingManager.forgetLessonsClicked(sessionID);
                LessonListFragment.clearImageHistory(context);
                initRecyclerView(context, getActivity().getSupportFragmentManager(), onlyBookmarks);
                // TODO: make page refresh immediately when pressed (right now must, navigate to a different page then back to remove lessons from view)
            }
        });

        FirebaseManager.updateFirestoreObjectLessons(context);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void initArrayList(Context context, boolean onlyBookmarks) {
//        objectNames.clear();
//        imageUriStrings.clear();

        myImages.clear();
        objectLessons.clear();

        ArrayList<MyImage> entireHistory = getImageHistory(context);

        for (MyImage mi : entireHistory) {
            if (!onlyBookmarks || mi.bookmarked) {
//                objectNames.add(mi.objectDetected);
//                imageUriStrings.add(mi.uriString);
                myImages.add(mi);
                objectLessons.add(FirebaseManager.getFirestoreObjectData(mi.objectDetected));
            }
        }

    }

    public void switchBookmarkFilter(boolean onlyBookmarks) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        initRecyclerView(context, fragmentManager, onlyBookmarks);
    }

    void initRecyclerView(Context context, FragmentManager fragmentManager, boolean onlyBookmarks) {
        initArrayList(context, onlyBookmarks);
//        Log.w("Stuff", "initRecyclerView; count: " + myImages.size());
        LessonListRecyclerViewAdapter adapter = new LessonListRecyclerViewAdapter(myImages, objectLessons, fragmentManager, context);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        // TODO: Figure out why this sometimes does not flip it?? Right now consistently doesn't work
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(llm);
    }

    public static void addMyImage(Context context, MyImage mi) {
        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }

        ArrayList<MyImage> entireHistory = getImageHistory(context);
//        ArrayList<MyImage> entireHistory = new ArrayList<>();
        entireHistory.add(mi);

        setImageHistory(entireHistory);
    }

    public static void setImageBookmark(Context context, long imageID, boolean bookmarked) {
        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }

        boolean found = false;
        ArrayList<MyImage> entireHistory = getImageHistory(context);
        for (MyImage mi : entireHistory) {
            if (mi.imageID == imageID) {
                mi.bookmarked = bookmarked;
                found = true;
                break;
            }
        }
        if (!found)
            Log.e("Stuff", "No image found to bookmark with that ID!");

        setImageHistory(entireHistory);
    }

    public static void setImageHistory(ArrayList<MyImage> entireHistory) {
        sharedPreferences.edit().putString(GlobalVars.IMAGE_HISTORY_PREF_KEY, HelperCode.arrayListToJson(entireHistory)).apply();
    }

    public static ArrayList<MyImage> getImageHistory(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }

        return HelperCode.jsonToMyImageArrayList(sharedPreferences.getString(GlobalVars.IMAGE_HISTORY_PREF_KEY, HelperCode.arrayListToJson(new ArrayList<MyImage>())));
    }

    public static void clearImageHistory(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }

        sharedPreferences.edit().remove(GlobalVars.IMAGE_HISTORY_PREF_KEY).apply();
    }

    public static MyImage getMyImageByID(Context context, long imageID) {
        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }

        ArrayList<MyImage> entireHistory = getImageHistory(context);
        for (MyImage mi : entireHistory) {
            if (mi.imageID == imageID) {
                return mi;
            }
        }

        Log.e("Stuff", "That imageID was not found");
        throw new RuntimeException("imageID not found");
    }

    public static void setThisFactIndex(Context context, long imageID) {
        if (!FirebaseManager.firestoreObjectLessonsGathered()) {
            HelperCode.callInSeconds(context, () -> setThisFactIndex(context, imageID), 0.5);
            return;
        }

        if (sharedPreferences == null) {
            sharedPreferences = HelperCode.getSharedPrefsObj(context);
        }


        ArrayList<MyImage> entireHistory = getImageHistory(context);

        MyImage mi = null;
        for (MyImage myImage : entireHistory) {
            if (myImage.imageID == imageID) {
                mi = myImage;
            }
        }

        ObjectLesson objectLesson = FirebaseManager.getFirestoreObjectData(mi.objectDetected);

        if (objectLesson == null) {
            Log.e("Stuff", "Trying to set this fact index again");
            HelperCode.callInSeconds(context, () -> setThisFactIndex(context, imageID), 0.25);
            return;
        }

        int lessonFactIndex = Math.abs((new Random()).nextInt()) % objectLesson.getObjectFacts().size();
        mi.lessonData.thisFactIndex = lessonFactIndex
        ;
        String lessonFact = objectLesson.getObjectFacts().get(lessonFactIndex);
        DataTrackingManager.lessonFactAssigned(mi.lessonData.lessonID, lessonFact);

        setImageHistory(entireHistory);
    }

    public static void trySetBookmarkFilter(boolean _onlyBookmarks) {
        onlyBookmarks = _onlyBookmarks;
    }

    public void setSessionID(String _sessionID) {
        sessionID = _sessionID;
    }

    @Override
    public void notifyTrackerOfPage() {
        DataTrackingManager.pageChange(this, sessionID, "LessonList");
    }
}