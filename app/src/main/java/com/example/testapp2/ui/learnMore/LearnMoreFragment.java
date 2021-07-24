package com.example.testapp2.ui.learnMore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.MainActivity;
import com.example.testapp2.MyImage;
import com.example.testapp2.ObjectLesson;
import com.example.testapp2.databinding.FragmentLearnMoreBinding;
import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;
import com.example.testapp2.ui.gallery.LessonListFragment;

public class LearnMoreFragment extends Fragment {

    private LearnMoreViewModel LearnMoreViewModel;
    private FragmentLearnMoreBinding binding;

    Context context;

    ObjectLesson ol;
    MyImage mi;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LearnMoreViewModel =
                new ViewModelProvider(this).get(LearnMoreViewModel.class);

        binding = FragmentLearnMoreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LearnMoreViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        // ^^^ Default code

        context = getActivity();

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        String objectDisplayName = ol.getObjectDisplayName();
        String lessonTopic = ol.getLessonTopic();
        String objectDescription = ol.getObjectDefinition();
        String videoLink = ol.getVideoLink();

        binding.titleText.setText(objectDisplayName + " - " + lessonTopic);
        binding.descriptionBubble.setText(objectDescription + "\n\n\n"); // Added newlines to fit speech bubble better
        binding.topicBubble.setText("Let's learn together about " + lessonTopic + "!");
        binding.bookmarkToggle.setChecked(mi.bookmarked);

        binding.watchVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink));
                startActivity(linkIntent);
            }
        });

        binding.backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.bookmarkToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LessonListFragment.setImageBookmark(context, mi.imageID, isChecked);
            }
        });

        binding.avatarImageView.setImageURI(AvatarSelectFragment.getCurAvatar(getActivity()).getImageUri());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setData(ObjectLesson _ol, MyImage _mi) {
        ol = _ol;
        mi = _mi;
    }
}