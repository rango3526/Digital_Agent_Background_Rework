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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp2.AvatarModel;
import com.example.testapp2.AvatarSelectAdapter;
import com.example.testapp2.GlobalVars;
import com.example.testapp2.HelperCode;
import com.example.testapp2.R;
import com.example.testapp2.databinding.FragmentAvatarSelectBinding;

import java.util.ArrayList;
import java.util.List;

public class AvatarSelectFragment extends Fragment {

    static List<AvatarModel> avatarList = new ArrayList<>();
    private AvatarSelectAdapter mAdapter;

    private AvatarSelectViewModel avatarSelectViewModel;
    private FragmentAvatarSelectBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        avatarSelectViewModel =
                new ViewModelProvider(this).get(AvatarSelectViewModel.class);

        binding = FragmentAvatarSelectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ^^^ Default code

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
        AvatarModel avatar = new AvatarModel("William", "Meet William. He was always fascinated by the gravitational force and its invention and thus a huge follower of Ser Isaac Newton. He pursued this love for Physics by getting a PhD in the field from University of Pennsylvania and is passionate about educating young people about Physics in everyday life.", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.jessica)
                + '/' + context.getResources().getResourceTypeName(R.drawable.jessica) + '/' + context.getResources().getResourceEntryName(R.drawable.jessica) );
        avatar = new AvatarModel("Jessica", "Meet Jessica. She got interested in Physics and Mathematics during her school years after reading about Newton’s laws. She is an ardent believer that one can gather a lot of knowledge by observing things present in one’s surroundings with curiosity, inspired by the great scientist Isaac Newton. Jessica further pursued their education at the University of Cambridge where she was taught by Stephen Hawking and currently works as an educator and entrepreneur helping others pique their interest in everyday objects.", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.derek)
                + '/' + context.getResources().getResourceTypeName(R.drawable.derek) + '/' + context.getResources().getResourceEntryName(R.drawable.derek) );
        avatar = new AvatarModel("Derek", "Meet Derek. His passion for Mechanics developed at the age of 10 years, while satisfying his curiosity of knowing how exactly a lever works. He pursued this passion by just observing the incidents and things around him and learning about its cause and operation. He graduated from Stanford University with a Doctorate in Mechanical Engineering and started working at GE Motors and is now leading a team at Hyundai. He also has his own Youtube channel wherein he explains how a device operates every week and aspires to inspire the next generation to cherish their curiosity and build on it.", imageUri);
        avatarList.add(avatar);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.emma)
                + '/' + context.getResources().getResourceTypeName(R.drawable.emma) + '/' + context.getResources().getResourceEntryName(R.drawable.emma) );
        avatar = new AvatarModel("Emma", "Meet Emma. She was always interested in knowing how a particular object or electronic device works. Her favorite hobby used to be disintegrating electronic devices and learning about their internal configuration. She cherished her innate love for mechanics and electronics by pursuing a degree in Electronics and Communications Engineering from Massachusetts Institute of Technology. She completed her Masters from MIT as well by which she had found her focus: Self-driving cars. Currently, she works as a Senior Communications Engineer at Tesla and hopes to pass on her passion for electronics to the youth.", imageUri);
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
}