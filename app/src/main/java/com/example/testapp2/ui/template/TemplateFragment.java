package com.example.testapp2.ui.template;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.HelperCode;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.databinding.FragmentSlideshowBinding;

public class TemplateFragment extends Fragment implements MyFragmentInterface {

    private com.example.testapp2.ui.template.TemplateViewModel templateViewModel;
    private FragmentSlideshowBinding binding;

    private String sessionID = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        templateViewModel =
                new ViewModelProvider(this).get(com.example.testapp2.ui.template.TemplateViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Default code ^^^

        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();
        notifyTrackerOfPage();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void notifyTrackerOfPage() {

    }
}