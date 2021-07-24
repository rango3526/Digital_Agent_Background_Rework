package com.example.testapp2.ui.avatarSelect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AvatarSelectViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AvatarSelectViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}