package com.example.testapp2.ui.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.databinding.FragmentOnBoardingBinding;
import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;

public class OnBoardingFragment extends Fragment implements MyFragmentInterface {

    private OnBoardingViewModel templateViewModel;
    private FragmentOnBoardingBinding binding;

    String sessionID = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        templateViewModel =
                new ViewModelProvider(this).get(OnBoardingViewModel.class);

        binding = FragmentOnBoardingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // default code ^^^
        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();
        notifyTrackerOfPage();

        binding.avatarImageView.setImageURI(AvatarSelectFragment.getCurAvatar(getActivity()).getImageUri());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void notifyTrackerOfPage() {
        DataTrackingManager.pageChange(this, sessionID, "OnBoarding");
    }
}