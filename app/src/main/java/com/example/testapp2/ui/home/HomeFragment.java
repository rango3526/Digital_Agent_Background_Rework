package com.example.testapp2.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.databinding.FragmentHomeBinding;
import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;
import com.example.testapp2.BuildConfig;

public class HomeFragment extends Fragment implements MyFragmentInterface {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    Context context;
    String sessionID = "";

    static boolean passingOverFragment = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ^^^ Above is the default code

        context = getActivity();
        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();

        // If just passing over, it should not record it because the user isn't explicitly visiting it
        if (!passingOverFragment) {
            notifyTrackerOfPage();
        }
        else
            passingOverFragment = false;


        // Begin searching for photos
        HelperCode.initializeAlarmManager(context);
        HelperCode.setTestAlarm(context);

//        AvatarSelectFragment.prepareAvatarData(getActivity());

        binding.avatarImageView.setImageURI(AvatarSelectFragment.getCurAvatar(getActivity()).getImageUri());

        return root;
    }

    public static void passOverFragment() {
        passingOverFragment = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void notifyTrackerOfPage() {
        DataTrackingManager.pageChange(this, sessionID, "Home");
    }
}