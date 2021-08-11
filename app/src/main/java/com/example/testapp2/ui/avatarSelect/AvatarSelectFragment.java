package com.example.testapp2.ui.avatarSelect;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp2.AvatarModel;
import com.example.testapp2.AvatarSelectAdapter;
import com.example.testapp2.DataTrackingManager;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.MyFragmentInterface;
import com.example.testapp2.R;
import com.example.testapp2.databinding.FragmentAvatarSelectBinding;

import java.util.ArrayList;
import java.util.List;

public class AvatarSelectFragment extends Fragment implements MyFragmentInterface {

    static List<AvatarModel> avatarList = new ArrayList<>();
    private AvatarSelectAdapter mAdapter;

    private AvatarSelectViewModel avatarSelectViewModel;
    private FragmentAvatarSelectBinding binding;

    String sessionID = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        avatarSelectViewModel =
                new ViewModelProvider(this).get(AvatarSelectViewModel.class);

        binding = FragmentAvatarSelectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ^^^ Default code

        if (sessionID.equals(""))
            sessionID = HelperCode.generateLongID();
        notifyTrackerOfPage();

        RecyclerView recyclerView = binding.avatarSelectRecyclerView;
        mAdapter = new AvatarSelectAdapter(avatarList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        return root;
    }

    public static void prepareAvatarData(Context context) {
        Log.e("Stuff", "Prepare avatar data called");
        if (avatarList.size() > 0)
            return; // since avatarList is static, don't add all these each time

        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.william)
                + '/' + context.getResources().getResourceTypeName(R.drawable.william) + '/' + context.getResources().getResourceEntryName(R.drawable.william) );
        AvatarModel avatar = new AvatarModel("William", "Education: PhD in Physics from the University of Pennsylvania\n\n" +
                "Interests: Teaching physics through everyday objects\n\n" +
                "Role model: Sir Isaac Newton\n\n", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.jessica)
                + '/' + context.getResources().getResourceTypeName(R.drawable.jessica) + '/' + context.getResources().getResourceEntryName(R.drawable.jessica) );
        avatar = new AvatarModel("Jessica", "Education: PhD in Physics from the University of Cambridge\n\n" +
                "Interests: Finding scientific value in everyday objects\n\n" +
                "Role model: Stephen Hawking\n\n", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.derek)
                + '/' + context.getResources().getResourceTypeName(R.drawable.derek) + '/' + context.getResources().getResourceEntryName(R.drawable.derek) );
        avatar = new AvatarModel("Derek", "Education: PhD in Mechanical Engineering from the Stanford University\n\n" +
                "Interests: Observing and understanding how things work in everyday environments\n\n" +
                "Role Model: James Watt\n\n", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.emma)
                + '/' + context.getResources().getResourceTypeName(R.drawable.emma) + '/' + context.getResources().getResourceEntryName(R.drawable.emma) );
        avatar = new AvatarModel("Emma", "Education: Master’s in Electronics Engineering from the Massachusetts Institute of Technology\n\n" +
                "Interests: Opening up everyday objects and learning how they work\n\n" +
                "Role Model: Nikola Tesla\n\n", imageUri);
        avatarList.add(avatar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static AvatarModel getCurAvatar(Context context) {
        String curAvatarName = HelperCode.getSharedPrefsObj(context).getString(GlobalVars.AVATAR_NAME_PREF_KEY, "Emma");

        for (AvatarModel am : avatarList) {
            if (am.getName().equals(curAvatarName)) {
                return am;
            }
        }
        Log.e("Stuff", "Avatar with name \"" + curAvatarName + "\" does not exist");
        for (AvatarModel am : avatarList) {
            Log.e("Stuff", am.getName());
        }
        throw new RuntimeException("Avatar with name \"" + curAvatarName + "\" does not exist");
    }

    public static void setCurAvatar(Context context, String name) {
        HelperCode.setAvatarNamePref(context, name);
    }

    public void setSessionID(String _sessionID) {
        sessionID = _sessionID;
    }

    @Override
    public void notifyTrackerOfPage() {
        DataTrackingManager.pageChange(this, sessionID, "AvatarSelect");
    }
}