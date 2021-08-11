package com.example.testapp2.ui.signIn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.MainActivity;
import com.example.testapp2.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {

    private SignInViewModel signInViewModel;
    private FragmentSignInBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signInViewModel =
                new ViewModelProvider(this).get(SignInViewModel.class);

        binding = FragmentSignInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Default code ^^^
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        EditText inputIDText = binding.inputID;
        binding.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = inputIDText.getText().toString();
                if (!id.equals("")) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("participantID", id);
                    startActivity(intent);
                }
                else { // TODO: define other specifications for a "valid" ID (ie special characters and stuff; anything that's compatible with firebase)
                    Toast.makeText(getActivity(), "Please enter a valid ID", Toast.LENGTH_SHORT).show();
                }
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