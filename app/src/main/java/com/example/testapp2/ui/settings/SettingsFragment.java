package com.example.testapp2.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MainActivity;
import com.example.testapp2.R;
import com.example.testapp2.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private SettingsViewModel templateViewModel;
    private FragmentSettingsBinding binding;

    String sessionID = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        templateViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Default code ^^^
        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();
        DataTrackingManager.pageChange(sessionID, "Settings");

        binding.stopAnalyzing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataTrackingManager.stopRefreshClicked(sessionID);
                HelperCode.cancelAlarm(getActivity());
                // TODO: change text on button to something like "Resume App" and make that work accordingly
            }
        });

        binding.changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.nav_avatarSelect);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}