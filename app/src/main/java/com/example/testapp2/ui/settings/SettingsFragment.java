package com.example.testapp2.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.AlarmReceiver;
import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MainActivity;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.R;
import com.example.testapp2.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment implements MyFragmentInterface {

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
        notifyTrackerOfPage();

        boolean appRefreshStatus = HelperCode.getSharedPrefsObj(getContext()).getBoolean(GlobalVars.APP_REFRESH_SETTING_PREF_KEY, true);
        binding.appRefreshToggle.setChecked(appRefreshStatus);

        binding.appRefreshToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HelperCode.initializeAlarmManager(getContext());
                    HelperCode.setTestAlarm(getContext());
                    AlarmReceiver.alarmTriggered(getContext());
                    DataTrackingManager.resumeRefreshClicked(sessionID);
                }
                else {
                    DataTrackingManager.stopRefreshClicked(sessionID);
                    HelperCode.cancelAlarm(getContext());
                }

                HelperCode.getSharedPrefsObj(getContext()).edit().putBoolean(GlobalVars.APP_REFRESH_SETTING_PREF_KEY, isChecked).apply();
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

    @Override
    public void notifyTrackerOfPage() {
        DataTrackingManager.pageChange(this, sessionID, "Settings");
    }
}