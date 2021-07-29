package com.example.testapp2.ui.lesson;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.FirebaseManager;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MainActivity;
import com.example.testapp2.MyImage;
import com.example.testapp2.ObjectLesson;
import com.example.testapp2.R;
import com.example.testapp2.databinding.FragmentLessonBinding;
import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;
import com.example.testapp2.ui.gallery.LessonListFragment;
import com.example.testapp2.ui.learnMore.LearnMoreFragment;

import java.util.Random;

public class LessonFragment extends Fragment {

    private LessonViewModel slideshowViewModel;
    private FragmentLessonBinding binding;

    FragmentManager fragmentManager;

    private MyImage mi;
    long sessionID = -1;

    Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(this).get(LessonViewModel.class);

        binding = FragmentLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        // ^^^ Default stuff
        context = getActivity();
        ObjectLesson ol = FirebaseManager.getFirestoreObjectData(mi.objectDetected);
        if (sessionID == -1)
            Log.e("Stuff", "This lesson needs a sessionID");
        DataTrackingManager.pageChange(sessionID, "Lesson");

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        fragmentManager = getActivity().getSupportFragmentManager();

        TextView textView = binding.avatarBeginLessonDialogue;
        ImageView image = binding.objectImage;
        image.setImageURI(Uri.parse(mi.uriString));
        textView.setText("Let's learn about that " + mi.objectDetected + "!");
        Log.w("Stuff", "STARTED LESSON LEARN ACTIVITY");
        binding.bookmarkToggle.setChecked(mi.bookmarked);

        binding.backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ol == null) {
                    Toast.makeText(context, "Check your internet connection and try again (restart app)", Toast.LENGTH_SHORT).show();
                    return;
                }

//                Intent intent = new Intent(context, LearnMoreFragment.class);
//                intent.putExtra("objectLessonHashmap", ol.getHashmapRepresentation());
//                intent.putExtra("myImageID", mi.imageID);
//                startActivity(intent);

                LearnMoreFragment learnMoreFragment = new LearnMoreFragment();
                learnMoreFragment.setData(ol, mi);
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main_layout, learnMoreFragment, "fragmentTag")
                        .addToBackStack(null)
                        .commit();

            }
        });

        binding.bookmarkToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LessonListFragment.setImageBookmark(context, mi.imageID, isChecked);
                DataTrackingManager.lessonBookmarked(mi.sessionID, isChecked);
                mi.bookmarked = isChecked;
            }
        });

        binding.avatarImageView.setImageURI(AvatarSelectFragment.getCurAvatar(getActivity()).getImageUri());

        DataTrackingManager.lessonOpened(sessionID, mi.objectDetected);
        DataTrackingManager.pageChange(sessionID, "Lesson");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setLessonData(MyImage _mi) {
        context = getActivity();
        mi = _mi;
        sessionID = mi.sessionID;
    }

//    public void setSessionID(long _sessionID) {
//        sessionID = _sessionID;
//    }
}